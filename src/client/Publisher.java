package client;

import java.io.Serializable;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import remote.IRemoteBroker;
import topic.Topic;

/*
 * This class handles publisher creation, updating, and implements publisher functions that are remotely called from brokers.
 * Khai Fung Lee, 1242579
 */

public class Publisher extends Client implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Topic> topicList; // list of topics publisher has created
	
	public Publisher(String username) {
		super(username);
		this.topicList = new ArrayList<Topic>();
	}
	// Copy constructor
	public Publisher(Publisher other) {
		this.username = other.username;
		// For array lists, need to create a new one and manually add to copy the lists
		this.topicList = new ArrayList<Topic>();
		
		for (Topic topic : other.topicList) {
			this.topicList.add(new Topic(topic));
		}
		
	}
	
	// Getter methods
	public ArrayList<Topic> getTopics() {
		return new ArrayList<>(this.topicList); 
	}
	
	public String toString() {
		return "Publisher: "+this.username+", Topics: "+this.topicList;
	}
	
	// Add topic to publisher's list
	public void addTopic(Topic newTopic) {
		this.topicList.add(newTopic);
	}
	
	// Show subscriber count of each topic that publisher has created
	private void showSubCount() {
		// For each topic in pub's topicList, show sub count
		for (Topic topic : this.topicList) {
			
			// Get number of subscribers from each topic's size of subList
			int subCount = topic.getSubList().size();
			System.out.println("TopicID: "+topic.getID()+ ", Topic Name: " + topic.getName() + ", Subscriber Count: " + subCount);
		}
		
	}
	
	// Remove specified topic (must also unsubscribe all subscribers)
	public void removeTopic(int topicID) {
		boolean topicNotFound = true;
		
		for (Topic topic : this.topicList) {
			if (topicID == topic.getID()) {
				topicNotFound = false;
				
				System.out.println("Removed topicID "+topicID);
				this.topicList.remove(topic);
				return;
			} 
		}
		if (topicNotFound) {
			System.out.println("Error: Topic does not exist. Please try again.\n");
			return;
		}
		
	}
	
	// Publisher's Main menu
	@Override
	public void showMenu() {
		System.out.println("1. create {topic_id} {topic_name} # Create a new topic");
		System.out.println("2. publish {topic_id} {message} # Publish a message to an existing topic");
		System.out.println("3. show # Show your current subscriber count for each topic");
		System.out.println("4. delete {topic_id} # Delete a topic");
		System.out.println("Please select command (create, publish, show, delete): ");	
	}
	
	public static void main(String[] args) {
		
		// Command line arguments: args[0] = username, [1] = broker_ip, [2] = broker_port
		String username = "";
		String brokerIp = null;
		String brokerPort = null;
		try {
			username = args[0];
			brokerIp = args[1];
			brokerPort = args[2];
		} catch (IndexOutOfBoundsException iobe) {
			System.err.println("Invalid command line arguments:\njava -jar publisher.jar username broker_ip broker_port");
			// GRACEFULLY EXIT PROGRAM
			System.err.println("Exiting program.");
			System.exit(0);
		}
		
		try {
			// Create new publisher and connect to broker specified in arguments
			Publisher publisher = new Publisher(username);
			PublisherWrapper publisherWrapper = new PublisherWrapper(publisher);
			
			//System.out.println("This publisher "+publisher+" is the wrapper class.");
			
			Scanner sc = new Scanner(System.in);
			
			IRemoteBroker broker = (IRemoteBroker) Naming.lookup("rmi://"+brokerIp+"/Broker"+brokerPort);
			System.out.println(broker.sayHello("Message from Broker"+brokerPort+": Connection established with "+publisher.getName()));
			
			// Add publisher info to connected broker to share across broker network
			broker.updateInfo(publisher);
			//System.out.println("Adding "+publisher+" to broker.");
			
			// While console is running, print console command menu
			System.out.println();
			
			// This menu should be running continuously until publisher crashes			
			while (true) {	
				publisher.showMenu();
				String fullInput = sc.nextLine();
				String[] inputs = fullInput.split(" ");
				String command = inputs[0];
				
				// Switch chase for different commands
				switch(command.toLowerCase()) {
				case "create":
					// Create a new topic
					try {
						// Get updated version of publisher from broker
						publisher = broker.getPublisher(publisher);
						
						// Get command line parameters
						int topicID = Integer.parseInt(inputs[1]);
						String topicName = inputs[2];
						
						//System.out.println(broker.sayHello("Creating topic with "+topicID+" and "+topicName));
						broker.createTopic(topicID, topicName, publisher);
						
						System.out.println("Successfully created topic "+topicName+" with ID "+topicID+"\n");	
						// Make sure publisher is updated version
						publisher = broker.getPublisher(publisher);
						
					} catch (IndexOutOfBoundsException | NumberFormatException e) {
						System.err.println("Error: Invalid arguments. Please try again.\n");
					} 
					
					break;
					
				case "publish":
					// Publish message to an existing topic
					// Conditions: sent to all subscribers, message limited to 100 char, topicID should be what they created
					try {
						// Get updated version of publisher from broker
						publisher = broker.getPublisher(publisher);
						
						// Get command line parameters
						int topicID = Integer.parseInt(inputs[1]);
						String message = String.join(" ", Arrays.copyOfRange(inputs, 2, inputs.length));
						
						boolean topicNotFound = true;
						
						// Check for conditions
						if (message.length() > 100) {
							System.out.println("Error: Message is too long. Please limit your message to 100 characters.\n");
						} else {
							for (Topic topic : publisher.getTopics()) {
								// Find topic to publish message to
								if (topic.getID() == topicID) {
									topicNotFound = false;
									
									// Construct full message to send
									DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");
									String currentTime = LocalDateTime.now().format(formatter);
									
									String fullMsg = currentTime + " "+topic.getID()+":"+topic.getName()+ " "+message;
									
									// Call a method to return a list of intended receivers 
									ArrayList<String> receiverList = broker.getSubscribers(topicID);
									
									if (receiverList.size() != 0) {
										broker.sendPubMessage(fullMsg, topicID, receiverList);
										System.out.println("Published message: " + fullMsg);
									} else {
										System.out.println("Error: No subscribers to send this message to.\n");
									}
								}
							}
							
							if (topicNotFound) {
								System.out.println("Error: Publisher did not create this topic. Please try again with a valid topic ID.\n");
								break;
							}
						}
						
					} catch (IndexOutOfBoundsException | NumberFormatException e) {
						System.err.println("Error: Invalid arguments. Please try again.\n");
					}
					
					break;
					
				case "show":
					// Get updated version of publisher from broker
					publisher = broker.getPublisher(publisher);
					
					// Show current subscriber count for each topic (if topicList aren't empty)
					if (publisher.getTopics().size() == 0) {
						System.out.println("Error: Publisher does not have any topics created.\n");
					} else {
						System.out.println("\nGetting sub count from: "+publisher);
						publisher.showSubCount();
						
						System.out.println();
					}
					
					break;
					
				case "delete":
					// Delete a topic
					// Get updated version of publisher from broker
					publisher = broker.getPublisher(publisher);
					try {
						// Get command line parameters
						int topicID = Integer.parseInt(inputs[1]);
						
						// Call a method to return a list of intended receivers (to get the notification)
						ArrayList<String> receiverList = broker.getSubscribers(topicID);
						
						publisher.removeTopic(topicID);
						
						// Relay updated publisher info to broker network
						broker.updateInfo(publisher);
						// Relay updated info to broker network (to update subbedTopics list of subscribers)
						broker.removeTopic(topicID, receiverList);
						
						System.out.println("Successfully removed topic "+topicID);
						
						// Make sure publisher is updated version
						publisher = broker.getPublisher(publisher);
						
					} catch (IndexOutOfBoundsException | NumberFormatException e) {
						System.err.println("Error: Invalid arguments. Please try again.\n");
					}
					
					break;
					
				default:
					System.out.println("Invalid command. Please try again.\n");
				}
				
				publisherWrapper.setPublisher(publisher);
				//System.out.println("Updated publisher wrapper info: "+publisherWrapper.getPublisher());
				
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				    
				    try {
				    	// Get the publisher reference from the wrapper
				        Publisher pubToRemove = publisherWrapper.getPublisher();
				    	
					    // Remove publisher from broker subList
				        System.out.println("Publisher program crashed. Removing "+pubToRemove+" from the system...");
					    broker.deletePublisher(pubToRemove);
					    
					    sc.close();
					    
				    } catch (Exception e) {
				    	System.err.println("Error trying to delete publisher: "+e.getMessage());
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
