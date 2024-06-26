package webSocketMessages.userCommands;

import chess.ChessMove;

public class MoveCommand extends UserGameCommand {

    private ChessMove move;
    private Integer gameID;

    public MoveCommand(String authToken) {
        super(authToken);
        this.commandType = CommandType.MAKE_MOVE;
    }

    public ChessMove getMove() {
        return move;
    }

    public void setMove(ChessMove move) {
        this.move = move;
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public String getAuthToken() {
        return getAuthString();
    }
}