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
                possibleMoves.addAll(knightMoves(board, myPosition));
                break;
            case ROOK:
                possibleMoves.addAll(rookMoves(board, myPosition));
                break;
            case PAWN:
                possibleMoves.addAll(pawnMoves(board, myPosition));
                break;
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

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> knightMove = new ArrayList<>();
        int [] possibleRows = {-2, -1, 1, 2, 2, 1, -1, -2};
        int [] possibleCols = {1, 2, 2, 1, -1, -2, -2, -1};

        for (int i = 0; i < 8; i++) {
            int newRow = myPosition.getRow() + possibleRows[i];
            int newCol = myPosition.getColumn() + possibleCols[i];

            if (isValid(board, newRow + 1, newCol + 1)) {
                ChessPosition newPosition = new ChessPosition(newRow + 1, newCol + 1);
                ChessMove move = new ChessMove(myPosition, newPosition, null);
                knightMove.add(move);
            }
        }
        return knightMove;
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
                    ChessPiece chessP = board.getPiece(new ChessPosition(newRow + 1, newCol + 1));
                    ChessPosition newPosition = new ChessPosition(newRow + 1, newCol + 1);
                    ChessMove move = new ChessMove(myPosition, newPosition, null);
                    rookMove.add(move);

                    newRow += row;
                    newCol += col;

                    if (chessP != null) {
                        break;
                    }
                }
            }
        }
        return rookMove;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pawnMove = new ArrayList<>();
        Collection<ChessMove> promoted = new ArrayList<>();

        int direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int promotionRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 6 : 1;
        int startRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : 6;

        // Single move forward
        int newRow = myPosition.getRow() + direction;
        int newCol = myPosition.getColumn();

        if (isValid(board, newRow + 1, newCol + 1)) {
            ChessPiece destinationPiece = board.getPiece(new ChessPosition(newRow + 1, newCol + 1));
            if (destinationPiece == null) {
                ChessMove move = new ChessMove(myPosition, new ChessPosition(newRow + 1, newCol + 1), null);
                pawnMove.add(move);

                // Double move on initial position
                if (startRow == myPosition.getRow() && pieceColor == ChessGame.TeamColor.WHITE) {
                    ChessMove doubleMove = new ChessMove(myPosition, new ChessPosition(newRow + 2, newCol + 1), null);
                    ChessPiece chessP = board.getPiece(new ChessPosition(newRow + 2, newCol + 1));
                    if (chessP == null) {
                        pawnMove.add(doubleMove);
                    }
                }

                if (startRow == myPosition.getRow() && pieceColor == ChessGame.TeamColor.BLACK) {
                    ChessMove doubleMove = new ChessMove(myPosition, new ChessPosition(newRow, newCol + 1), null);
                    ChessPiece chessP = board.getPiece(new ChessPosition(newRow, newCol + 1));
                    if (chessP == null) {
                        pawnMove.add(doubleMove);
                    }
                }
            }
        }

        // Diagonal capture moves
        int[] captureCols = {-1, 1};

        for (int captureCol : captureCols) {
            int captureRow = myPosition.getRow() + direction;
            int captureColAdjusted = myPosition.getColumn() + captureCol;

            if (isValid(board, captureRow + 1, captureColAdjusted + 1)) {
                ChessPiece capturePiece = board.getPiece(new ChessPosition(captureRow + 1, captureColAdjusted + 1));

                if (capturePiece != null && capturePiece.getTeamColor() != pieceColor) {
                    ChessMove captureMove = new ChessMove(myPosition, new ChessPosition(captureRow + 1, captureColAdjusted + 1), null);
                    pawnMove.add(captureMove);
                }
            }
        }
        if (myPosition.getRow() == promotionRow) {
            for (ChessMove move : pawnMove) {
                // Promotion to Queen
                promoted.add(new ChessMove(myPosition, new ChessPosition(move.getEndPosition().getRow() + 1, move.getEndPosition().getColumn() + 1), ChessPiece.PieceType.QUEEN));
                // Promotion to Bishop
                promoted.add(new ChessMove(myPosition, new ChessPosition(move.getEndPosition().getRow() + 1, move.getEndPosition().getColumn() + 1), ChessPiece.PieceType.BISHOP));
                // Promotion to Rook
                promoted.add(new ChessMove(myPosition, new ChessPosition(move.getEndPosition().getRow() + 1, move.getEndPosition().getColumn() + 1), ChessPiece.PieceType.ROOK));
                // Promotion to Knight
                promoted.add(new ChessMove(myPosition, new ChessPosition(move.getEndPosition().getRow() + 1, move.getEndPosition().getColumn() + 1), ChessPiece.PieceType.KNIGHT));
            }
        }

        if (promoted.isEmpty()) {
            return pawnMove;
        }
        else{
            return promoted;
        }
    }


    private boolean isValid(ChessBoard board, int row, int col) {
        if (row > 8 || row < 1 || col > 8 || col < 1){
            return false;
        }
        ChessPiece square = board.getPiece(new ChessPosition(row, col));
        return square == null || square.getTeamColor() != pieceColor;
    }
}
