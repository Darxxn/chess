package ui;

import exception.ResponseException;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

    public WebSocketFacade(String url, MakeBoard game) throws ResponseException {
        try {
            this.game = game;
            url = url.replace("http://", "ws://") + "/connect";
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, new URI(url));
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    returnedMessage(message);
                }
            });
        }
        catch (DeploymentException | IOException | URISyntaxException exception) {
            throw new ResponseException(500, "Failed to connect to the server");
        }
    }

    public void returnedMessage(String message) {

    }

}
