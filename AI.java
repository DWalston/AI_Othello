import java.util.Scanner;
import java.util.Arrays;

public class AI {
    // the board for the game
    public Board game;
    // true if it's the AI's turn, false if it's the human's
    private boolean turn;
    // reference for which player is using which piece
    private char piece; private char opPiece;
    // used to get the human's input
    private Scanner in = new Scanner(System.in);
    // Constants for the AI to use
    final private int m_d = 8;    // max depth for search
    /* Since there is a total of 64 spaces, the theoretical min/max scores are
       -64 and 64, respectively. Here we set the absolute min and max scores to be
       -100 and 100, simply to ensure there is no weird effects with the heuristic checking*/
    final private int MIN = -100;  // lowest possible score
    final private int MAX = 100;   // highest possible score
    
    // Default initilization. By default, AI goes second
    public AI () {
        game = new Board();
        turn = false;
        piece = 'O';
        opPiece = 'X';
    }
    
    // allows user to set whether AI goes first or second
    // Parameter: True if AI goes first, False if Human goes first
    public AI (boolean first) {
        game = new Board();
        turn = first;
        if (turn) {
            piece = 'O';
            opPiece = 'X';
        } else {
            piece = 'X';
            opPiece = 'O';
        }
    }
        
    /* create an instance with a prebuilt board. for testing purposes
       Parameters:
       board: initial gamestate
       first: True if AI goes first, False if Human goes first
    */
    public AI (Board board, boolean first) {  
        game = board;
        turn = first;
        if (turn) {
            piece = 'O';
            opPiece = 'X';
        } else {
            piece = 'X';
            opPiece = 'O';
        }
    }
    
    /* Makes a single turn of the game, which involves finding the next move to make
       and sending it to the board, then printing the board state for the human player
    
       If it is the human's turn, it will take user input from the command line
       and make the inputted move unless it is not legal, in which case it will request new input
    
       If it is the AI's turn, it will begin the AB-pruning process, beginning with each move it can
       currently legally make
    */
    public boolean makeTurn() {
        // Human's turn
        if (!turn) {
            // The system will loop until the human puts in a valid move input, in the form "x y"
            boolean done = false;
            while (!done) {
                // Check if the player has any possible moves
                int[][] moves = game.getMoves(opPiece);
                // if not, end the game
                if (moves.length == 0)
                    return true;
                // inform players they can take their turn, and give possible moves as reference
                System.out.println("Your Move (piece = " + opPiece + ")");
                System.out.println("Possible moves: " + Arrays.deepToString(moves)); 
                // take input and apply move
                int x = in.nextInt();
                int y = in.nextInt();
                done = game.makeMove(x,y);
                if (!done)
                    System.out.println("Invalid move: try again");
            }
        // AI's turn
        } else {
            System.out.println("AI's move (piece = " + piece + ")");
            // find all moves the AI can take
            int[][] moves = game.getMoves(piece);
            if (moves.length == 0)
                return true;
            
            // Begin minmax searching with AB-pruning, starting with each move the AI can currently take
            int[] bestMove = moves[0];  // move with the best possible end score
            float score; float bestScore = MIN;   // current score and best found score respectively
            // for each possible move,
            for (int[] m : moves) {
                // Find the best possible score resulting from each move, assuming opponent plays optimally
                score = abpruning(game, m[0], m[1],bestScore, MAX, false, 1);
                // If score of tested branch is better, set bestScore and store the move
                if (score > bestScore) {
                    bestMove = m;
                    bestScore = score;
                }
            }
            // Make best found score, with user referrence
            System.out.println("Placing at: " + Arrays.toString(bestMove));
            game.makeMove(bestMove[0], bestMove[1]);
        }
        
        // print current board state and switch the turns
        System.out.println(game.toString());
        turn = !turn;
        return false;
    }
    
    // Find the testing heuristic for the current board state
    // Parameter: the current board state to be tested
    public float getHeuristic(Board gameState) {
        int opMoves = gameState.getMoves(opPiece).length;  // Number of moves the opponent can make
        if (opMoves == 0)  // if the opponent cant legally take any moves
            return testWinner(gameState);  // See if AI is currently winning
        
        /* Current Heuristic:
           (Number of pieces AI has - Number of pieces opponent has) / number of moves the opponent can make*/
        return (gameState.countTiles(piece) - gameState.countTiles(opPiece))/opMoves;
    }
    
    // When a game end is found, the heuristic is based on whether the AI won the match or not
    public float testWinner(Board gameState) {
        int ascore = gameState.countTiles(piece);   // the ai's score
        int pscore = gameState.countTiles(opPiece); // the opponent's score
        if (ascore > pscore)       // win scenario
            return MAX;
        else if (ascore < pscore)  // lose scenario
            return MIN;
        else                       // tie
            return MIN + 1;        // not preferred, but better than opponent winning
    }
    
    /* Main AB-pruning method. Calls recursively up to the max depth to test each possible move
       to find the best possible gamestate, assuming the opponent is playing optimally
    
       Parameters:
       testCase: the current board state to test
       x,y: the next move (in the form [x,y]) to be tested by the min-max search
       a,b: the current Alpha and Beta to be used while pruning branches
       turn: True if it is the AI's turn, False if it is the Human's turn
       depth: the current depth of the recursion. Used to prevent stackOverflow errors.
    */
    public float abpruning (Board testCase, int x, int y, float a, float b, boolean turn, int depth) { 
        // to begin, create a new testing board to prevent ruining the current game state
        Board test = new Board(testCase);
        test.makeMove(x,y);
        //System.out.println(test.toString());  // for testing, do not turn back on
        // Get the current moves which can be made, depending on who's turn is being tested
        int[][] moves;
        if (turn)
            moves = test.getMoves(piece);
        else
            moves = test.getMoves(opPiece);
        
        // If there is no legal moves, test who wins in this board state
        if (moves.length == 0) {
            return testWinner(test);
        }
        
        // otherwise, if the max depth is reached, test the current heuristic
        if (depth >= m_d) {
            return getHeuristic(test);
        }
        
        float score; float bestScore;
        if (turn)  // Max Search
            bestScore = MIN;
        else       // Min Search
            bestScore = MAX;
        for (int[] m : moves) {  // For each legal move,
            // test the best score coming from the branch
            score = abpruning(test,m[0],m[1],a,b,!turn,(depth+1));
            // Maximizing
            if (turn) {
                if (score > bestScore) {
                    bestScore = score;
                    if (bestScore > b)     // if the best score is greater than the current branch min,
                        return bestScore;  // prune
                    if (bestScore > a)     // if current score is greater than current branch max,
                        a = bestScore;     // set new alpha
                }
            // Minimizing
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    if (bestScore < a)      // if the current score is less than the current branch max
                        return bestScore;   // prune
                    if (bestScore < b)      // if current score is greater than current branch min,
                        b = bestScore;      // set new beta
                }
            }
        }
        
        return bestScore;  // return the best score
    }
}