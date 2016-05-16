package org.ozzy.demo;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;

@ApplicationScoped
public class SessionManager {

	private final Set<Session> sessions = new CopyOnWriteArraySet<Session>();
	
	public SessionManager(){	
	}
	
	public void addSession(Session session){
		sessions.add(session);
	}
	public void removeSession(Session session){
		sessions.remove(session);
	}
	
	public void sendMessageToSessions(String message){
		sessions.forEach( s -> {
			try {
				if (s.isOpen()) {
					s.getBasicRemote().sendText(message);
				}
			} catch (IOException e) {
				System.out.println("Error during send..");
				e.printStackTrace();
			}
		});
	}
}
