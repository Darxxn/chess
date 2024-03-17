package ui;

import dataAccess.DataAccessException;
import model.*;
import server.Server;
import java.net.*;
import java.io.*;
import com.google.gson.Gson;

public class ChessServer {
    private final String serverConnection;

    public ChessServer(String url) {
        serverConnection = url;
    }

    public AuthData registerUser(String username, String password, String email) throws DataAccessException {
        var path = "/user";
        var request = new UserData(username, password, email);
        return this.makeRequest("POST", path, request, AuthData.class, null);
    }

    public void logout(String token) throws DataAccessException {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null, token);
    }

    public AuthData login(String username, String password) throws  DataAccessException {
        var path = "/session";
        var request = new UserData(username, password, null);
        return this.makeRequest("POST", path, request, AuthData.class, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String token) throws DataAccessException {
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
            throwIfNotSuccessful(http);
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

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, DataAccessException {
        var status = http.getResponseCode();
        if (!success(status)) {
            throw new DataAccessException("failure: ");
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean success(int status) {
        return status / 100 == 2;
    }
}
