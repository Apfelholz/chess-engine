import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class App {
    static bord currentBoard;
    
    public static void main(String[] args) throws Exception {
        System.out.println("id name chesseng");
        System.out.println("uciok");

        BufferedReader r = new BufferedReader(
            new InputStreamReader(System.in));

        if (r.readLine() == "isready"){
            System.out.println("readyok");
        }

        String comand = r.readLine();

        String startpos = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0";
        currentBoard = new bord();
        currentBoard.init(startpos);

        while(comand != "exit"){
            comand = r.readLine();
            
            if (comand != null && comand.startsWith("position")) {
                parsePosition(comand);
                System.out.println(getbestmove(comand));
            } else {
                System.out.println("broken...");
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
        
    }

    static private String getbestmove(String comand){
        ArrayList<bord> moves = currentBoard.getMoves();
        return moves.getFirst().lastMove;
    }
}
