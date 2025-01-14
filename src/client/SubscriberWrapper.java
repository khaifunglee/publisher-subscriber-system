package client;

/*
 * This wrapper class is used to hold a reference to an instance of a subscriber (for referencing subscriber in program crash code)
 * Khai Fung Lee, 1242579
 */

public class SubscriberWrapper {
    private Subscriber subscriber;

    public SubscriberWrapper(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public Subscriber getSubscriber() {
        return new Subscriber(subscriber);
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = new Subscriber(subscriber);
    }
}
