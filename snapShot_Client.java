package snapShot;
import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import snapShot.snapShot_Implementation;
import snapShot.snapShot_Client;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class snapShot_Client extends Thread {
	//static boolean locked = false;
	//static boolean locked3 = false;
	static int snapID2 = 0;

    static Semaphore token_mux = new Semaphore(1);
    static Semaphore tx_mux = new Semaphore(1);

    	public static void main(String[] args) {
		int ID = getID();
		checkID(ID);
	}
	public static int getID(){
    		int ID = 0;
    		Scanner scan = new Scanner(System.in);
    		System.out.println("Give Id for this Node: ");
		while (!scan.hasNextInt()){ 
       			System.out.println("Not A valid ID \n");
        		scan.next();
   	 	}
   	 	ID = scan.nextInt(); 
 		return ID;
	}
	public static void checkID(int ID){
		int hostID;
    		Scanner scan = new Scanner(System.in);
    		System.out.println("ID of one node to connect to, or <0> to start it as a first node: ");
		while (!scan.hasNextInt()){ 
       			System.out.println("Not A valid ID \n");
        		scan.next();
   	 	}
		hostID = scan.nextInt();
		if (hostID > 0 && hostID != ID){
			ArrayList<Integer> newNodeList = new ArrayList<Integer>();
			String url = new String("rmi://localhost/"+Integer.toString(hostID));
			try {
				snapShot_Interface snInt = (snapShot_Interface)Naming.lookup(url);
				newNodeList = snInt.check();
			}
			catch(Exception e) {
				System.out.println("Cant fined Node"+Integer.toString(hostID) + ", try again pls.");
				System.exit(0);
			}
			for (int i = 0; i< newNodeList.size(); i++){
				if (newNodeList.get(i) == ID){
					System.out.println("Node" + ID + " alredy exist, please try again.");
					System.exit(0);
				}
			}
			try {
				snapShot_Implementation sn = new snapShot_Implementation();
				Naming.rebind(Integer.toString(ID), sn);
				System.out.println("Node" + ID + " is ready . . .");
			}
			catch (Exception e) {
    				e.printStackTrace();
				System.exit(0);
			}
			newNodeList.add(hostID);
			connect(ID, newNodeList);
		}
		else if (hostID == 0){
			try {
				snapShot_Implementation sn = new snapShot_Implementation();
				Naming.rebind(Integer.toString(ID), sn);
				System.out.println("Node" + ID + " is ready . . .");
			}
			catch (Exception e) {
    				e.printStackTrace();
			}
		}
		else if (hostID == ID){
			System.out.println("ahh .. try also sending ur self a facebook request ..");
		}
		else System.out.println("Invalid hostID..");
	}
	public static void connect(int id, ArrayList<Integer> nodes){
		int ID = id;
		int maxtry = 0;
		String myUrl = new String("rmi://localhost/"+Integer.toString(ID));
		while (!(nodes.isEmpty()) && maxtry <5) {
			String url = new String("rmi://localhost/"+Integer.toString(nodes.get(0)));
			try {
				snapShot_Interface snInt = (snapShot_Interface)Naming.lookup(url);
				snInt.register(ID);
				snapShot_Interface snInt2 = (snapShot_Interface)Naming.lookup(myUrl);
				snInt2.register(nodes.get(0));
				System.out.println("Connected to Node"+nodes.get(0) );
				nodes.remove(0);
				maxtry = 0;
			}
			catch(Exception e) {
				maxtry++;
				if(maxtry == 5){
					nodes.remove(0);
					maxtry = 0;
				}				
			}
		}
		System.out.println("Connect as much nodes as you needded \n and < start > to start TRANSACTION ...");
   		Scanner scanner = new Scanner(System.in);
    		String start = scanner.nextLine();
		while (!start.equals("start")){ 
       			System.out.println("type in < start > to start transaction" + start);
        		//scanner.next();
			start = scanner.nextLine();
   	 	}
  		//scanner.nextLine();
		start(id);		
	}
	public static void start(int id){
		String myUrl = new String("rmi://localhost/"+Integer.toString(id));
		try {
			snapShot_Interface snInt2 = (snapShot_Interface)Naming.lookup(myUrl);
			ArrayList<Integer> nodes = new ArrayList<Integer>();
			nodes = snInt2.check();
			for (int i = 0; i<nodes.size(); i++){	
				int hostID = nodes.get(i);
				new startThread(hostID).start();
			        new startThread2(hostID).start();				
			}
			new startThread2(id).start();
		}
		catch(Exception e) {
			System.out.println("Failed to Connect!!");
		}
		startTransaction(id);
	}
	public static void startTransaction(int id){
		Random randomGenerator = new Random();
		String myUrl = new String("rmi://localhost/"+Integer.toString(id));
		ArrayList<Integer> transactionList = new ArrayList<Integer>();
		try {
			snapShot_Interface snInt2 = (snapShot_Interface)Naming.lookup(myUrl);
			transactionList = snInt2.check();
			for (int i = transactionList.size()-1; i>=0; i--){
				if(transactionList.get(i) == id){
					transactionList.remove(i);
				}
				int maxtry = 1;
				while (maxtry <5 && maxtry != 0) {
				String url = new String("rmi://localhost/"+Integer.toString(transactionList.get(i)));
					try {
						snapShot_Interface snInt = (snapShot_Interface)Naming.lookup(url);
						snInt.check();
						maxtry = 0;
					}
					catch(Exception e) {
						maxtry++;
						if (maxtry == 5){
							transactionList.remove(i);
						}
					}
				}				
			}
		}
		catch(Exception ex) {
			System.out.println("Failed to Connect!!");
		}
		ArrayList<ArrayList<Integer>> numSeq = new ArrayList<ArrayList<Integer>>(); 
		ArrayList<ArrayList<Integer>> checkOnLink = new ArrayList<ArrayList<Integer>>(); 
		for(int i = 0; i < transactionList.size(); i++)  {
			numSeq.add(new ArrayList<Integer>());
			numSeq.get(i).add(transactionList.get(i));
			numSeq.get(i).add(0);
			numSeq.get(i).add(0);
			numSeq.get(i).add(0);

			checkOnLink.add(new ArrayList<Integer>());
			checkOnLink.get(i).add(transactionList.get(i));
			checkOnLink.get(i).add(0);
		}
		try {
			snapShot_Interface snInt2 = (snapShot_Interface)Naming.lookup(myUrl);
			snInt2.regSeq(numSeq);
			snInt2.regCheckOnLink(checkOnLink);

		}
		catch(Exception e) {}
		System.out.println("Transaction started with peer nodes: "+transactionList);
		Timer t = new Timer();
		int seqNum = 1;
		int hostID = transactionList.get(randomGenerator.nextInt(transactionList.size()));
		int amount = randomGenerator.nextInt(100) + 1;
		int time = randomGenerator.nextInt(2000) + 100;
		boolean sending = true;
		int maxtry = 0;
		while(sending){
			int newSeqNum = -1;
//			if(!locked){
//				locked = true;
        	boolean permit = false;
        	boolean permit2 = false;
				try {
			
                			//tx_mux.acquire();
        				permit = tx_mux.tryAcquire(50L, TimeUnit.MILLISECONDS);
        				permit2 = token_mux.tryAcquire(50L, TimeUnit.MILLISECONDS);
                			//token_mux.acquire();
					if (permit & permit2){
              					newSeqNum = sendAndReceive(id,hostID,amount,seqNum, "send");
						token_mux.release();
               					tx_mux.release();
					}
					else if (permit & !permit2){
						tx_mux.release();
					}
					else if (!permit & permit2){
						token_mux.release();
					}
				}
            			catch (Exception ex){System.out.println("snapshot failed please try again 5");}

                		finally {

			//		locked = false;
				}
				//if(maxtry > 3){
				//	newSeqNum = seqNum+1;
				//}
				if(newSeqNum > seqNum){
					hostID = transactionList.get(randomGenerator.nextInt(transactionList.size()));
					amount = randomGenerator.nextInt(100) + 1;
					time = randomGenerator.nextInt(100) + 100;
					seqNum = newSeqNum;
				//	maxtry = 0;
				}
				//maxtry++;
			//}
			try{
				Thread.sleep(time);
			}
			catch(Exception e) {}
		}
	}
	public static void takeSnapShot(int id){
		int tokenSeq = 0;	
		int tokenId;
		String myUrl = new String("rmi://localhost/"+Integer.toString(id));	
		boolean snapShotOn = true;
		while(true) {
			try{
				snapShot_Interface snInt2 = (snapShot_Interface)Naming.lookup(myUrl);		
				snapShotOn = snInt2.checkSnap();

			}
			catch (Exception ex){}
			if(!snapShotOn){
				snapShotOn = true;
				tokenSeq++;
				//System.out.println("Press \"ENTER\" to take a snapshot");
   				Scanner scanner = new Scanner(System.in);
  				scanner.nextLine();
				tokenId = tokenSeq+(id*1000);
				sendAndReceiveToken(id, 0, tokenId, 0, "token");
			}	
		}
	}
	public static int sendAndReceive3(int id, int hostID, int amount, int seqNum, String transaction){
		int returnval = -1;
		returnval = sendAndReceiveToken(id, hostID, amount,seqNum, "token");
		return returnval;
	}

	public static int sendAndReceive2 (int id, int hostID, int amount, int seqNum, String transaction){
		int returnval = -1;
        boolean permit = false;
    
        try {
        permit = tx_mux.tryAcquire(50L, TimeUnit.MILLISECONDS);
		if (permit) {
			//locked = true;
			try {
				returnval = sendAndReceive(id, hostID, amount,seqNum, transaction);
			    tx_mux.release();
            }
            catch (Exception e){}
            }
            }catch (Exception ex){System.out.println("snapshot failed please try again 5");}
            finally {
				//locked = false;
			}
		
		return returnval;
	}

public static int sendAndReceive(int id, int hostID, int amount, int seqNum, String transaction){

		Random randomGenerator2 = new Random();
		int returnval = 0;
		ArrayList<ArrayList<Integer>> seqNumList = new ArrayList<ArrayList<Integer>>(); 
		String myUrl = new String("rmi://localhost/"+Integer.toString(id));
		try {
			snapShot_Interface snInt2 = (snapShot_Interface)Naming.lookup(myUrl);
			int total = snInt2.getBalance();
			//seqNumList = snInt2.getSeq();
			//System.out.println(id);
			if (transaction == "send" && total >= 100) {
				String url = new String("rmi://localhost/" + Integer.toString(hostID));
				try {
					snapShot_Interface snInt = (snapShot_Interface)Naming.lookup(url);
					int delivered = snInt.sendAndReceive(id, hostID, amount, seqNum, "send");
						if (delivered == seqNum) {
							snInt2.sent(amount);
							total = snInt2.getBalance();
							System.out.println("Sent : " + amount + "   seqN : " + seqNum +"  toNd : " + hostID);
							seqNum++;
						}
					
					return seqNum;
				}
				catch (Exception e){}			
			}

			else if (transaction ==  "receive"){
				//System.out.println("MY id is : " + id);
//				locked2 = true;
				ArrayList<ArrayList<Integer>> checkOnLink = new ArrayList<ArrayList<Integer>>(); 
				try{
					seqNumList = snInt2.getSeq();
					boolean snapShotOn = snInt2.checkSnap();
					int snapTotal = 0;
					ArrayList<Integer> checkSnap = new ArrayList<Integer>();

					for (int i = 0; i<seqNumList.size(); i++){
						if (seqNumList.get(i).get(0) == hostID && seqNum > seqNumList.get(i).get(1)) {
					                checkOnLink = snInt2.getCheckOnLink();
							if(snapShotOn && checkOnLink.get(i).get(1)==0){
								seqNumList = snInt2.getSeq();
								seqNumList.get(i).set(3,seqNumList.get(i).get(3)+amount);
								//System.out.println("seq: "+id+ " "+seqNumList);
								//System.out.println("snap:"+checkOnLink);
							}
							seqNumList.get(i).set(1,seqNum);
							snInt2.regSeq(seqNumList);
							snInt2.received(amount);
				//System.out.println("Received : " + amount + "   sequNumb : " + seqNum + "   fromNode : " + hostID);
							snapTotal = snInt2.getSnapTotal();
							seqNumList = snInt2.getSeq();
							checkOnLink = snInt2.getCheckOnLink();
							for (int j = 0; j<checkOnLink.size(); j++){
								checkSnap.add(checkOnLink.get(j).get(1));
							}
							//System.out.println("seq: "+ id + " " +seqNumList);
							//System.out.println("check snap:"+checkOnLink);
							//System.out.println("check snap:"+checkSnap);
							if(!checkSnap.contains(0)){
								int snapId = snInt2.getSnapId();
								snInt2.setSnap(false);
						      		if(snapID2 != snapId && snapId > 1000){
									snapID2 = snapId;
									snapTotal = snInt2.getSnapTotal();
									System.out.println("Snapshot: " + snapId);
									System.out.println("Total: " + snapTotal);
								int totalOnLink = 0;
									for (int k = 0; k<seqNumList.size(); k++){
										System.out.println(seqNumList.get(k).get(0)  + "->" + id +"  "+seqNumList.get(k).get(3));
										totalOnLink = totalOnLink + seqNumList.get(k).get(3);
									}
									System.err.println(""+snapId+ " " + snapTotal + " " + totalOnLink);
								}
							}
							//locked2 = false;
							return seqNum;
						}
					}
				}
				catch (Exception ex){}//locked2 = false;}
			}
		}
		catch (Exception exx){System.out.println("snapshot failed please try again 3");}
		//locked2 = false;
		return -1;
	}
	public static int sendAndReceiveToken(int id, int hostID, int amount, int seqNum, String transaction){	
	
        try {
            
            System.out.println("just recieved token");	
            
            System.out.println("just acquired mutex token");	
            ArrayList<ArrayList<Integer>> checkOnLink = new ArrayList<ArrayList<Integer>>(); 
            String myUrl = new String("rmi://localhost/"+Integer.toString(id));
            int returnval=-1;			
            try {
                snapShot_Interface snInt2 = (snapShot_Interface)Naming.lookup(myUrl);
                checkOnLink = snInt2.getCheckOnLink();
                //int snapID = snInt2.getSnapId();
                boolean snapShotOn = snInt2.checkSnap();
                if (!snapShotOn) {        
                    token_mux.acquire();
                    snInt2.setSnapId(amount);
		    tx_mux.acquire();
                    snInt2.setSnap(true);
		    tx_mux.release();
                    broadcastToken(id, hostID, amount, seqNum, transaction);
                    returnval = seqNum;
       
                    token_mux.release();	 
            System.out.println("just released mutex token");	
                }
            
                for (int i = 0; i<checkOnLink.size(); i++){
                    if (checkOnLink.get(i).get(0) == hostID) {
                        checkOnLink = snInt2.getCheckOnLink();
                        checkOnLink.get(i).set(1,1);
                        snInt2.regCheckOnLink(checkOnLink);
                        //System.out.println("sgdfgdfgdfgdfgdfgdfgdfg");
                        returnval = seqNum;
                    }
                }
            } 
            catch(Exception exxx){System.out.println("snapshot failed please try again 5");}
            return returnval;
        }  
        catch (Exception ex){System.out.println("snapshot failed please try again 5");}
    finally {
        }
        return -1;
    }

	public static void broadcastToken(int id, int hostID, int amount, int seqNum, String transaction){
		Random randomGenerator2 = new Random();
		ArrayList<ArrayList<Integer>> seqNumList = new ArrayList<ArrayList<Integer>>(); 
		String myUrl = new String("rmi://localhost/"+Integer.toString(id));
		try {
			snapShot_Interface snInt2 = (snapShot_Interface)Naming.lookup(myUrl);
			seqNumList = snInt2.getSeq();
			for (int j = 0; j<seqNumList.size(); j++){
				int hostID2 = seqNumList.get(j).get(0);
				String url = new String("rmi://localhost/" + Integer.toString(hostID2));
				snapShot_Interface snInt = (snapShot_Interface)Naming.lookup(url);
				boolean sent = false;
				int delivered = -1;
				while(!sent){
					delivered = snInt.sendAndReceive(id, hostID2, amount,seqNum,"token");				
					if (delivered == seqNum) {
						sent = true;
						//seqNumList.get(j).set(2,1);
						//snInt2.regSeq(seqNumList);
					}
					else {
						try{
							Thread.sleep(50);
						}
						catch (Exception ex){}	
					}		
				}
			}
//			try{
//				Thread.sleep(20);
//			}
			//catch (Exception ex){}
			//locked3 = false;
		}
		catch(Exception exxx){System.out.println("snapshot failed please try again 5");}
		//locked3 = false;
	}		
}
class startThread extends Thread {
	private int hostID;
	public startThread(int hostID){
		this.hostID = hostID;
	}
	public void run(){
		String url = new String("rmi://localhost/"+Integer.toString(hostID));
		try {
			snapShot_Interface snInt = (snapShot_Interface)Naming.lookup(url);
			snInt.startT(hostID);
		}
		catch(Exception e) {
		}			
	}
}
class startThread2 extends Thread {
	private int ID;
	public startThread2(int ID){
		this.ID = ID;
	}
	public void run(){
		String url = new String("rmi://localhost/"+Integer.toString(ID));
		try {
			snapShot_Interface snInt = (snapShot_Interface)Naming.lookup(url);
			snInt.startSnap(ID);
		}
		catch(Exception e) {
		}			
	}
}
