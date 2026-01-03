// java
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;

public class bordTest {

    private final String fen = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq e3 5 10";

    private HashMap<Character, Integer> makeEncoding() {
        HashMap<Character, Integer> encoding = new HashMap<>();
        encoding.put('P', 1); encoding.put('N', 2); encoding.put('B', 3);
        encoding.put('R', 4); encoding.put('Q', 5); encoding.put('K', 6);
        encoding.put('p', -1); encoding.put('n', -2); encoding.put('b', -3);
        encoding.put('r', -4); encoding.put('q', -5); encoding.put('k', -6);
        return encoding;
    }

    @Test
    public void testInit_ParsesWithStandard8x8LineUp() {
        bord b = new bord();
        b.board = new int[8][8];
        b.castlingRights = new boolean[4];
        b.enPassant = "-";
        b.init(fen);

        assertEquals(-4, b.board[0][0]);
    }

    @Test
    public void testInit_ParsesCastlingEnPassantAndMoves_WhenWideLineUp() {
        bord b = new bord();
        b.board = new int[8][64];       // make columns wide so init doesn't index out of bounds
        b.castlingRights = new boolean[4];
        b.enPassant = "-";

        b.init(fen);

        assertTrue(b.castlingRights[0]); // K
        assertTrue(b.castlingRights[1]); // Q
        assertTrue(b.castlingRights[2]); // k
        assertTrue(b.castlingRights[3]); // q

        assertEquals("e3", b.enPassant);

        assertEquals(5, b.halfmoveClock);
        assertEquals(10, b.fullmoveNumber);
    }

    @Test
    public void testInit_PopulatesBoard_AccordingToImplementationScanningLogic() {
        bord b = new bord();
        b.board = new int[8][64];
        b.castlingRights = new boolean[4];
        b.enPassant = "-";

        b.init(fen);

        HashMap<Character, Integer> encoding = makeEncoding();

        int line = 0;
        int col = 0;
        String boardSub = fen.split(" ")[0];
        for (char c : boardSub.toCharArray()) {
            if (c == '/') {
                line++;
                col = 0;
                continue;
            }
            if (Character.isDigit(c)) {
                col += Character.getNumericValue(c);
                continue;
            }
            int expected = encoding.get(c);
            assertEquals("Mismatch at line " + line + " col " + col + " for char '" + c + "'",
                         expected, b.board[line][col]);
            col++;
        }
    }
}