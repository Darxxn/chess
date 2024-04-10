package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;

import chess.ChessGame.TeamColor;
import dataAccess.*;
import model.*;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import webSocketMessages.serverMessages.*;
import webSocketMessages.userCommands.*;
import service.*;

import java.io.IOException;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {

    private WebSocketSessions sessions = new WebSocketSessions();;
    private WebSocketService service = new WebSocketService();

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("Connected");
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Closed: " + statusCode + " " + reason);
        sessions.removeSession(session);
        session.close();
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.out.println("Error: " + error.getMessage());
        var errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR);
        errorMessage.setErrorMessage("Error: " + error.getMessage());
        try {
            System.out.println(new Gson().toJson(errorMessage));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
        } catch (IOException exception) {
            System.out.println("Error sending error message: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws DataAccessException, IOException {
        System.out.println("Message: " + message);
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        Gson gson = new Gson();
        switch (command.getCommandType()) {
            case JOIN_PLAYER:
                JoinPlayerCommand joinPlayerCommand = gson.fromJson(message, JoinPlayerCommand.class);
                joinPlayer(joinPlayerCommand, session);
                break;
            case JOIN_OBSERVER:
                JoinObserverCommand joinObserverCommand = gson.fromJson(message, JoinObserverCommand.class);
                joinObserver(joinObserverCommand, session);
                break;
            case MAKE_MOVE:
                MoveCommand makeMoveCommand = gson.fromJson(message, MoveCommand.class);
                makeMove(makeMoveCommand, session);
                break;
            case LEAVE:
                LeaveGameCommand leaveGameCommand = gson.fromJson(message, LeaveGameCommand.class);
                leaveGame(leaveGameCommand, session);
                break;
            case RESIGN:
                ResignCommand resignCommand = gson.fromJson(message, ResignCommand.class);
                resignGame(resignCommand, session);
                break;
            default:
                throw new DataAccessException("Invalid command type");
        }
    }

    public void joinPlayer(JoinPlayerCommand command, Session session) {
        GameData gameData;
        try {
            gameData = service.getGameData(command.getGameID(), command.getAuthString());
        } catch (DataAccessException exception) {
            onError(session, exception);
            return;
        }

        if (command.getUsername() == null) {
            setUsernameFromAuthToken(command, session);
        }

        var game = gameData.game();
        var username = command.getUsername();
        var whiteUsername = gameData.whiteUsername();
        var blackUsername = gameData.blackUsername();

        if (game.getTeamTurn() == TeamColor.FINISHED) {
            onError(session, new DataAccessException("Game is finished"));
            return;
        }
        if(gameData.whiteUsername() == null && gameData.blackUsername() == null) {
            onError(session, new DataAccessException("Game has not been created."));
            return;
        }
        if ((command.getPlayerColor() == TeamColor.WHITE &&  !gameData.whiteUsername().equals(command.getUsername())) || (command.getPlayerColor() == TeamColor.BLACK && !Objects.equals(gameData.blackUsername(), command.getUsername()))){
            onError(session, new DataAccessException("Username already taken"));
            return;
        }

        sessions.addSessionToGame(command.getGameID(), command.getAuthString(), session);

        sendMessagesForJoinAndObserve(game, command.getGameID(), command.getAuthString(), session, username + " has joined the game as the " + command.getPlayerColor().toString() + " player", whiteUsername, blackUsername);
    }

    public void joinObserver(JoinObserverCommand command, Session session) {
        if (command.getUsername() == null) {
            setUsernameFromAuthToken(command, session);
        }

        GameData gameData;
        try {
            gameData = service.getGameData(command.getGameID(), command.getAuthString());
        } catch (DataAccessException exception) {
            onError(session, exception);
            return;
        }

        var game = gameData.game();
        var username = command.getUsername();
        var whiteUsername = gameData.whiteUsername();
        var blackUsername = gameData.blackUsername();

        if (game.getTeamTurn() == TeamColor.FINISHED) {
            onError(session, new DataAccessException("Game is finished"));
            return;
        }

        sessions.addSessionToGame(command.getGameID(), command.getAuthString(), session);

        sendMessagesForJoinAndObserve(game, command.getGameID(), command.getAuthString(), session, username + " has joined the game as an observer", whiteUsername, blackUsername);
    }

    public void makeMove(MoveCommand command, Session session) {
        GameData gameData;
        try {
            gameData = service.makeMove(command.getGameID(), command.getAuthToken(), command.getMove());
        } catch (DataAccessException exception) {
            onError(session, exception);
            return;
        }

        try {
            gameData = service.getGameData(command.getGameID(), command.getAuthToken());
        } catch (DataAccessException exception) {
            onError(session, exception);
            return;
        }

        var game = gameData.game();
        var move = command.getMove().toString();
        var whiteUsername = gameData.whiteUsername();
        var blackUsername = gameData.blackUsername();

        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        loadMessage.setWhiteUsername(whiteUsername);
        loadMessage.setBlackUsername(blackUsername);
        try {
            sendMessage(command.getGameID(), loadMessage, command.getAuthString(), session);
        } catch (DataAccessException | IOException exception) {
            onError(session, exception);
            return;
        }
        try {
            broadcastMessage(command.getGameID(), loadMessage, command.getAuthString());
        } catch (IOException exception) {
            onError(session, exception);
            return;
        }

        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(move);
        try {
            broadcastMessage(command.getGameID(), notificationMessage, command.getAuthString());
        } catch (IOException exception) {
            onError(session, exception);
            return;
        }
    }

    public void leaveGame(LeaveGameCommand command, Session session) {
        String username;
        try {
            username = service.leaveGame(command.getGameID(), command.getAuthString());
        } catch (DataAccessException exception) {
            onError(session, exception);
            return;
        }

        var gameID = command.getGameID();

        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has left the game");
        try {
            broadcastMessage(gameID, notificationMessage, command.getAuthString());
        } catch (IOException exception) {
            onError(session, exception);
            return;
        }

        sessions.removeSessionFromGame(command.getGameID(), command.getAuthString(), session);
    }

    public void resignGame(ResignCommand command, Session session) {
        String username;
        try {
            username = service.resignGame(command.getGameID(), command.getAuthString());
        } catch (DataAccessException exception) {
            onError(session, exception);
            return;
        }

        var gameID = command.getGameID();

        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has resigned the game.");
        try {
            broadcastMessage(gameID, notificationMessage, "");
        } catch (IOException exception) {
            onError(session, exception);
            return;
        }

        sessions.removeSessionFromGame(command.getGameID(), command.getAuthString(), session);
    }

    private void sendMessagesForJoinAndObserve(ChessGame game, Integer gameID, String authToken, Session session, String message, String whiteUsername, String blackUsername) {
        var loadMessage=new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        loadMessage.setWhiteUsername(whiteUsername);
        loadMessage.setBlackUsername(blackUsername);
        try {
            sendMessage(gameID, loadMessage, authToken, session);
        } catch (DataAccessException | IOException exception) {
            onError(session, exception);
            return;
        }

        var notificationMessage=new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(message);
        try {
            broadcastMessage(gameID, notificationMessage, authToken);
        } catch (IOException exception) {
            onError(session, exception);
            return;
        }
    }

    private <T extends UserGameCommand> void setUsernameFromAuthToken(T command, Session session) {
        try {
            if(command.getCommandType() == UserGameCommand.CommandType.JOIN_PLAYER)
                ((JoinPlayerCommand) command).setUsername(service.getUsernameFromAuthToken(command.getAuthString()));
            else if(command.getCommandType() == UserGameCommand.CommandType.JOIN_OBSERVER)
                ((JoinObserverCommand) command).setUsername(service.getUsernameFromAuthToken(command.getAuthString()));
        } catch (DataAccessException exception) {
            onError(session, exception);
        }
    }

    private void sendMessage(Integer gameID, ServerMessage message, String authToken, Session session)
            throws DataAccessException, IOException {
        if (session != null) {
            if (session.isOpen()) {
                String jsonMessage = new Gson().toJson(message);
                System.out.println("Sending message to session: " + session + ", message: " + jsonMessage);
                session.getRemote().sendString(jsonMessage);
                session.getRemote().flush();
            } else {
                System.out.println("Cannot send message. Session is closed.");
            }
        } else {
            System.out.println("Cannot send message. Session is null.");
        }
    }

    private void broadcastMessage(Integer gameID, ServerMessage message, String exceptThisAuthToken)
            throws IOException {
        System.out.println("Broadcasting message: " + message.toString());
        sessions.getSessionsForGame(gameID).forEach((authToken, session) -> {
            if (!Objects.equals(authToken, exceptThisAuthToken)) {
                try {
                    String jsonMessage = new Gson().toJson(message);
                    System.out.println("Broadcasting message: " + jsonMessage);
                    session.getRemote().sendString(jsonMessage);
                } catch (IOException exception) {
                    System.out.println("Error broadcasting message: " + exception.getMessage());
                    exception.printStackTrace();
                }
            }
        });
    }
}