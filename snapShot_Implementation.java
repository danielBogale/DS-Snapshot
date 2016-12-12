package snapShot;
import java.rmi.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import snapShot.snapShot_Client;

public class snapShot_Implementation extends UnicastRemoteObject implements snapShot_Interface{
	private static final long serialVersionUID = 1L;
	int totalBalance = 1000000;	
	int snapTotal = 1000000;	
	boolean startedTransaction = false;
	boolean snapShotOn = false;
	int snapShotId = 0;
	ArrayList<Integer> listOfNodes = new ArrayList<Integer>();
	ArrayList<ArrayList<Integer>> numSeqList = new ArrayList<ArrayList<Integer>>();
	ArrayList<ArrayList<Integer>> checkOnLink = new ArrayList<ArrayList<Integer>>();  


	protected snapShot_Implementation() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	public ArrayList<Integer> check() throws RemoteException {
		return (listOfNodes);
	}
	public int register(int id) throws RemoteException {
		listOfNodes.add(id);
		return (id);
	}
	public boolean startedTransaction() throws RemoteException {
		return (startedTransaction);
	}
	public int getBalance() throws RemoteException {
		startedTransaction = true;
		return (totalBalance);
	}
	public void received(int amount) throws RemoteException {
		totalBalance = totalBalance + amount;
	}
	public void sent(int amount) throws RemoteException {
		totalBalance = totalBalance - amount;
	}
	public void startT(int id) throws RemoteException {
		snapShot_Client.startTransaction(id);
	}
	public void startSnap(int id) throws RemoteException {
		snapShot_Client.takeSnapShot(id);
	}
	public void regSeq(ArrayList<ArrayList<Integer>> seqNumber) throws RemoteException {
		numSeqList = seqNumber;
	}
	public void regCheckOnLink(ArrayList<ArrayList<Integer>> checkMoneyOnLink) throws RemoteException {
		checkOnLink = checkMoneyOnLink;
	}
	public ArrayList<ArrayList<Integer>> getSeq() throws RemoteException {
		return (numSeqList);
	}
	public ArrayList<ArrayList<Integer>> getCheckOnLink() throws RemoteException {
		return (checkOnLink);
	}
	public int sendAndReceive (int senderID, int id, int amount, int seqNum, String transaction) throws RemoteException {
		int delivered = -1;
		if (seqNum != 0) {
			//System.out.println("Received : " + amount + "   sequNumb : " + seqNum + "   fromNode : " + senderID);
			delivered = snapShot_Client.sendAndReceive2(id, senderID, amount, seqNum, "receive");
		}
		else if (seqNum == 0){
			//System.out.println(transaction);
			delivered = snapShot_Client.sendAndReceive3(id, senderID, amount, seqNum, "token");
		}
		return (delivered);
	}
	public void setSnap(boolean snapOn) throws RemoteException {
		if(!snapOn){
			snapShotOn = false;
			snapShotId = 0;
			for (int i = 0; i < numSeqList.size(); i++){
				numSeqList.get(i).set(2,0);
				numSeqList.get(i).set(3,0);
				checkOnLink.get(i).set(1,0);
			}
		}
		else {
			snapTotal = totalBalance;		
			snapShotOn = true;
		}
	}
	public boolean checkSnap() throws RemoteException  {
		return (snapShotOn);
	}
	public int getSnapId() throws RemoteException {
		return (snapShotId);
	}
	public void setSnapId(int snapId) throws RemoteException {
		//snapTotal = totalBalance;		
		snapShotId = snapId;
	}
	public int getSnapTotal() throws RemoteException {
		return (snapTotal);
	}
}
