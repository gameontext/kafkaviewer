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

/**
 * Endpoint that listens directly to the cdi events and relays them as json to
 * all connected websocket clients.
 */
@ServerEndpoint(value = "/kafkacdi")
public class KafkaCDISocket {

    @Inject
    SessionManager sessions;

    public void processRecord(@Observes GameOnEvent event) {
        System.out.println("Processing record.. ");
        JsonObjectBuilder message = Json.createObjectBuilder();
        message.add("offset", event.getOffset());
        message.add("topic", event.getTopic());

        message.add("timestamp", System.currentTimeMillis());
        message.add("key", String.valueOf(event.getKey()));
        message.add("value", String.valueOf(event.getValue()));

        String txt = message.build().toString();
        System.out.println("Sending: " + txt);
        sessions.sendMessageToSessions(txt);
        System.out.println(("Sent."));
    }

    public KafkaCDISocket() {
        System.out.println("Created kafkacdisocket " + this.hashCode());
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
