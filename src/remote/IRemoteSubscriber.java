package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * This interface implements the methods required to setup a two-way connection between subscriber and broker for the subscriber console to be able to receive published messages and notifications.
 * Khai Fung Lee, 1242579
 */

public interface IRemoteSubscriber extends Remote {
	
	public void receiveMessage(String message) throws RemoteException;

	public IRemoteBroker getBrokerServer() throws RemoteException;

	public void setBrokerServer(IRemoteBroker broker) throws RemoteException;
	
	public String getName() throws RemoteException;
	
}
