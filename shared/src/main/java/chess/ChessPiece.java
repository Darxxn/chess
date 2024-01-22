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
                possibleMoves.addAll(queenMoves(board, myPosition));
                break;
            case BISHOP:
                possibleMoves.addAll(bishopMoves(board, myPosition));
                break;
            case KNIGHT:
            case ROOK:
                possibleMoves.addAll(rookMoves(board, myPosition));
                break;
            case PAWN:
//                possibleMoves.addAll(pawnMoves(board, myPosition, getTeamColor()));
//                break;
            case null:
                break;
        }
        return possibleMoves;
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> kingMove = new ArrayList<>();

        // move around in a 3x3 grid with the King as the center
        for (int row = -1; row <= 1; row++) {
            for (int col = -1; col <= 1; col++) {

                // skip the current position
                if (row == 0 && col == 0) {
                    continue;
                }

                // find the new position based on the offset
                int newRow = myPosition.getRow() + row;
                int newCol = myPosition.getColumn() + col;

                // see if it's still on the board
                if (isValid(board, newRow + 1, newCol + 1)) {
                    ChessPosition newPosition = new ChessPosition(newRow + 1, newCol + 1);
                    ChessMove move = new ChessMove(myPosition, newPosition, null);
                    kingMove.add(move);
                }
            }
        }
        return kingMove;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> queenMove = new ArrayList<>();

        queenMove.addAll(kingMoves(board, myPosition));
        queenMove.addAll(bishopMoves(board, myPosition));
        queenMove.addAll(rookMoves(board, myPosition));

        return queenMove;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> bishopMove = new ArrayList<>();
        for (int row = -1; row <= 1; row += 2) {
            for (int col = -1; col <= 1; col += 2) {
                for (int travel = 1; travel <= 7; travel++) {

                    int newRow = myPosition.getRow() + row * travel;
                    int newCol = myPosition.getColumn() + col * travel;

                    if(!isValid(board, newRow + 1, newCol + 1)) {
                        break;
                    }

                    if (isValid(board, newRow + 1, newCol + 1)) {
                        ChessPiece chessP = board.getPiece(new ChessPosition(newRow + 1, newCol + 1));
                        ChessPosition newPosition = new ChessPosition(newRow + 1, newCol + 1);
                        ChessMove move = new ChessMove(myPosition, newPosition, null);
                        bishopMove.add(move);
                        if (chessP != null) {
                            break;
                        }
                    }
                }
            }
        }
        return bishopMove;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> rookMove = new ArrayList<>();
        for (int row = -1; row <= 1; row++) {
            for (int col = -1; col <= 1; col++) {
                if (row == 0 && col == 0) {
                    continue;
                }

                if (row != 0 && col != 0) {
                    continue;
                }

                int newRow = myPosition.getRow() + row;
                int newCol = myPosition.getColumn() + col;

                while(isValid(board, newRow + 1, newCol + 1)) {
                    ChessPosition newPosition = new ChessPosition(newRow + 1, newCol + 1);
                    ChessMove move = new ChessMove(myPosition, newPosition, null);
                    rookMove.add(move);

                    newRow += row;
                    newCol += col;
                }
            }
        }
        return rookMove;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor teamColor) {
        Collection<ChessMove> pawnMove = new ArrayList<>();

        int movingForward = (teamColor == ChessGame.TeamColor.WHITE) ? -1 : 1;

        int newRow = myPosition.getRow() + movingForward;
        int newCol = myPosition.getColumn();

        if (isValid(board, newRow, newCol) && board.getPiece(new ChessPosition(newRow, newCol)) == null) {
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            ChessMove move = new ChessMove(myPosition, newPosition, null);
            pawnMove.add(move);

            if (startingPosPawn(myPosition, teamColor)) {
                newRow += movingForward;
                if (isValid(board, newRow, newCol) && board.getPiece(new ChessPosition(newRow, newCol)) == null) {
                    newPosition = new ChessPosition(newRow, newCol);
                    move = new ChessMove(myPosition, newPosition, null);
                    pawnMove.add(move);
                }
            }
        }

        int[] captureCols = {myPosition.getColumn() - 1, myPosition.getColumn() + 1};
        for (int captureCol : captureCols) {
            newRow = myPosition.getRow() + movingForward;
            newCol = captureCol;

            if (isValid(board, newRow, newCol) && enemyPiece(board, myPosition, new ChessPosition(newRow, newCol))) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessMove move = new ChessMove(myPosition, newPosition, null);
                pawnMove.add(move);
            }
        }
        return pawnMove;
    }

    private boolean isValid(ChessBoard board, int row, int col) {
        if (row > 8 || row < 1 || col > 8 || col < 1){
            return false;
        }
        ChessPiece square = board.getPiece(new ChessPosition(row, col));
        return square == null || square.getTeamColor() != this.pieceColor;
    }

    private boolean isPathBlocked(ChessBoard board, ChessPosition startPos, ChessPosition endPos) {
        int rowChange = endPos.getRow() - startPos.getRow();
        int colChange = endPos.getColumn() -startPos.getColumn();

        int row = Integer.compare(rowChange, 0);
        int col = Integer.compare(colChange, 0);

        int checkRow = startPos.getRow() + row;
        int checkCol = startPos.getColumn() + col;

        while (checkRow != endPos.getRow() || checkCol != endPos.getColumn()) {
            if (board.getPiece(new ChessPosition(checkRow, checkCol)) != null) {
                return true;
            }

            checkRow += row;
            checkCol += col;
        }
        return false;
    }

    private boolean startingPosPawn(ChessPosition myPosition, ChessGame.TeamColor teamColor) {
        return (teamColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 6) ||
                (teamColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 1);
    }

    private boolean enemyPiece(ChessBoard board, ChessPosition myPosition, ChessPosition newPosition) {
        ChessPiece thisPiece = board.getPiece(myPosition);
        ChessPiece newPiece = board.getPiece(newPosition);
        return newPiece != null && newPiece.getTeamColor() != thisPiece.getTeamColor();
    }
}