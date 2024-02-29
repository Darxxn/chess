package server;

import dataAccess.DataAccessException;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.*;
import spark.*;
import com.google.gson.Gson;
import service.*;

public class Server {
    private final UserService userService = new UserService();
    private final DataService authService = new DataService();
    private final GameService gameService = new GameService();

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.post("/game", this::createGame);
        Spark.delete("/session", this::logout);
        Spark.delete("/db", this::clearApplication);
        Spark.get("/game", this::listGames);
        Spark.put("/game",this::joinGame);
        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object registerUser(Request req, Response res) {
        try {
            RegisterRequest user = new Gson().fromJson(req.body(), RegisterRequest.class);
            String username = userService.add(user);
            String authToken = authService.add(user);
            RegisterResponse response = new RegisterResponse(username, authToken);
            res.status(200);
            return new Gson().toJson(response, RegisterResponse.class);
        }
        catch(DataAccessException exception) {
            return ErrorMethod(exception, res);
        }
    }

    private Object loginUser(Request req, Response res) {
        try {
            LoginRequest login = new Gson().fromJson(req.body(), LoginRequest.class);
            String username = userService.login(login);
            String authToken = authService.login(login);
            LoginResponse response = new LoginResponse(username, authToken);
            res.status(200);
            return new Gson().toJson(response, LoginResponse.class);
        }
        catch(DataAccessException exception) {
            return ErrorMethod(exception, res);
        }
    }

    private Object createGame(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            CreateGameRequest newGame = new Gson().fromJson(req.body(), CreateGameRequest.class);
            authService.verify(authToken);
            CreateGameResponse newResponse = gameService.createGame(newGame);
            res.status(200);
            return new Gson().toJson( newResponse, CreateGameResponse.class);
        }
        catch(DataAccessException exception) {
            return ErrorMethod(exception, res);
        }
    }

    private Object logout(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            authService.logout(authToken);
            res.status(200);
            return "{}";
        }
        catch(DataAccessException exception) {
            return ErrorMethod(exception, res);
        }
    }

    private Object listGames(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            authService.verify(authToken);
            ListGamesResponse games = gameService.listGames();
            res.status(200);
            return new Gson().toJson(games, ListGamesResponse.class);
        }
        catch(DataAccessException exception) {
            return ErrorMethod(exception, res);
        }
    }

    private Object joinGame(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            JoinGameRequest joinGame = new Gson().fromJson(req.body(), JoinGameRequest.class);
            String username = authService.getUsername(authToken);
            gameService.joinGame(joinGame, username);
            res.status(200);
            return "{}";
        }
        catch(DataAccessException exception) {
            return ErrorMethod(exception, res);
        }
    }

    private int getStatus(String message) {
        return switch (message) {
            case "Error: bad request" -> 400;
            case "Error: unauthorized" -> 401;
            case "Error: already taken" -> 403;
            default -> 500;
        };
    }

    private Object clearApplication(Request req, Response res) {
        userService.clear();
        authService.clear();
        gameService.clear();
        res.status(200);
        return "{}";
    }

    private String ErrorMethod(DataAccessException exception, Response res) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());
        res.status(getStatus(exception.getMessage()));
        return new Gson().toJson(errorResponse, ErrorResponse.class);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
