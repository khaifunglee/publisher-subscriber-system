package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import client.*;
import topic.Topic;

/*
 * This interface creates all the methods required by the broker to call to implement subscriber & publisher functions.
 * Khai Fung Lee, 1242579
 */

public interface IRemoteBroker extends Remote {
	public String sayHello(String msg) throws RemoteException;
	
	public void addBroker(IRemoteBroker broker) throws RemoteException;
	
	public void addSubServer(IRemoteSubscriber SubServer) throws RemoteException;
	
	public Publisher getPublisher(Publisher publisher) throws RemoteException;
	
	public Subscriber getSubscriber(Subscriber subscriber) throws RemoteException;
	
	public void updateInfo(Client client) throws RemoteException;
	
	public void updatePubList(Publisher publisher) throws RemoteException;
	
	public void updateSubList(Subscriber subscriber) throws RemoteException;
	
	public void addSub(Topic topic, String subscriberName) throws RemoteException;
	
	public void removeSub(Topic topic, String subscriberName) throws RemoteException;
	
	public void createTopic(int topicID, String topicName, Publisher publisher) throws RemoteException;
	
	public void removeTopic(int topicID, ArrayList<String> receiverList) throws RemoteException;
	
	public void showSubbedTopics(String username) throws RemoteException;
	
	public ArrayList<String> getSubscribers(int topicID) throws RemoteException;
	
	public void sendPubMessage(String message, int topicID, ArrayList<String> receiverList) throws RemoteException;
	
	public void forwardMessage(String message, int topicID, ArrayList<String> receiverList) throws RemoteException;
	
	public ArrayList<Topic> listTopics() throws RemoteException;

	public ArrayList<IRemoteBroker> getBrokerList() throws RemoteException;	
	
	public void deleteSubscriber(Subscriber subscriber) throws RemoteException;
	
	public void deletePublisher(Publisher publisher) throws RemoteException;
	
	public void deleteSubList(Subscriber subscriber) throws RemoteException;
	
	public void deletePubList(Publisher publisher) throws RemoteException;

}
