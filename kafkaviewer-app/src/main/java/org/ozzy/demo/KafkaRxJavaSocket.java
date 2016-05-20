package org.ozzy.demo;

import javax.enterprise.event.Observes;
import javax.annotation.PostConstruct;
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

import rx.Subscription;
import java.io.IOException;


@ServerEndpoint(value = "/kafkarxjava")
public class KafkaRxJavaSocket {

	@Inject
	KafkaRxJavaObservable kafka;

	private volatile Session session=null;
	private Subscription subscription=null;

	public KafkaRxJavaSocket() {
	}

	private String toJson(GameOnEvent event){
		JsonObjectBuilder message = Json.createObjectBuilder();
		message.add("offset", event.getOffset());
		message.add("topic", event.getTopic());

		message.add("timestamp",System.currentTimeMillis());
		message.add("key", String.valueOf(event.getKey()));
		message.add("value", String.valueOf(event.getValue()));
		String result = message.build().toString();
		System.out.println("Rx Endpoint ["+this.hashCode()+"] saw "+result);
		return result;
	}

	private void sendToSession(String text){
		try {
			if (session!=null && session.isOpen()) {
				session.getBasicRemote().sendText(text);
			}
		} catch (IOException e) {
			System.out.println("Error during send..");
			e.printStackTrace();
		}
	}

	@PostConstruct
	public void init(){
		System.out.println("Rx Endpoint ["+this.hashCode()+"] Initializing rxjava based websocket.");
		subscription = kafka.consume()
			.filter(gameOnEvent -> gameOnEvent.getKey().equals("coffee"))
			.map(gameOnEvent -> toJson(gameOnEvent))
			.subscribe(jsonMessage -> sendToSession(jsonMessage));
		System.out.println("Rx Endpoint ["+this.hashCode()+"] RxJava observable init complete.");
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig ec) {
		System.out.println("Rx Endpoint ["+this.hashCode()+"] Session opened... ");
		this.session = session;
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		System.out.println("Rx Endpoint ["+this.hashCode()+"] Closing session");
		this.session = null;
		System.out.println("Rx Endpoint ["+this.hashCode()+"] Unsubscribing Rxjava observable");
		Subscription subscription = this.subscription;
		this.subscription = null;
		subscription.unsubscribe();
		System.out.println("Rx Endpoint ["+this.hashCode()+"] Observable unsubscribed.");
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		// noop
	}

	@OnError
	public void OnError(Session session, Throwable t) {
		System.out.println("ERROR (removing session): ");
		t.printStackTrace();
		this.session = null;
		if(this.subscription!=null){
			Subscription subscription = this.subscription;
			this.subscription = null;
			subscription.unsubscribe();
		}
	}

}
