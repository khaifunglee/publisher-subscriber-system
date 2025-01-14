package client;

import java.io.Serializable;

/*
 * This class is an abstract class that extends to Publisher and Subscriber class.
 * Khai Fung Lee 1242579
 */

public abstract class Client implements Serializable {
	
	private static final long serialVersionUID = 1L;
	protected String username;
	
	public Client() {
		this.username = "Default";
	}
	public Client(String username) {
		this.username = username;
	}
	
	public String getName() {
		return this.username;
	}
	// Abstract methods to be implemented by Publisher & Subscriber
	public abstract void showMenu();
	
	public abstract String toString();

}
