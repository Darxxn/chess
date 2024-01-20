package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ChessPiece that = (ChessPiece) object;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        switch(type) {
            case KING:
                possibleMoves.addAll(kingMoves(board, myPosition));
                break;
            case QUEEN:
//                possibleMoves.addAll(queenMoves(board, myPosition));
//                break;
            case BISHOP:
                possibleMoves.addAll(bishopMoves(board, myPosition));
                break;
            case KNIGHT:
            case ROOK:
            case PAWN:
            case null:
        }
        return possibleMoves;
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> kingMove = new ArrayList<>();
        for (int row = -1; row <= 1; row++) {
            for (int col = -1; col <= 1; col++) {
                if (row == 0 && col == 0) {
                    continue;
                }

                int newRow = myPosition.getRow() + row;
                int newCol = myPosition.getColumn() + col;

                if (isValid(newRow, newCol)) {
                    ChessPosition newPosition = new ChessPosition(newRow + 1, newCol + 1);
                    ChessMove move = new ChessMove(myPosition, newPosition, null);
                    kingMove.add(move);
                }
            }
        }
        return kingMove;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> bishopMove = new ArrayList<>();
        for (int row = -1; row <= 1; row += 2) {
            for (int col = -1; col <= 1; col += 2) {
                for (int travel = 1; travel <= 7; travel++) {
                    int newRow = myPosition.getRow() + row * travel;
                    int newCol = myPosition.getColumn() + col * travel;

                    if (isValid(newRow, newCol)) {
                        ChessPosition newPosition = new ChessPosition(newRow + 1, newCol + 1);
                        ChessMove move = new ChessMove(myPosition, newPosition, null);
                        bishopMove.add(move);
                    }
                }
            }
        }
        return bishopMove;
    }

//    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
//        Collection<ChessMove> queenMove = new ArrayList<>();
//    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
