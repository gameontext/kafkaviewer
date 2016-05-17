# Game On! Kafka Viewer

A Simple WebApp to monitor a topic in kafka/message hub, and use a websocket
to relay the messages to a browser.

To run the viewer, build the project with gradle,
and then docker-compose build kafkaviewer and then docker-compose up kafkaviewer

The Kafka container used by Game-On must be running before launching kafkaviewer else docker-compose will complain.

This viewer can also be run against MessageHub instead of local Kafka, just define the
 - KAFKA_URL
 - KAFKA_USER
 - KAFKA_PASSWORD
 environment vars within the docker-compose.yml, and _remove_ the net section. (If you fail to do this,
   kafkaviewer will still function, but you'll need Kafka running locally even though you never use it.)

 When running you can view the web interface at http://{docker ip}:7080/KafkaViewer/


## Contributing

Want to help! Pile On!

[Contributing to Game On!](https://github.com/gameontext/gameon/blob/master/CONTRIBUTING.md)
