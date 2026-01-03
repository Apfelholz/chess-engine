import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;

public class bord {
    public int[][] board = new int[8][8];
    public char activePlayer;
    public boolean[] castlingRights = new boolean[4]; // K, Q, k, q
    public String enPassant = "-";
    public int halfmoveClock;
    public int fullmoveNumber;

    public String lastMove = "";
    public bord lastBord;

    public bord(String move, int toR, int toC, int piceRow, int piceCol, bord lastbord){
        this.lastMove = move;
        this.lastBord = lastbord;

        this.board = lastbord.board.clone();
        if (this.activePlayer == 'w'){
            this.activePlayer = 'b';
        }else {
            this.activePlayer = 'w';
        }

        halfmoveClock++;
        fullmoveNumber = halfmoveClock /2;

        move(move, piceRow, piceCol);
    }

    private void move(String move, int piceRow, int piceCol){
        int tomove = board[piceRow][piceCol];
        board[piceRow][piceCol] = 0;
        int[] data = parseMove(move);
        board[data[1]][data[2]] = tomove;
    }

    public void init(String fen) {
        Map<Character, Integer> encoding = new HashMap<>();
        encoding.put('P', 1);
        encoding.put('N', 2);
        encoding.put('B', 3);
        encoding.put('R', 4);
        encoding.put('Q', 5);
        encoding.put('K', 6);
        encoding.put('p', -1);
        encoding.put('n', -2);
        encoding.put('b', -3);
        encoding.put('r', -4);
        encoding.put('q', -5);
        encoding.put('k', -6);

        String[] parts = fen.split(" ");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid FEN: expected 6 space-separated fields");
        }

        String placement = parts[0];
        String active = parts[1];
        String castling = parts[2];
        String ep = parts[3];

        // Clear board
        for (int r = 0; r < 8; r++) {
            Arrays.fill(board[r], 0);
        }

        int rank = 0;
        int file = 0;
        for (char c : placement.toCharArray()) {
            if (c == '/') {
                rank++;
                file = 0;
                continue;
            }
            if (Character.isDigit(c)) {
                int empties = c - '0';
                for (int i = 0; i < empties; i++) {
                    if (rank >= 8 || file >= 8) break;
                    board[rank][file++] = 0;
                }
                continue;
            }
            Integer val = encoding.get(c);
            board[rank][file++] = (val != null) ? val : 0;
        }

        activePlayer = active.charAt(0);

        Arrays.fill(castlingRights, false);
        if (!"-".equals(castling)) {
            for (char c : castling.toCharArray()) {
                switch (c) {
                    case 'K': castlingRights[0] = true; break;
                    case 'Q': castlingRights[1] = true; break;
                    case 'k': castlingRights[2] = true; break;
                    case 'q': castlingRights[3] = true; break;
                }
            }
        }

        enPassant = "-".equals(ep) ? "-" : ep;

        try {
            halfmoveClock = Integer.parseInt(parts[4]);
            fullmoveNumber = Integer.parseInt(parts[5]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid move counters in FEN", e);
        }
    }

    public ArrayList<bord> getMoves() {
        ArrayList<bord> moves = new ArrayList<>();

        boolean whiteToMove = activePlayer == 'w';

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];
                if (piece == 0) continue;

                if (whiteToMove && piece > 0) {
                    generatePieceMoves(piece, row, col, moves);
                } else if (!whiteToMove && piece < 0) {
                    generatePieceMoves(piece, row, col, moves);
                }
            }
        }
        return moves;
    }

    private void generatePieceMoves(int piece, int row, int col, ArrayList<bord> moves) {
    int abs = Math.abs(piece);
        switch (abs) {
            case 1 -> generatePawnMoves(piece, row, col, moves);
            case 2 -> generateKnightMoves(piece, row, col, moves);
            case 3 -> generateSlidingMoves(piece, row, col, moves, BISHOP_DIRS);
            case 4 -> generateSlidingMoves(piece, row, col, moves, ROOK_DIRS);
            case 5 -> generateSlidingMoves(piece, row, col, moves, QUEEN_DIRS);
            case 6 -> generateKingMoves(piece, row, col, moves);
        }
    }

    private void generatePawnMoves(int piece, int row, int col, ArrayList<bord> moves) {
        int dir = piece > 0 ? -1 : 1;
        int startRow = piece > 0 ? 6 : 1;

        // one forward
        if (inBounds(row + dir, col) && board[row + dir][col] == 0) {
            addMove(piece, row, col, row + dir, col, moves);

            // two forward
            if (row == startRow && board[row + 2 * dir][col] == 0) {
                addMove(piece, row, col, row + 2 * dir, col, moves);
            }
        }

        // captures
        for (int dc : new int[]{-1, 1}) {
            int r = row + dir;
            int c = col + dc;
            if (inBounds(r, c) && board[r][c] * piece < 0) {
                addMove(piece, row, col, r, c, moves);
            }
        }
    }

    private static final int[][] KNIGHT_MOVES = {
        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
        {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    private void generateKnightMoves(int piece, int row, int col, ArrayList<bord> moves) {
        for (int[] d : KNIGHT_MOVES) {
            int r = row + d[0];
            int c = col + d[1];
            if (!inBounds(r, c)) continue;
            if (board[r][c] * piece <= 0) {
                addMove(piece, row, col, r, c, moves);
            }
        }
    }

    private static final int[][] BISHOP_DIRS = {
        {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };
    private static final int[][] ROOK_DIRS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };
    private static final int[][] QUEEN_DIRS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1},
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    private void generateSlidingMoves(int piece, int row, int col, ArrayList<bord> moves, int[][] dirs) {
        for (int[] d : dirs) {
            int r = row + d[0];
            int c = col + d[1];

            while (inBounds(r, c)) {
                if (board[r][c] == 0) {
                    addMove(piece, row, col, r, c, moves);
                } else {
                    if (board[r][c] * piece < 0) {
                        addMove(piece, row, col, r, c, moves);
                    }
                    break;
                }
                r += d[0];
                c += d[1];
            }
        }
    }

    private static final int[][] KING_MOVES = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1}, {1, 0}, {1, 1}
    };

    private void generateKingMoves(int piece, int row, int col, ArrayList<bord> moves) {
        for (int[] d : KING_MOVES) {
            int r = row + d[0];
            int c = col + d[1];
            if (!inBounds(r, c)) continue;
            if (board[r][c] * piece <= 0) {
                addMove(piece, row, col, r, c, moves);
            }
        }
        // TODO: castling
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }

    private void addMove(int piece, int fromR, int fromC, int toR, int toC, ArrayList<bord> moves) {
        moves.add(new bord(makeMove(piece, toR, toC), toR, toC, fromR, fromC, this));
    }

    private int[] parseMove(String move) {
        // piece
        char pieceChar = move.charAt(0);
        char lower = Character.toLowerCase(pieceChar);

        int type = switch (lower) {
            case 'p' -> 1;
            case 'n' -> 2;
            case 'b' -> 3;
            case 'r' -> 4;
            case 'q' -> 5;
            case 'k' -> 6;
            default -> -1;
        };

        // target square
        char fileChar = move.charAt(1);
        char rankChar = move.charAt(2);

        int col = fileChar - 'A';
        int row = '8' - rankChar;

        return new int[] { type, row, col };
    }


    private String makeMove(int type, int row, int col) {
    StringBuilder move = new StringBuilder();

    // piece letter
    char pieceChar = switch (type) {
        case 1 -> 'p';
        case 2 -> 'n';
        case 3 -> 'b';
        case 4 -> 'r';
        case 5 -> 'q';
        case 6 -> 'k';
        default -> '?';
    };

    // black pieces uppercase
    if (activePlayer == 'b') {
        pieceChar = Character.toUpperCase(pieceChar);
    }

    move.append(pieceChar);

    // target square
    char file = (char) ('A' + col);
    char rank = (char) ('8' - row);

    move.append(file);
    move.append(rank);

    return move.toString();
}

public bord() {
    this.board = new int[8][8];
    this.activePlayer = 'w'; // Set a default active player
    this.castlingRights = new boolean[4]; // K, Q, k, q
    this.enPassant = "-";
    this.halfmoveClock = 0;
    this.fullmoveNumber = 1; // Starting from the first move
}

public boolean moveFromNotation(String moveNotation) {
    if (moveNotation == null || moveNotation.length() != 4) {
        return false;
    }

    try {
        // Parse Quellfeld (von)
        char fromFile = moveNotation.charAt(0);
        char fromRank = moveNotation.charAt(1);
        int fromCol = fromFile - 'a';
        int fromRow = '8' - fromRank;

        // Parse Zielfeld (zu)
        char toFile = moveNotation.charAt(2);
        char toRank = moveNotation.charAt(3);
        int toCol = toFile - 'a';
        int toRow = '8' - toRank;

        // Validiere Koordinaten
        if (!inBounds(fromRow, fromCol) || !inBounds(toRow, toCol)) {
            return false;
        }

        // Prüfe ob Feld besetzt ist
        int piece = board[fromRow][fromCol];
        if (piece == 0) {
            return false;
        }

        // Prüfe ob richtige Seite am Zug ist
        boolean whiteToMove = activePlayer == 'w';
        if ((whiteToMove && piece <= 0) || (!whiteToMove && piece > 0)) {
            return false;
        }

        // Prüfe ob Zielfeld leer oder gegnerisch besetzt ist
        int targetPiece = board[toRow][toCol];
        if (targetPiece != 0 && (piece > 0) == (targetPiece > 0)) {
            return false; // Eigene Figur im Weg
        }

        // Führe den Zug aus
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = 0;

        // Wechsle Spieler
        activePlayer = (activePlayer == 'w') ? 'b' : 'w';
        halfmoveClock++;
        fullmoveNumber = halfmoveClock / 2;

        // Speichere letzten Zug
        lastMove = moveNotation;

        return true;

    } catch (Exception e) {
        return false;
    }
}
}