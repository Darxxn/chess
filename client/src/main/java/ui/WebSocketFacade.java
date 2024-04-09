package ui;

import javax.websocket.*;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private GameHandler gameHandler;
    private MakeBoard game;

    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }

    @OnClose
    public void onClose(){
    }

    @OnError
    public void onError(){
    }

}
