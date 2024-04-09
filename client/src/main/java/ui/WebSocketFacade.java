package ui;

import dataAccess.DataAccessException;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import webSocketMessages.serverMessages.*;

import com.google.gson.*;

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

    public WebSocketFacade(String url, MakeBoard game) throws DataAccessException {
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
            throw new DataAccessException("Failed to connect to the server");
        }
    }

    public void returnedMessage(String message) {
        // Convert Json into JsonObject
        var gson = new Gson();
        var jsonEle = gson.fromJson(message, JsonElement.class);
        var jsonObj = jsonEle.getAsJsonObject();

        var messageType = jsonObj.get("serverMessageType").getAsString();

        switch (messageType) {
            case "LOAD_GAME":
                var loadGameMessage = gson.fromJson(jsonObj, LoadGameMessage.class);
                game.updateGame(loadGameMessage.getGame(), loadGameMessage.getWhiteUsername(), loadGameMessage.getBlackUsername());
                break;
            case "NOTIFICATION":
                var notificationMessage = gson.fromJson(jsonObj, NotificationMessage.class);
                game.printMessage(notificationMessage.getMessage());
                break;
            case "ERROR":
                var errorMessage = gson.fromJson(jsonObj, ErrorMessage.class);
                game.printMessage(errorMessage.getErrorMessage());
                break;
            default:
                System.out.println("Unknown message type: " + messageType);
                break;
        }
    }

}
