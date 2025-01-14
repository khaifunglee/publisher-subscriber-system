package client;

/*
 * This wrapper class is used to hold a reference to an instance of a publisher (for referencing publisher in program crash code)
 * Khai Fung Lee, 1242579
 */

public class PublisherWrapper {
    private Publisher publisher;

    public PublisherWrapper(Publisher publisher) {
        this.publisher = publisher;
    }

    public Publisher getPublisher() {
        return new Publisher(publisher);
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = new Publisher(publisher);
    }
}
