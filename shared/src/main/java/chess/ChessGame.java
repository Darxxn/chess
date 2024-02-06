package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard chessBoard;
    private TeamColor currentTurn;

    public ChessGame() {
        chessBoard = new ChessBoard();
        chessBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);

        Collection<ChessMove> possibleChessMoves = piece.pieceMoves(chessBoard, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : possibleChessMoves) {
            ChessPiece targetPiece = chessBoard.getPiece(move.getEndPosition());
            chessBoard.addPiece(move.getEndPosition(), piece);
            chessBoard.addPiece(move.getStartPosition(), null);

            if (!isInCheck(piece.getTeamColor())) {
                legalMoves.add(move);
            }

            chessBoard.addPiece(move.getStartPosition(), piece);
            chessBoard.addPiece(move.getEndPosition(), targetPiece);
        }
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {

        ChessPiece piece = chessBoard.getPiece(move.getStartPosition());

        if (piece != null && piece.getTeamColor() == currentTurn) {
            Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

            if (validMoves != null && validMoves.contains(move)) {

                if (move.getPromotionPiece() != null) {
                    ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
                    this.chessBoard.addPiece(move.getEndPosition(), promotedPiece);
                }

                else {
                    this.chessBoard.addPiece(move.getEndPosition(), piece);
                }
                this.chessBoard.addPiece(move.getStartPosition(), null);

                if (isInCheck(currentTurn)) {
                    this.chessBoard.addPiece(move.getEndPosition(), null);
                    this.chessBoard.addPiece(move.getStartPosition(), piece);
                    throw new InvalidMoveException("King is in check");
                }

                switchTurn();
            }
            else {
                throw new InvalidMoveException("Invalid move");
            }
        }
        else {
            throw new InvalidMoveException("Invalid move");
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = kingFinder(teamColor);

        if (kingPosition != null) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    ChessPosition newPosition = new ChessPosition(row + 1, col + 1);
                    ChessPiece opponentPiece = chessBoard.getPiece(newPosition);

                    if (opponentPiece != null && opponentPiece.getTeamColor() != teamColor) {
                        Collection<ChessMove> checkMoves = opponentPiece.pieceMoves(chessBoard, newPosition);

                        for (ChessMove move : checkMoves) {
                            if (move.getEndPosition().equals(kingPosition)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            Collection<ChessPosition> allPositions = getPiecePositions(teamColor);

            for (ChessPosition position : allPositions) {
                Collection<ChessMove> validMoves = validMoves(position);
                for (ChessMove move : validMoves) {
                    ChessBoard tempBoard = new ChessBoard();
                    ChessPiece piece = tempBoard.getPiece(move.getStartPosition());
                    tempBoard.addPiece(move.getEndPosition(), piece);
                    tempBoard.addPiece(move.getStartPosition(), null);
                    if (!isInCheck(teamColor)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        Collection<ChessPosition> allPositions = getPiecePositions(teamColor);

        for (ChessPosition position : allPositions) {
                Collection<ChessMove> validMoves = validMoves(position);
                if (!validMoves.isEmpty()) {
                    return false;
                }
            }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }

    private void switchTurn() {
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    private Collection<ChessPosition> getPiecePositions(TeamColor teamColor) {
        Collection<ChessPosition> allPositions = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row + 1, col + 1));
                if (piece != null && (piece.getTeamColor() == teamColor)) {
                    allPositions.add(new ChessPosition(row + 1, col + 1));
                }
            }
        }
        return allPositions;
    }

    private ChessPosition kingFinder(TeamColor teamColor){
        int row;
        int col;
        for (row = 0; row < 8; row++){
            for (col = 0; col <8; col++){
                ChessPiece allPieces = chessBoard.getPiece(new ChessPosition(row+1,col+1));
                if(allPieces !=null && allPieces.getPieceType() == ChessPiece.PieceType.KING && allPieces.getTeamColor() == teamColor){
                    return new ChessPosition(row+1,col+1);
                }
            }
        }
        return null;
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ChessGame chessGame = (ChessGame) object;
        return Objects.deepEquals(chessBoard, chessGame.chessBoard) && Objects.deepEquals(currentTurn, chessGame.currentTurn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chessBoard, currentTurn);
    }
}
