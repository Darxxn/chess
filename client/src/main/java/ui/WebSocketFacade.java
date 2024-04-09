package ui;

import chess.ChessGame;
import dataAccess.DataAccessException;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import webSocketMessages.serverMessages.*;

import com.google.gson.*;
import webSocketMessages.userCommands.JoinObserverCommand;
import webSocketMessages.userCommands.JoinPlayerCommand;
import webSocketMessages.userCommands.LeaveGameCommand;

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

    public void joinPlayer(String authToken, Integer gameID, String username, ChessGame.TeamColor playerColor) {
        var joinPlayerCommand = new JoinPlayerCommand(authToken);
        joinPlayerCommand.setGameID(gameID);
        joinPlayerCommand.setUsername(username);
        joinPlayerCommand.setPlayerColor(playerColor);

        sendMessage(joinPlayerCommand);
    }

    public void joinObserver(String authToken, Integer gameID, String username) {
        var joinObserverCommand = new JoinObserverCommand(authToken);
        joinObserverCommand.setGameID(gameID);
        joinObserverCommand.setUsername(username);

        //Send the message
        sendMessage(joinObserverCommand);
    }

    public void leaveGame(String authToken, Integer gameID) {
        var leaveGameCommand = new LeaveGameCommand(authToken);
        leaveGameCommand.setGameID(gameID);
        sendMessage(leaveGameCommand);
    }

    private void sendMessage(Object message) {
        if (this.session != null && this.session.isOpen()) {
            try {
                this.session.getBasicRemote().sendText(new Gson().toJson(message));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } else {
            System.out.println("Unable to send message. Session is either null or closed.");
        }
    }

}
