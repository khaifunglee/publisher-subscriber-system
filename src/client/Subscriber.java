package client;

import java.rmi.Naming;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

import remote.*;
import topic.Topic;

/*
 * This class handles subscriber creation, updating, and implements subscriber functions that are remotely called from brokers.
 * Khai Fung Lee, 1242579
 */

public class Subscriber extends Client implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Topic> subbedTopics; // list of topics subscriber follows
	
	public Subscriber(String username) {
		super(username);
		this.subbedTopics = new ArrayList<Topic>();
	}
	
	// Copy constructor
	public Subscriber(Subscriber other) {
		this.username = other.username;
		// For array lists, need to create a new one and manually add to copy the lists
		this.subbedTopics = new ArrayList<Topic>();
		
		for (Topic topic : other.subbedTopics) {
			this.subbedTopics.add(new Topic(topic));
		}
		
	}
	// Getter methods
	public ArrayList<Topic> getSubbedTopics() {
		return new ArrayList<>(this.subbedTopics); 
	}
	
	@Override
	public String toString() {
		return "Subscriber: "+this.username+", Subbed Topics: "+this.subbedTopics;
	}
	
	// Subscribe to a specified topic
	public void subscribe(Topic topic) {
		
		// Add subscriber to topic's sub list
		topic.addSub(this.username);
		// Add to subbed topics list
		this.subbedTopics.add(topic);
		
	}
	// Unsubscribe to a specified topic
	public void unsubscribe(Topic subbedTopic) {
		
		System.out.println("Removing "+subbedTopic.getName()+" from subbed topics list.\n");
		this.subbedTopics.remove(subbedTopic);
		
		// Also remove subscriber from this topic's subList
		subbedTopic.removeSub(this.username);
	}
	
	// Show full list of topics for subscribers
	public void showSubbedTopics() {
		for (Topic topic : this.subbedTopics) {
			System.out.println(topic);
		}
	}
	
	
	// Subscriber's Main menu
	@Override
	public void showMenu() {
		// This menu should be running continuously until publisher crashes	
		System.out.println("1. list # List all available topics");
		System.out.println("2. sub {topic_id} # Subscribe to a topic");
		System.out.println("3. current # Show your current subscriptions");
		System.out.println("4. unsub {topic_id} # Unsubscribe from a topic");
		System.out.println("Please select command (list, sub, current, unsub): ");
	}
	
	public static void main(String[] args) {
	// Command line arguments: args[0] = username, [1] = broker_ip, [2] = broker_port
		String username = null;
		String brokerIp = null;
		String brokerPort = null;
		
		SubServer subServer = null;
		
		try {
			username = args[0];
			brokerIp = args[1];
			brokerPort = args[2];
		} catch (IndexOutOfBoundsException iobe) {
			System.err.println("Invalid command line arguments:\njava -jar subscriber.jar username broker_ip broker_port");
			// GRACEFULLY EXIT PROGRAM
			System.err.println("Exiting program.");
			System.exit(0);
		}
		
		try {
			// Server-side: Create a registry for subscriber (to receive messages from broker)
			java.rmi.registry.LocateRegistry.createRegistry(0); // let the system choose any available port
			
			// Create new broker to keep track of broker list			
			subServer = new SubServer(username);
			
			java.rmi.Naming.rebind("Subscriber" + username, subServer);
			
			System.out.println("Subscriber" + username + " bound in registry.");
			
		} catch (Exception e) {
			System.err.println("Unable to create subscriber registry: " + e.getMessage());
			// GRACEFULLY EXIT PROGRAM
			System.err.println("Exiting program.");
			System.exit(0);
		}
		
		try {
			// Create new subscriber and connect to broker specified in arguments
			Subscriber subscriber = new Subscriber(username);
			SubscriberWrapper subscriberWrapper = new SubscriberWrapper(subscriber);
			
			Scanner sc = new Scanner(System.in);
			
			IRemoteBroker broker = (IRemoteBroker) Naming.lookup("rmi://"+brokerIp+"/Broker"+brokerPort);
			System.out.println(broker.sayHello("Message from Broker"+brokerPort+": Connection established with "+subscriber.getName()));
			
			// Add subscriber info to connected broker to share across broker network
			broker.addSubServer(subServer);
			broker.updateInfo(subscriber);
			//System.out.println("Adding "+subscriber+" to broker.");
			
			// While console is running, print console command menu
			System.out.println();
			
			while (true) {
				
				subscriber.showMenu();
				String fullInput = sc.nextLine();
				String[] inputs = fullInput.split(" ");
				String command = inputs[0];
				
				// Switch case for different commands
				switch(command.toLowerCase()) {
				case "list":
					// Retrieve topic list from broker
					ArrayList<Topic> topicList = broker.listTopics();
					
					if (topicList.size() == 0) {
						// Print error message for no topics available
						System.out.println("Error: No topics have been created in the system yet.\n");
					} else {
						System.out.println("Listing all available topics from broker: ");
						for (Topic topic : topicList) {
							// List all available topics to subscribe
							// Print output onto subscriber console as specified
							System.out.println(topic);
						}
					}
					
					System.out.println();
					break;
					
				case "sub":
					// Subscribe to a specified topic (takes one argument: int topic_id)
					try {
						// Get updated version of subscriber from broker
						subscriber = broker.getSubscriber(subscriber);
						
						// Get command line parameters
						int topicID = Integer.parseInt(inputs[1]);
						boolean found = false;
						boolean duplicate = false;
						
						// Retrieve topic list from broker
						ArrayList<Topic> allTopics = broker.listTopics();
						
						// For each topic in the topicList, find the corresponding topic based on the ID
						for (Topic topic : allTopics) {
							if (topic.getID() == topicID) {
								
								// Check if this topic is not already in subscriber's subbedTopics list
								for (Topic subbedTopic : subscriber.getSubbedTopics()) {
									if (subbedTopic.getID() == topic.getID()) {
										System.out.println("Error: Subscriber is already subscribed to this topic.\n");
										duplicate = true;
										found = true;
										break;
									}
								}
								if (!duplicate) {
									// Add this topic into this subscriber's subbedTopics list (if it isn't already subbed)
									//System.out.println("Adding topic "+topic+" to "+subscriber.getName()+"'s list.\n");
									subscriber.subscribe(topic);
									
									// Relay updated subscriber info to broker network
									broker.updateInfo(subscriber);
									// Relay updated topic info to broker network (update subList on Publisher class)
									broker.addSub(topic, subscriber.getName());
									
									System.out.println("Successfully subscribed to topic "+topicID);
									
									found = true;
									break;
								}
							}
						}
						if (!found) {
							System.out.println("Error: Could not find topic with this topicID to subscribe to.\n");
						}
						
					} catch (IndexOutOfBoundsException | NumberFormatException e) {
						System.err.println("Error: Invalid arguments. Please try again.\n");
					} 
					
					break;
					
				case "current":
					// Get updated version of subscriber from broker
					subscriber = broker.getSubscriber(subscriber);
					//System.out.println(subscriber);
					
					// Show current subscriptions of subscriber (if subbedList isn't empty)
					if (subscriber.getSubbedTopics().size() == 0) {
						System.out.println("Error: Subscriber has not subscribed to any topics yet.\n");
						
					} else {
						System.out.println("Listing all current subscriptions for "+subscriber.getName());
						subscriber.showSubbedTopics();
					}
					System.out.println();
					break;
					
				case "unsub":
					// Unsubscribe from a specified topic
					// Get updated version of subscriber from broker
					subscriber = broker.getSubscriber(subscriber);
					
					try {
						Topic subbedTopic = null;
						// Get command line parameters
						int topicID = Integer.parseInt(inputs[1]);
						
						for (Topic topic : subscriber.getSubbedTopics()) {
							if (topic.getID() == topicID) {
								subbedTopic = topic; 
							}
						}
						
						// Call unsubscribe method
						if (subbedTopic != null) {
							subscriber.unsubscribe(subbedTopic);
							
							// Relay this change to broker network
							broker.updateInfo(subscriber);
							// Relay updated topic info to broker network (update subList on Publisher class)
							broker.removeSub(subbedTopic, subscriber.getName());
							
							System.out.println("Successfully unsubscribed to topic "+topicID);
							
						} else {
							// Else, print error message
							System.out.println("Error: Unable to unsubscribe as subscriber is not subscribed to this topic.\n");
							break;
						}						
						
					} catch (IndexOutOfBoundsException | NumberFormatException e) {
						System.err.println("Error: Invalid arguments. Please try again.\n");
					}
					
					break;
				default:
					System.out.println("Invalid command. Please try again.\n");
				}
				
				subscriberWrapper.setSubscriber(subscriber);
				//System.out.println("Updated subscriber wrapper info: "+subscriberWrapper.getSubscriber());
				
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				    
				    try {
				    	// Get the subscriber reference from the wrapper
				        Subscriber subToRemove = subscriberWrapper.getSubscriber();
				    	
					    // Remove subscriber from broker subList
				        System.out.println("Subscriber program crashed. Removing "+subToRemove+" from the system...");
					    broker.deleteSubscriber(subToRemove);
					    
					    sc.close();
					    
				    } catch (Exception e) {
				    	System.err.println("Error trying to delete subscriber: "+e.getMessage());
				    }
				}));
				
			}
			
		} catch (Exception e) {
			System.err.println("Error connecting to broker: " + e.getMessage());
			// GRACEFULLY EXIT PROGRAM
			System.err.println("Exiting program.");
			System.exit(0);
		}

	}

}


