package topic;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * This class handles topics created and used by publisher and subscriber functions.
 * Khai Fung Lee, 1242579
 */

public class Topic implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	// Instance variables
	private int ID;
	private String name;
	private String publisherName; // Name of publisher that created topic
	private ArrayList<String> subList; // List of subscribers following topic
	
	public Topic(int ID, String name, String publisherName) {
		this.ID = ID;
		this.name = name;
		this.publisherName = publisherName;
		this.subList = new ArrayList<>();
	}
	
	// Copy constructor
	public Topic(Topic other) {
		this.ID = other.ID;
		this.name = other.name;
		this.publisherName = other.publisherName;
		this.subList = new ArrayList<String>();
		
		for (String subName : other.getSubList()) {
			this.subList.add(subName);
		}
	}
	
	// Getter methods
	public int getID() {
		return this.ID;
	}
	
	public String getName() {
		return this.name;
	}
	public String getPublisher() {
		return this.publisherName;
	}
	public ArrayList<String> getSubList() {
		return new ArrayList<>(this.subList);
	}
	
	// Method to add subscriber to list when subscriber follows this topic
	public void addSub(String subName) {
		this.subList.add(subName);
	}
	// Method to remove subscriber from list when subscriber unsubs
	public void removeSub(String subName) {
		this.subList.remove(subName);
	}
	
	// toString method
	public String toString() {
		return "Topic ID: "+this.ID+", Topic Name: "+this.name+", created by "+this.publisherName;
	}
}
