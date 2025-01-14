package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import remote.*;

public class SubServer extends UnicastRemoteObject implements IRemoteSubscriber {
	
	// Instance variables that each subscriber server has
	private String username; // Username corresponding to subscriber (since multiple subs could connect to same broker)
	private IRemoteBroker brokerServer; // Corresponding broker network that the subscriber is connected to
	
	protected SubServer(String username) throws RemoteException {
		super();
		this.username = username;
	}
	
	private static final long serialVersionUID = 1L; 
	
	// This method is for broker connected to subscriber to remotely call this method to print publisher message on subscriber console
	@Override
	public synchronized void receiveMessage(String message) throws RemoteException {
		System.out.println(message);
	}
	
	// This method is to retrieve this SubServer's brokerServer that it is connected to
	@Override
	public synchronized IRemoteBroker getBrokerServer() throws RemoteException {
		return this.brokerServer;
	}

	@Override
	public synchronized void setBrokerServer(IRemoteBroker broker) throws RemoteException {
		// Set this subServer's brokerServer to allow subscriber console to receive messages
		this.brokerServer = broker;			
		
		System.out.println("This sub server is currently connected to remote broker: " + this.brokerServer);

	}

	@Override
	public synchronized String getName() throws RemoteException {
		// TODO Auto-generated method stub
		return this.username;
	}
	
}
