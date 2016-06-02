# Game On! Kafka Viewer

A Simple WebApp to monitor a topic in kafka/message hub, and use a websocket
to relay the messages to a browser.


To run the viewer, build the project with 

 gradle build

and then build the container with 

 docker-compose build 

and then run it as part of the game-on set of containers by passing kafkaviewers docker-compose file as an additional argument. 

 docker-compose -f /path/to/gameon/docker-compose.yml -f /path/to/kafkaviewer/docker-compose-kafkaviewer.yml up

The Kafka container used by Game-On must be running before launching kafkaviewer else docker-compose will complain.

This viewer can also be run against MessageHub instead of local Kafka, just define the env vars

 - KAFKA_URL
 - KAFKA_USER
 - KAFKA_PASSWORD

to contain the appropriate messagehub content, and remove the links section from the kafkaviewer docker compose yml

When running you can view the web interface at http://{docker ip}:7080/KafkaViewer/


## Contributing

Want to help! Pile On!

[Contributing to Game On!](https://github.com/gameontext/gameon/blob/master/CONTRIBUTING.md)
