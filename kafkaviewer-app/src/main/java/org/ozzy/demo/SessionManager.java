/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.ozzy.demo;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.inject.Vetoed;
import javax.websocket.Session;

/**
 * A simple bean that tracks websocket sessions, and allows broadcasting text to
 * them.
 */
// do not let CDI build this bean directly, it
// must be created via the SessionManagerInjector
// Produces method.
@Vetoed
public class SessionManager {

    private final Set<Session> sessions = new CopyOnWriteArraySet<Session>();

    public SessionManager() {
    }

    public void addSession(Session session) {
        sessions.add(session);
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }

    public synchronized void sendMessageToSessions(String message) {
        System.out.println("Asked to send message '" + message + "' to sessions.");
        sessions.forEach(s -> {
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
