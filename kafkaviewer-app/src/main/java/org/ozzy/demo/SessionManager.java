package org.ozzy.demo;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.inject.Vetoed;
import javax.websocket.Session;

/**
 A simple bean that tracks websocket sessions,
 and allows broadcasting text to them.
*/
//do not let CDI build this bean directly, it
//must be created via the SessionManagerInjector
//Produces method.
@Vetoed
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

	public synchronized void sendMessageToSessions(String message){
		System.out.println("Asked to send message '"+message+"' to sessions.");
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
