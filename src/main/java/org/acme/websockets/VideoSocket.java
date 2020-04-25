package org.acme.websockets;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/video/{username}")
@ApplicationScoped
public class VideoSocket {

    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
         sessions.put(username, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
         sessions.remove(username);
    }

    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
         sessions.remove(username);
    }

    @OnMessage
    public void onMessage(ByteBuffer message, @PathParam("username") String username) {
        broadcast(message, username);
    }

    private void broadcast(ByteBuffer message, String username) {
        sessions.forEach((u,s)->{
            Date start = new Date();
            s.getAsyncRemote().sendObject(message, result ->  {
                Date finish = new Date();
                System.out.println("Total send time: " + (finish.getTime() - start.getTime() + " milliseconds."));
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });

    }
}
