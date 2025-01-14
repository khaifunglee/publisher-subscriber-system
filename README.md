# publisher-subscriber-system
**Description:** This repository is to practice my implementation of java RMI and concurrency by constructing a publisher-subscriber architectural system with a broker network. 

**Features:**
  - Multi-threaded server to handle multiple clients concurrently with real-time message distribution
  - Java RMI to handle connections between client and server
  - Fault tolerance if publishers/subscribers crash
  - Publisher functions: CREATE topic, PUBLISH message, SHOW subscriber count, DELETE topic
  - Subscriber functions: LIST topics, SUBSCRIBE to topic, SHOW subscriptions, UNSUBSCRIBE from topic
  - Console I/O for publisher & subscriber interactions

**Technologies Used:** Java, Java RMI, multithreading, concurrency, etc.

**Prerequisites**: Ensure you have JDK 8 or higher installed.

**Setup Instructions:**
  - Building the project: open a terminal and navigate to the project directory. Then, compile the java source files located in the `src` directory: `javac -d bin src/*.java`
  - Running the project:
    1. Start the broker network by running `java -cp bin Broker [port_number] -b [broker_ip_1:port1 broker_ip_2:port2]` on three separate terminal tabs. The `-b` flag indicates other broker pairs, hence the first broker being run doesn't need this flag, only the second and third brokers should include the flag with the other broker IP/port pairs when once they are up and running.
    2. Start the publisher(s) on separate terminal tabs with `java -cp bin Publisher [username] [broker_ip] [broker_port]`.
    3. Start the subscriber(s) on separate terminal tabs with `java -cp bin Subscriber [username] [broker_ip] [broker_port]`.

