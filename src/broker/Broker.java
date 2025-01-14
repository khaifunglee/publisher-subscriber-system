package broker;

import java.rmi.Naming;

/*
 * This class facilitates the running and connection between brokers, serving as a server and client side.
 * Khai Fung Lee, 1242579
 */

import remote.IRemoteBroker;

/*
 * This class handles the creation and connection between brokers
 */

public class Broker {
	
	public static void main(String[] args) {
		
		// Initialize variables
		int portNum = 0;
		BrokerServer broker = null;
		// Get command line arguments
		try {
			portNum = Integer.parseInt(args[0]);
			
		} catch (IndexOutOfBoundsException | NumberFormatException e) {
			System.err.println("Invalid command line arguments:\njava -jar broker.jar port -b [broker_ip_1:port1 broker_ip_2:port2] " + e.getMessage());
			// GRACEFULLY EXIT PROGRAM
			System.err.println("Exiting program.");
			System.exit(0);
		}
		
		// Server-side: Create a registry for the broker node
		try {
			java.rmi.registry.LocateRegistry.createRegistry(portNum);
			
			// Create new broker to keep track of broker list			
			broker = new BrokerServer();
			
			java.rmi.Naming.rebind("Broker" + Integer.toString(portNum), broker);
			
			System.out.println("Broker" + Integer.toString(portNum) + " bound in registry.");
			
		} catch (Exception e) {
			System.err.println("Unable to create broker registry: " + e.getMessage());
			// GRACEFULLY EXIT PROGRAM
			System.err.println("Exiting program.");
			System.exit(0);
		}
		
		// Client-side: for inter-broker connection (only runs if -b flag is shown)
		try {
			if (args[1].equals("-b")) {
				try {  
					// Connect to 2nd broker
					String broker2Info = args[2];
					String broker2IP = broker2Info.split(":")[0];
					String broker2Port = broker2Info.split(":")[1];
					
					System.out.println("Connecting Broker"+Integer.toString(portNum)+ " to Broker"+broker2Port);
				
					IRemoteBroker broker2 = (IRemoteBroker) Naming.lookup("rmi://"+broker2IP+"/Broker"+broker2Port);
					System.out.println(broker2.sayHello("Message from Broker"+broker2Port+": Connection established."));
					
					// Add broker2 to broker1's brokerList and vice-versa
					broker2.addBroker(broker);
					
				} catch (IndexOutOfBoundsException iobe) {
					System.err.println("Invalid command line arguments:\njava -jar broker.jar port -b [broker_ip_1:port1 broker_ip_2:port2] " + iobe.getMessage());
					// GRACEFULLY EXIT PROGRAM
					System.err.println("Exiting program.");
					System.exit(0);
					
				} catch (Exception e) {
					System.err.println("1st Error connecting to broker: " + e.getMessage());
					//e.printStackTrace();
					// GRACEFULLY EXIT PROGRAM
					System.err.println("Exiting program.");
					System.exit(0);
				}
				try {
					// Connect to 3rd broker (if necessary)
					String broker3Info = args[3];
					try {
						String broker3IP = broker3Info.split(":")[0];
						String broker3Port = broker3Info.split(":")[1];
						
						System.out.println("Connecting Broker"+Integer.toString(portNum)+ " to Broker"+broker3Port);
						
						IRemoteBroker broker3 = (IRemoteBroker) Naming.lookup("rmi://"+broker3IP+"/Broker"+broker3Port);
						System.out.println(broker3.sayHello("Message from Broker"+broker3Port+": Connection established."));
						
						// Add broker2 to broker1 and broker3's brokerList and vice-versa
						broker3.addBroker(broker);
						
					} catch (IndexOutOfBoundsException iobe) {
						System.err.println("Invalid command line arguments:\njava -jar broker.jar port -b [broker_ip_1:port1 broker_ip_2:port2] " + iobe.getMessage());
						// GRACEFULLY EXIT PROGRAM
						System.err.println("Exiting program.");
						System.exit(0);
						
					} catch (Exception e) {
						System.err.println("2nd Error connecting to broker: " + e.getMessage());
						// GRACEFULLY EXIT PROGRAM
						System.err.println("Exiting program.");
						System.exit(0);
					}
				} catch (IndexOutOfBoundsException iobe) {
					System.out.println("Second broker node registry created.");
					
				} catch (Exception e) {
					e.getMessage();
					// GRACEFULLY EXIT PROGRAM
					System.err.println("Exiting program.");
					System.exit(0);
				}
				
			} else {
				System.err.println("Invalid command line arguments:\njava -jar broker.jar port -b [broker_ip_1:port1 broker_ip_2:port2]");
				// GRACEFULLY EXIT PROGRAM
				System.err.println("Exiting program.");
				System.exit(0);
			}
		} catch (IndexOutOfBoundsException iobe) {
			// If -b flag, assume this is the first broker being connected
			System.out.println("First broker node registry created.");
			
		} catch (Exception e) {
			e.getMessage();
			// GRACEFULLY EXIT PROGRAM
			System.err.println("Exiting program.");
			System.exit(0);
		}
		System.out.println("hi");
		
	}

}
