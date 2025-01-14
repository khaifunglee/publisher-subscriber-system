package broker;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import client.*;
import remote.*;
import topic.Topic;

/*
 * This class implements all of the methods from the IRemoteBroker interface to be called remotely by clients (subscribers, publishers, or other brokers).
 * Khai Fung Lee 1242579
 */

public class BrokerServer extends UnicastRemoteObject implements IRemoteBroker {
	
	// Instance variables that each broker has
	private static ConcurrentHashMap<String, Publisher> pubList = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, Subscriber> subList = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, IRemoteSubscriber> subServerList = new ConcurrentHashMap<>();
	protected ArrayList<IRemoteBroker> brokerList = new ArrayList<>();
	
	protected BrokerServer() throws RemoteException {
		super();
	}

	private static final long serialVersionUID = 1L;
	
	// Test method that is used to start message exchange protocol to confirm connection between client & server
	@Override
	public synchronized String sayHello(String msg) throws RemoteException {
		return msg;
	}
	
	// This method is to connect two brokers together (two-way connection)
	@Override
	public void addBroker(IRemoteBroker broker) throws RemoteException {
		
		// Add broker to this broker's brokerList if it hasn't been added already
		if (!brokerList.contains(broker)) {
			brokerList.add(broker);
			
			System.out.println("Added broker: " + broker);
			
			// Now add this broker to the other broker's list (bi-directional)
			try {
				if (!broker.getBrokerList().contains(this)) {
					broker.addBroker(this);
					System.out.println("Just did the reverse lol, adding broker: "+this);
				}
				
			} catch (RemoteException e) {
				System.err.println("Error adding this broker to the new broker's list: " + e.getMessage());
			}			
			System.out.println("Current Broker List size: " + brokerList.size());
			
		} else {
			System.out.println("Broker is already in list, skipping addition.");
		}
		
	}
	
	// This method is to make sure broker and subscriber are connected on each side and can communicate with each other
	@Override
	public synchronized void addSubServer(IRemoteSubscriber subServer) throws RemoteException {
		
		// Add subscriber server to this broker's SubServerList if it hasn't been added already
		if (!this.subServerList.contains(subServer)) {
			this.subServerList.put(subServer.getName(), subServer);
			
			System.out.println("Added SubServer: " + subServer.getName());
			
			// Now add this broker to the other broker's list (bi-directional)
			try {
				if (subServer.getBrokerServer() != this) {
					subServer.setBrokerServer(this);
					System.out.println("Just did the reverse lol, adding broker to this subServer: "+this);
				}
				
			} catch (RemoteException e) {
				System.err.println("Error adding this broker to the new broker's list: " + e.getMessage());
			}			
			
		} else {
			System.out.println("This sub server is already in the list, skipping addition.");
		}
		
	}
	// This method retrieves the broker list (list of brokers that this broker is connected to)
	@Override
	public synchronized ArrayList<IRemoteBroker> getBrokerList() throws RemoteException {
	    return brokerList;
	}
	
	// Retrieve up to date publisher from broker network
	@Override
	public synchronized Publisher getPublisher(Publisher publisher) throws RemoteException {
		
		// Iterate over broker's pubList to find the up to date version of this publisher
		System.out.println("Getting publisher: "+publisher);
		Publisher upToDatePub = pubList.get(publisher.getName());
		return upToDatePub;
	}
	// Retrieve up to date subscriber from broker network
	@Override
	public synchronized Subscriber getSubscriber(Subscriber subscriber) throws RemoteException {
		
		// Iterate over broker's subList to find up to date version of subscriber
		Subscriber upToDateSub = subList.get(subscriber.getName());
		return upToDateSub;
		
	}
	
	// Updates publisher/subscriber info to publisher/subscriber hash map and shares it across other brokers
	@Override
	public synchronized void updateInfo(Client client) throws RemoteException {
		// If client is publisher
		if (client instanceof Publisher) {
			Publisher publisher = (Publisher) client;
			
			pubList.put(publisher.getName(), publisher);
			System.out.println("Publisher updated: " + publisher);
			
			// Notify other brokers to update publisher info in their pubList
			for (IRemoteBroker broker : brokerList) {
				broker.updatePubList(publisher);
			}
			
		// If client is subscriber
		} else if (client instanceof Subscriber){
			Subscriber subscriber = (Subscriber) client;
			
			subList.put(subscriber.getName(), subscriber);
			System.out.println("Subscriber updated: " + subscriber);
			
			// Notify other brokers to update subscriber info to their subList
			for (IRemoteBroker broker : brokerList) {
				broker.updateSubList(subscriber);
			}
		}
		
	}
	
	// Updates publisher's topic list after a 'sub' function has occurred (add subscriber to publisher's topic)
	@Override
	public synchronized void addSub(Topic topic, String subscriberName) throws RemoteException {
		// Find publisher to update
		String publisherName = topic.getPublisher();
		Publisher pubToUpdate = pubList.get(publisherName);
		
		System.out.println("Adding "+subscriberName+" to "+publisherName+"'s sub list in the topic list.");
		// Add subscriber to this publisher's topic's subList
		for (Topic topicToUpdate : pubToUpdate.getTopics()) {
			if (topicToUpdate.getID() == topic.getID()) {
				
				// If topic matches, then add subscriber name to this topic's sublist
				System.out.println("Adding "+subscriberName+" to publisher's topic sub list.");
				topicToUpdate.addSub(subscriberName);
			}
		}
		
		// For every function called, modified changes should be reflected across all brokers.
		this.updateInfo(pubToUpdate);
	}
	
	// Update publisher's topic list after 'unsub' function (remove subscriber from publisher's topic)
	@Override
	public synchronized void removeSub(Topic topic, String subscriberName) throws RemoteException {
		// Find publisher to update
		String publisherName = topic.getPublisher();
		Publisher pubToUpdate = pubList.get(publisherName);
		
		System.out.println("Removing "+subscriberName+" from "+publisherName+"'s sub list in the topic list.");
		// Remove subscriber from this publisher's topic's subList
		for (Topic topicToUpdate : pubToUpdate.getTopics()) {
			if (topicToUpdate.getID() == topic.getID()) {
				// If topic matches, then remove subscriber name from this topic's subList
				System.out.println("Removing "+subscriberName+" to publisher's topic sub list.");
				topicToUpdate.removeSub(subscriberName);
			}
		}
		
		// For every function called, modified changes should be reflected across all brokers.
		this.updateInfo(pubToUpdate);
	}
	
	// This method is for brokers only, updates pubList in other brokers
	@Override
	public synchronized void updatePubList(Publisher publisher) throws RemoteException {
	
		pubList.put(publisher.getName(), publisher);
		System.out.println("Publisher added to this broker's list: " + publisher.getName());
		System.out.println("\nCurrent full publisher list: "+pubList);
	
	}
	// This method is for brokers only, updates subList in other brokers
	@Override
	public synchronized void updateSubList(Subscriber subscriber) throws RemoteException {
		subList.put(subscriber.getName(), subscriber);
		System.out.println("Subscriber added to this broker's list: " + subscriber.getName());
		System.out.println("\nCurrent full subscriber list: "+subList);
	}
	
	// Create new topic under this publisher's name and add to publisher's topicList
	@Override
	public synchronized void createTopic(int topicID, String topicName, Publisher publisher) throws RemoteException {
		
		Topic newTopic = new Topic(topicID, topicName, publisher.getName());
		publisher.addTopic(newTopic);
		
		// For every function called, modified changes should be reflected across all brokers.
		this.updateInfo(publisher);
		
		System.out.println("Successfully created new topic.");
	}

	// Lists all available topics for subscriber
	@Override
	public synchronized ArrayList<Topic> listTopics() throws RemoteException {
		// Initialize topicList
		ArrayList<Topic> topicList = new ArrayList<Topic>();
		
		// Add every publisher's topic list into topicList
		for (ConcurrentHashMap.Entry<String, Publisher> entry : pubList.entrySet()) {
			Publisher publisher = entry.getValue();
			
			for (Topic topic : publisher.getTopics()) {
				topicList.add(topic);
			}
		}
		
		return topicList;
	}
	
	// Show topics that a subscriber is subscribed to based on subList on broker network
	@Override
	public void showSubbedTopics(String username) throws RemoteException {
		// Get subscriber from username
		Subscriber subscriber = subList.get(username);
		subscriber.showSubbedTopics();
		
	}
	
	// This method gets all subscribers that are subscribed to the specified topicID
	@Override
	public synchronized ArrayList<String> getSubscribers(int topicID) throws RemoteException {
		
		ArrayList<String> receiverList = new ArrayList<String>();
		
		// Make sure we are getting the most up-to-date subList in broker network
		for (ConcurrentHashMap.Entry<String, Subscriber> entry : subList.entrySet()) {
			Subscriber subscriber = entry.getValue();
			
			for (Topic topic : subscriber.getSubbedTopics()) {
				if (topicID == topic.getID()) {
					// Send this message to the subscriber so that they receive the message on their console
					System.out.println("Adding "+subscriber.getName()+" to receiver list.");
					receiverList.add(subscriber.getName());
				}
			}
		}
		return receiverList;
	}
	
	// Send publisher-created message to all subscribers of the specified topicID
	@Override
	public synchronized void sendPubMessage(String message, int topicID, ArrayList<String> receiverList) throws RemoteException {
		
		// Now, check iterate over each sub and check if they are connected to this broker.
		// If yes, can send message to them. If not, forward this message to other brokers.
		// This method will keep getting called until every subscriber has received the message, which would make the receiverList empty
		System.out.println("Receiver List: "+receiverList);
		boolean noReceivers = true;
		
		for (String receiverName : receiverList) {
			if (this.subServerList.containsKey(receiverName)) {
				// Get subServer of intended receiver
				IRemoteSubscriber receiverServer = this.subServerList.get(receiverName);
				
				// Send message over
				receiverServer.receiveMessage(message);
				
				System.out.println("Sending message to "+receiverName);
				noReceivers = false;
				
			}
		}
		// If no intended receivers connected to this broker, print message then forward to other brokers
		if (noReceivers) {
			System.out.println("This broker is not connected to any subscribers subscribed to this topic.");
		}
		
		// Forward this message to other two brokers (flooding method)
		for (IRemoteBroker broker : this.brokerList) {
			System.out.println("Forwarding this message to other brokers...");
			broker.forwardMessage(message, topicID, receiverList);
		}	
	}
	
	// This method is for brokers that haven't received the publisher message
	@Override
	public synchronized void forwardMessage(String message, int topicID, ArrayList<String> receiverList) throws RemoteException {
		
		// Now, check iterate over each sub and check if they are connected to this broker.
		// If yes, can send message to them. If not, print error message.
		// This method will keep getting called until every subscriber has received the message, which would make the receiverList empty
		System.out.println("Forwarded receiver List: "+receiverList);
		boolean noReceivers = true;
		
		for (String receiverName : receiverList) {
			// If this subscriber is connected to this broker...
			if (this.subServerList.containsKey(receiverName)) {
				// Get subServer of intended receiver
				IRemoteSubscriber receiverServer = this.subServerList.get(receiverName);
				
				System.out.println("Sending message to "+receiverName);
				// Send message over
				receiverServer.receiveMessage(message);
				
				noReceivers = false;
			}
		}
		// If no intended receivers connected to this broker, print message then forward to other brokers
		if (noReceivers) {
			System.out.println("This broker is not connected to any subscribers subscribed to this topic.");
		}
	}
	
	// Remove topic from broker network (aka subList), then notify subscribers (how?)
	@Override
	public synchronized void removeTopic(int topicID, ArrayList<String> receiverList) throws RemoteException {
		
		String notification = "TopicID "+topicID+ " has been removed from the system. Automatically unsubscribing.";
		boolean noReceivers = true;
		System.out.println(receiverList);
		// Remove topic from subscriber's subbed list then relay updated subscriber info to other brokers
		for (String receiverName : receiverList) {
			// Get subscriber
			Subscriber subscriber = subList.get(receiverName);
			
			// Get topic to unsubscribe from topicID
			Topic topic = null;
			
			for (Topic subbedTopic : subscriber.getSubbedTopics()) {
				if (subbedTopic.getID() == topicID) {
					topic = subbedTopic;
				}
			}
			
			System.out.println("Automatically unsubscribing "+subscriber.getName()+" from topicID "+topic);
			subscriber.unsubscribe(topic);
			
			// Relay updated subscriber info to other brokers
			this.updateInfo(subscriber);
			
			// Check whether this subscriber is connected to this broker. if yes, send notification msg. if not, forward to other brokers
			if (this.subServerList.containsKey(receiverName)) {
				// Get subServer of intended receiver
				IRemoteSubscriber subServer = this.subServerList.get(subscriber.getName());
				
				System.out.println("Sending notification to "+subscriber.getName());
				subServer.receiveMessage(notification);
				noReceivers = false;
			} else {
				System.out.println("Subscriber "+subscriber.getName()+" is not connected to this broker.");
			}
			
		}
		if (noReceivers) {
			System.out.println("This broker is not connected to any subscribers subscribed to this topic.");
		}
		
		// Forward this message to other two brokers (flooding method)
		for (IRemoteBroker broker : this.brokerList) {
			System.out.println("Forwarding this message to other brokers...");
			broker.forwardMessage(notification, topicID, receiverList);
		}	
		
	}
	
	// Run this method when subscriber program crashes...
	@Override
	public synchronized void deleteSubscriber(Subscriber subToRemove) throws RemoteException {
		// Remove subscriber from broker subList
		System.out.println("Unsubscribing from all topics that subscriber subbed to: "+subToRemove.getSubbedTopics());
		
		for (Topic topicToRemove : subToRemove.getSubbedTopics()) {
			System.out.println("Removing "+topicToRemove.getID()+ " from this subscriber.");

			subToRemove.unsubscribe(topicToRemove);
			// Make sure that the publishers that created this topic lose this subscriber
			removeSub(topicToRemove, subToRemove.getName());	
		}
		
		System.out.println("Removing subscriber from system");
		subList.remove(subToRemove.getName());
		
		System.out.println("Publisher info: "+pubList);
		
		// Update subList on other brokers
		for (IRemoteBroker broker : brokerList) {
			broker.deleteSubList(subToRemove);
		}
		
	}
	
	// Run this method when publisher program crashes...
	@Override
	public synchronized void deletePublisher(Publisher pubToRemove) throws RemoteException {
		// Remove publisher from broker pubList
		System.out.println("Removing all topics that this publisher created: "+pubToRemove.getTopics());
		
		for (Topic topicToRemove : pubToRemove.getTopics()) {
			System.out.println("Removing "+topicToRemove.getID()+" from the system.");
			
			pubToRemove.removeTopic(topicToRemove.getID());
			// Make sure that the subscribers that subscribed to this topic are notified of topic getting deleted
			ArrayList<String> receiverList = getSubscribers(topicToRemove.getID());
			
			removeTopic(topicToRemove.getID(), receiverList);
		}
		
		System.out.println("Removing publisher from system");
		pubList.remove(pubToRemove.getName());
		// Debug message to make sure all subscribers that subbed to this publisher's topics aren't subbed anymore
		System.out.println("New subscriber info: "+subList);
		
		// Update subList on other brokers
		for (IRemoteBroker broker : brokerList) {
			broker.deletePubList(pubToRemove);
		}
		
	}
	
	// This method is for brokers only, updates subList in other brokers
	@Override
	public synchronized void deleteSubList(Subscriber subscriber) throws RemoteException {
		// Remove subscriber from other brokers
		subList.remove(subscriber.getName());
		
		// Remove subscriber from broker subList
		System.out.println("Unsubscribing from all topics that subscriber subbed to");
		for (Topic topicToRemove : subscriber.getSubbedTopics()) {
			subscriber.unsubscribe(topicToRemove);
			
			removeSub(topicToRemove, subscriber.getName());
		}
		
		System.out.println("Subscriber removed from this broker's list: " + subscriber.getName());
		System.out.println("\nCurrent full subscriber list: "+subList);
		System.out.println("\nCurrent full publisher list: "+pubList);
		
	}
	
	// This method is for brokers only, updates pubList in other brokers
	@Override
	public synchronized void deletePubList(Publisher publisher) throws RemoteException {
		// Remove publisher from other brokers
		pubList.remove(publisher.getName());
		
		// Remove publisher from broker pubList
		System.out.println("Deleting all topics that publisher created");
		for (Topic topicToRemove : publisher.getTopics()) {
			publisher.removeTopic(topicToRemove.getID());
			
			ArrayList<String> receiverList = getSubscribers(topicToRemove.getID());
			
			removeTopic(topicToRemove.getID(), receiverList);
		}
		
		System.out.println("Publisher removed from this broker's list: " + publisher.getName());
		System.out.println("\nCurrent full subscriber list: "+subList);
		System.out.println("\nCurrent full publisher list: "+pubList);
		
	}
	
}
