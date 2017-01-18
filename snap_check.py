import sys
import re

# {snapshot:sum}
res = {}

r = re.compile("(?P<s>\d+)\s+(?P<a>\d+)\s+(?P<t>\d+)")

for fn in sys.argv[1:]:
    with open(fn, "r") as f:
        for l in f:
            try:
                #snap, state, link = (int(x) for x in l.split())
                snap, state, link = (int(x) for x in r.match(l).groups())
                if (snap in res):
                    res[snap] += state + link
                else:
                    res[snap] = state + link
            except Exception, e:
                print "Skipping line:", l
                #print e

for sn in sorted(res.keys()):
    print sn, res[sn]
