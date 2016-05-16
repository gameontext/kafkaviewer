package org.ozzy.demo;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/kafka")
public class KafkaSocket {
	
	//we don't actually use the kafka bean directly
	//but by having it injected, we know it's been created once
	//that means we've subscribed to the topics etc.
	@Inject
	Kafka kafka;

	@Inject
	SessionManager sessions;
	
	public void processRecord(@Observes GameOnEvent event) {
		System.out.println("Processing record.. ");
		JsonObjectBuilder message = Json.createObjectBuilder();
		message.add("offset", event.getOffset());
		message.add("topic", event.getTopic());

		message.add("timestamp",System.currentTimeMillis());
		message.add("key", String.valueOf(event.getKey()));
		message.add("value", String.valueOf(event.getValue()));

		String txt = message.build().toString();
		System.out.println("Sending: " + txt);
		sessions.sendMessageToSessions(txt);
		System.out.println(("Sent."));
	}

	public KafkaSocket() {
		System.out.println("Created kafkasocket "+this.hashCode());
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig ec) {	
		System.out.println("Session opened... ");
		sessions.addSession(session);
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		System.out.println("Closing session");
		sessions.removeSession(session);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		// noop
	}

	@OnError
	public void OnError(Session session, Throwable t) {
		System.out.println("ERROR (removing session): ");
		t.printStackTrace();
		sessions.removeSession(session);
	}

}
