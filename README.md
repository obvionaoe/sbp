# sbp
**sbp** is a simple gRPC message subscription service made in Kotlin

## Setup

To install the scripts run `./gradlew installDist`.

## Running

### Broker

To start the broker server just run `./build/install/tp1/bin/broker-server`.\
This will create the SQLite database and start the gRPC services.

### Publisher

To start the publisher client you will need to run\
`./build/install/tp1/bin/publisher-client <lambda> <tag>`\
where `<lambda>` is a number of events to trigger randomly in the space of an hour\
and `<tag>` is the type of message this publisher will be sending.

### Subscriber

To start the subscriber client you will need to run\
`./build/install/tp1/bin/subscriber-client <tag>`\
where `<tag>` is the type of message you want to subscribe to.