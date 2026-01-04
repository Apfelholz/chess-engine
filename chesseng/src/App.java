import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class App {
    static bord currentBoard;
    
    public static void main(String[] args) throws Exception {

        BufferedReader r = new BufferedReader(
            new InputStreamReader(System.in));

        if (r.readLine().equals("uci")){
            System.out.println("id name chesseng");
            System.out.println("uciok");
        }

        if (r.readLine().equals("isready")){
            System.out.println("readyok");
        }

        String comand = r.readLine();

        String startpos = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0";
        currentBoard = new bord();
        currentBoard.init(startpos);

        Boolean posReady = false;

        while(!comand.equals("exit")){
            comand = r.readLine();

            if (comand.equals("isready")){
                System.out.println("readyok");
            }else if (comand.startsWith("position")){
                posReady = true;
                parsePosition(comand);
            }else if (comand.startsWith("go") && posReady) {
                System.out.println("bestmove " + getbestmove(comand));
            } else {
                System.out.println("------ comand unprossasbil: " + comand);
            }
        }
    }

    static private void parsePosition(String command) {
        String[] parts = command.split(" ");
        int index = 1; // Skip "position"
        
        String fenString;
        if (parts[index].equals("startpos")) {
            fenString = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0";
            index++;
        } else if (parts[index].equals("fen")) {
            // FEN string spans multiple tokens
            StringBuilder fen = new StringBuilder();
            index++;
            int fenParts = 0;
            while (index < parts.length && fenParts < 6 && !parts[index].equals("moves")) {
                fen.append(parts[index]).append(" ");
                fenParts++;
                index++;
            }
            fenString = fen.toString().trim();
        } else {
            return; // Invalid command
        }
        
        // Initialize board with FEN
        currentBoard = new bord();
        currentBoard.init(fenString);
        
        // Parse moves if present
        if (index < parts.length && parts[index].equals("moves")) {
            index++;
            List<String> moves = new ArrayList<>();
            while (index < parts.length) {
                moves.add(parts[index]);
                index++;
            }
            
            // Apply moves to board
            for (String move : moves) {
                applyMove(move);
            }
        }
    }
    
    static private void applyMove(String move) {
        currentBoard.moveFromNotation(move);
        System.out.println(move);
    }

    static private String getbestmove(String comand){
        // EINSTELLUNG: Anzahl der Ebenen für die Breitensuche
        int SEARCH_DEPTH = 3;
        
        ArrayList<bord> moves = currentBoard.getMoves();
        
        if (moves.isEmpty()) {
            return "0000"; // Keine legalen Züge verfügbar
        }
        
        String bestMove = moves.get(0).lastMove;
        int bestEval = Integer.MIN_VALUE;
        boolean maximizing = currentBoard.activePlayer == 'w';
        
        // Für jeden möglichen ersten Zug
        for (bord firstMove : moves) {
            int eval = breadthFirstSearch(firstMove, SEARCH_DEPTH - 1, !maximizing);
            
            // Beste Bewertung für den aktuellen Spieler finden
            if (maximizing) {
                if (eval > bestEval) {
                    bestEval = eval;
                    bestMove = firstMove.lastMove;
                }
            } else {
                if (eval < bestEval || bestEval == Integer.MIN_VALUE) {
                    bestEval = eval;
                    bestMove = firstMove.lastMove;
                }
            }
        }
        
        return bestMove;
    }

    static private int breadthFirstSearch(bord position, int depth, boolean maximizing) {
        // Basis-Fall: Maximale Tiefe erreicht
        if (depth == 0) {
            return position.eval;
        }
        
        ArrayList<bord> moves = position.getMoves();
        
        // Keine legalen Züge mehr (Schachmatt oder Patt)
        if (moves.isEmpty()) {
            return position.eval;
        }
        
        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            // Alle Züge auf dieser Ebene durchgehen
            for (bord move : moves) {
                int eval = breadthFirstSearch(move, depth - 1, false);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            // Alle Züge auf dieser Ebene durchgehen
            for (bord move : moves) {
                int eval = breadthFirstSearch(move, depth - 1, true);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }

    static public void printBoard(bord bord){
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                System.out.print(bord.board[rank][file] + " ");
            }
            System.out.println();
        }
    }
}
