package ui;

import dataAccess.DataAccessException;
import model.*;
import request.JoinGameRequest;
import result.ListGamesResponse;
import java.net.*;
import java.io.*;
import com.google.gson.Gson;

public class ServerFacade {
    private final String serverConnection;

    public ServerFacade(String url) {
        serverConnection = url;
    }

    public GameData createGame(String token, String gameName) throws DataAccessException {
        var path = "/game";
        var request = new GameData(0, null, null, gameName, null);
        return this.sendRequest("POST", path, request, GameData.class, token);
    }

    public ListGamesResponse listGames(String token) throws DataAccessException {
        var path = "/game";
        var method = "GET";
        return this.sendRequest(method, path, null, ListGamesResponse.class, token);
    }

    public GameData joinGame(String token, int gameID, String color) throws DataAccessException {
        var path = "/game";
        var request = new JoinGameRequest(color, gameID);
        return this.sendRequest("PUT", path, request, GameData.class, token);
    }

    public AuthData registerUser(String username, String password, String email) throws DataAccessException {
        var path = "/user";
        var request = new UserData(username, password, email);
        return this.sendRequest("POST", path, request, AuthData.class, null);
    }

    public void logout(String token) throws DataAccessException {
        var path = "/session";
        this.sendRequest("DELETE", path, null, null, token);
    }

    public AuthData login(String username, String password) throws  DataAccessException {
        var path = "/session";
        var request = new UserData(username, password, null);
        return this.sendRequest("POST", path, request, AuthData.class, null);
    }

    private <T> T sendRequest(String method, String path, Object request, Class<T> responseClass, String token) throws DataAccessException {
        try {
            URL url = (new URI(serverConnection + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (token != null) {
                http.setRequestProperty("Authorization", token);
            }

            writeBody(request, http);
            http.connect();
            notSuccessful(http);
            return readBody(http, responseClass);

        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void notSuccessful(HttpURLConnection http) throws IOException, DataAccessException {
        var status = http.getResponseCode();
        if (!success(status)) {
            throw new DataAccessException("Error\n");
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream body = http.getInputStream()) {
                InputStreamReader input = new InputStreamReader(body);
                if (responseClass != null) {
                    response = new Gson().fromJson(input, responseClass);
                }
            }
        }
        return response;
    }

    private boolean success(int status) {
        return status / 100 == 2;
    }

    public void clearData() {
        try {
            URL url = new URL(serverConnection + "/db");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("DELETE");
            http.connect();

            int responseCode = http.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Database cleared");
            } else {
                System.out.println("Failed to clear data: Server returned response code " + responseCode);
            }

            http.disconnect();
        } catch (Exception ex) {
            System.out.println("Failed to clear data: " + ex.getMessage());
        }
    }
}
