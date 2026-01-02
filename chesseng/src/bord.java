import java.util.HashMap;

public class bord {
    public int[][] lineUp;
    public char player;
    public boolean[] castling;
    public char[] EnPassant;
    public int halfmove;
    public int move;

    public void init(String inLineUp) {

        HashMap<Character, Integer> encoding = new HashMap<>();
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

        int line = 0;
        int col = 0;

        for (char c : inLineUp.substring(0, 71).toCharArray()){
            if (c == '/'){
                line++;
                continue;
            }
            lineUp[line][col] = encoding.get(c);
            col++;
        }

        for (char c : inLineUp.split(" ")[1].toCharArray()){
           if (c == '-'){
            break;
           }

           if (c == 'K'){
            castling[0] = true;
           }else if (c == 'Q'){
            castling[1] = true;
           }else if (c == 'k'){
            castling[2] = true;
           }else if (c == 'q'){
            castling[3] = true;
           }
        }

        int i = 0;

        for (char c : inLineUp.split(" ")[2].toCharArray()){
            if (c == '-'){
                break;
            }else {
                if (i == 0){
                    EnPassant[0] = c;
                    i++;
                }
                if (i == 1){
                    EnPassant[1] = c;
                }
            }
        }

        halfmove = Integer.parseInt(inLineUp.split(" ")[3]);

        move = Integer.parseInt(inLineUp.split(" ")[4]);
    }
}