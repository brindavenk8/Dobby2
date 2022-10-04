import java.io.*;
import java.util.*;
import java.nio.file.*;

public class Dobby {
    static Board[] globalBoard = new Board[9];
    static char ourMoveChar = 'd';
    static File dobbyGo = new File("Dobby.go");
    static boolean on;
    public static Board globalWinners = new Board();
    static int depth = 3;

    public static Board[] globalBoard() {
        for(int i = 0; i < 9; i++) {
            globalBoard[i] = new Board();
        }
        return globalBoard;
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        System.out.println("running main!");
        int nextBoard = initializeBoard(); //play first four moves provided by referee
        System.out.println("initialized board");

        while(!isGameOver()){ // loop to play the game with Dobby 
            //System.out.println("Dobby is inside the game loop");
            boolean dobbyPlays = dobbyGo.exists();
            on = true;
            
            if(dobbyPlays){
                
                String lastMove = ""; //the last move that was played by the opponent
                
                //get opponent's last move
                try { 
                    File moveFile = new File("move_file");
                    Scanner scanner = new Scanner(moveFile);
                    if(moveFile.length() != 0) { //check if opponent's move is written in move_file
                        lastMove = scanner.nextLine(); //get opponent move from move_file
                        String moves[] = lastMove.split(" ");
                        String player = moves[0];
                       

                        if(!player.equals("Dobby")) {
                            System.out.println("Opponent's last move: " + lastMove); 
                            nextBoard = Board.markBoardWithMove(lastMove, ourMoveChar, globalBoard, globalWinners); //mark our board with opponent move
                            System.out.println("stored opponent's move");
                        }
                    }
                    scanner.close();         
                  } 
                  catch (FileNotFoundException e) {
                    System.out.println("An error occurred while reading in opponent's move.");
                    e.printStackTrace();
                  }

                dobbyPlays = false;

                // Dobby plays!
                System.out.println("Dobby will make a move in board #" + nextBoard);
                makeAMove(nextBoard); //minimax with alpha-beta pruning
                System.out.println("Dobby made a move");
                Thread.sleep(100);
                
            }
        }

    }

    /*
     * makeAMove: 
     * implement minimax with alpha-beta pruning
     * might require helper functions
     */
    static void makeAMove(int currentBoard) throws IOException {
        System.out.println("Dobby is trying to pick a move to play! In board #" + currentBoard);

        int chosenMove = 0; // square in local board -- we will pick this through minimax with alpha-beta pruning
        int currentBoard2 = currentBoard; 
        int bestScore = Integer.MIN_VALUE;

        // if currentBoard is won, pick a new local board to play in
        if(globalWinners.squares[currentBoard] == 'd') { //board was won by Dobby -- need to pick a different board to play in
            System.out.println("Dobby won currentBoard = "+currentBoard+", choosing currentBoard2");
            // globalWinners.squares[currentBoard] = 'd';
            currentBoard2 = gamePathTree(globalWinners).getFirst();
            System.out.println("currentBoard2 = "+currentBoard2);
            chosenMove = gamePathTree(globalBoard[currentBoard2]).getFirst();
            //call minimaxUtil
        }
        else if(globalWinners.squares[currentBoard] == 'o') { //board was won by opponent -- need to pick a different board to play in
            // globalWinners.squares[currentBoard] = 'o';
            System.out.println("opponnent won currentBoard = "+currentBoard+", choosing currentBoard2");
            currentBoard2 = gamePathTree(globalWinners).getFirst();
            System.out.println("currentBoard2 = "+currentBoard2);
            chosenMove = gamePathTree(globalBoard[currentBoard2]).getFirst();
            //call minimaxUtil
        }
        else { //board is not won by either player
            /*
            LinkedList<Integer> emptySquares = gamePathTree(globalBoard[currentBoard]);
            chosenMove = emptySquares.getFirst();
            */
            System.out.println("current board has not yet been won (if(winner == 0))");
            chosenMove = minimaxUtil(currentBoard, bestScore);
        } 

        String moveFileInput = "Dobby " + currentBoard2 + " " + chosenMove;

        Board.markBoardWithMove(moveFileInput, ourMoveChar, globalBoard, globalWinners); 
               
        try {
            Path movePath = Paths.get("move_file");
            //clear file before writing in it
            Files.writeString(movePath, moveFileInput, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
     * utility function for calling minimax algorithm
     * inputs:
     *      int board: index of the local board to start minimax evaluations in (root node)
     *      int bestScore: score value of the best move so far
     * output:
     *      int chosenMove: index of square to play in local board
     */
    public static int minimaxUtil(int board, int bestScore) {
        System.out.println("beginning minimaxUtil");
        int chosenMove = -1;

        // make copies of static global objects
        Board[] globalBoardCopy = new Board[9];
        for(int j = 0; j < globalBoard.length; j++) {
            globalBoardCopy[j] = globalBoard[j].makeBoardCopy();
        }
        Board globalWinnersCopy = globalWinners.makeBoardCopy();

        LinkedList<Integer> emptySquares = gamePathTree(globalBoardCopy[board]); // empty squares on local board
        char[] currentSquares = globalBoardCopy[board].squares; // array of all squares in local board
        System.out.println("made copies of globalBoard and globalWinners");
        // bestScore = minimax(globalBoardCopy, globalWinnersCopy, board, depth, false);
        
        //start the minimax loop
        System.out.println("starting the minimax for loop");
        for(int i = 0; i < emptySquares.size(); i++){ 
            System.out.println("minimax for loop i = " + i);
            // System.out.println("error = " +currentSquares[emptySquares.get(i)]);
            currentSquares[emptySquares.get(i)] = 'd';
            // ourBoard.squares[i] = 'd';
            System.out.println("doing recursive minimax now for opponent move");
            int score = minimax(globalBoardCopy, globalWinnersCopy, board, 0, false, 0, 10);
            System.out.println("ending recursive minimax");
            currentSquares[emptySquares.get(i)] = ' ';
            // bestScore = Math.max(score, bestScore);
            if(score > bestScore){
                System.out.println("updating bestScore");
                bestScore = score;
                chosenMove = emptySquares.get(i); //create a parallel linkedlist to emptySquares of scores
            } 
        }

        //choose a score from parallel list -- based on heuristic function to minimize tree expansion
        
        return chosenMove;
    }

    /*
     * minimax algorithm
     * input: 
     *      
     *      int board: local board in the uttt game
     *      int depth: how many moves deep to go (based on heuristic)
     *      boolean isMaximizing: if we want to get the score for maximizing player or minimizing player best move
     */
    public static int minimax(Board[] globalBoardCopy, Board globalWinnersCopy, int board, int localDepth,
     boolean isMaximizing, int alpha, int beta) {
        // int chosenMove = 0;
        
        // boolean end = isGameOver(); //TODO determine ending configuration, not end of game
        //isgameover will take the passed in winners 

        if(localDepth == Dobby.depth){    // TODO consider winning positions, DEPTH, and other terminal cases
            return pointsWon(globalBoardCopy);
        }

        if(isMaximizing) { //Dobby is playing
            int bestScore = Integer.MIN_VALUE;
            LinkedList<Integer> emptySquares = gamePathTree(globalBoardCopy[board]);

            for(int i = 0; i < emptySquares.size(); i++) {                
                //make copies of globalBoard and globalBoardWinners
                Board[] globalBoardCopy2 = new Board[9];
                for(int j = 0; j < globalBoardCopy.length; j++) {
                    globalBoardCopy2[j] = globalBoardCopy[j].makeBoardCopy();
                }
                Board globalWinnersCopy2 = globalWinnersCopy.makeBoardCopy();

                //set i to be new board
                globalBoardCopy2[board].squares[emptySquares.get(i)] = 'd';

                //get opponent's next step score
                int score = minimax(globalBoardCopy2, globalWinnersCopy2, emptySquares.get(i), localDepth + 1, false, alpha, beta); 
                
                //reset copy of board
                globalBoardCopy2[board].squares[emptySquares.get(i)] = ' ';

                // TODO do the move selection where the minimax is called - that will be one place
                /*
                if(score > bestScore){   
                    // just calc the score inside the minimax
                    // get max of score, bestScore
                    bestScore = score;
                    chosenMove = i; //not needed
                }
                 */
                bestScore = Math.max(score, bestScore);
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        }
        else if(!isMaximizing) { //opponent is playing
            int bestScore = Integer.MAX_VALUE;
            LinkedList<Integer> emptySquares = gamePathTree(globalBoardCopy[board]);

            for(int i = 0; i < emptySquares.size(); i++) {
                

                //make copies of globalBoard and globalBoardWinners
                Board[] globalBoardCopy2 = new Board[9];
                for(int j = 0; j < globalBoardCopy.length; j++) {
                    globalBoardCopy2[j] = globalBoardCopy[j].makeBoardCopy();
                }
                Board globalWinnersCopy2 = globalWinnersCopy.makeBoardCopy();

                //set i to be new board
                globalBoardCopy2[board].squares[emptySquares.get(i)] = 'o';

                //get Dobby's next step score
                int test = emptySquares.get(i);
                System.out.println("test = "+test);
                int score = minimax(globalBoardCopy2, globalWinnersCopy2, test, localDepth + 1, true, alpha, beta); 
                
                //reset copy of board
                globalBoardCopy2[board].squares[emptySquares.get(i)] = ' ';

                /*
                if(score < bestScore){ // get min of score, bestScore
                    bestScore = score;
                    chosenMove = i; //not needed
                }
                 */
                bestScore = Math.min(score, bestScore);
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        }
        return 0;
    }


    /*
     * initializeBoard: executes first four moves and determines Player 1
     */
    static int initializeBoard() {
        System.out.println("in initializeBoard");
        globalBoard = globalBoard(); //set up globalBoard
        int nextBoard = 0;
        File firstFourMoves = new File("first_four_moves");

        boolean waiting = true;

        while(waiting) {
            if(firstFourMoves.exists()) {
                // populate board with first four moves
                try {
                    System.out.println("started try to populate first four moves");
                    Scanner scanner = new Scanner(firstFourMoves);
                    boolean initialized = false;
                    while(!initialized) {
                        if(firstFourMoves.exists()) {
                            while (scanner.hasNextLine()) {
                                System.out.println("found next line");
                            String move = scanner.nextLine();
                            System.out.println("move: " + move);
                            //mark the board -- helper
                            nextBoard = Board.markBoardWithMove(move, ourMoveChar, globalBoard, globalWinners); //update our board with the first four moves provided   
                            System.out.println("marked a move");
                            }
                            initialized = true;
                        }
                    }
                    scanner.close();
            
                    } catch (FileNotFoundException e) {
                        System.out.println("An error occurred while reading in first four moves.");
                        e.printStackTrace();
                    }
                waiting = false;
            }
        }

        
          return nextBoard;

    }



    /*
     * playOpponentMove: read the opponent move, verify they are valid, and execute them. 
     * If the opponent move is invalid, your program must display the offending move, 
     * and state the error.
     * Input:
     * Output:
     */
    void playOpponentMove() {

    }

    /*
     * pointsWon: utility function for determining number of points won by current player
     * Input: 
     *      Board terminalConfig: terminal board configuration
     * Output:
     *      int points: number of points won by current player
     */
    static int pointsWon(Board[] globalBoardCopy){ //TODO write scoring function in here
        int score = 0;

        // LinkedList<Integer> emptySquares = gamePathTree(square);

        // int[] points = new int[9]; //storing points for each square in terminalConfig

        // points[4] = 5; //middle square is best
        
        for(int i = 0; i < globalBoardCopy.length; i++) {
            for (int j = 0; j < globalBoardCopy[i].squares.length; j++) {
                if((globalBoardCopy[i].squares[j] == 'd')) {
                    score++;
                }
                else if ((globalBoardCopy[i].squares[j] == 'o')) {
                    score--;
                }
            }
        }


        return score;
    }


    /*
     * utility function to determine validity of a move (opponent or self)
     * reads input file and tests if valid against globalBoard 
     */
    boolean isMoveValid(String move) {
        boolean valid = false;





        return valid;
    }

    /*
     * isGameOver: check if game has ended
     */
    static boolean isGameOver() {
        boolean end = false;
        File endgame = new File("end_game");

        if(endgame.exists()){
            end = true;
        }
        String test = "";
        if (end) {
            test = "true";
            System.out.println("Player 1 won the game. NEED TO UPDATE THIS FUNCTION. -- end = " + test);
        }
        else{
            test = "false";
        }

        
        return end;
    }

    /*
     * input: localBoard (determined by last move played by opponent)
     */
    static LinkedList<Integer> gamePathTree(Board currentBoard) { //list of empty squares in input localBoard-- possible squares to play
        
        //loop to get to the end 
        //TODO: eventually implement heuristic to determine when to stop
        LinkedList<Integer> emptySquares = new LinkedList<Integer>();
        // Board currentBoard = globalBoard[localBoard];
        System.out.println("displaying currentBoard = " + currentBoard);
        currentBoard.display();
        System.out.println("-----------------");
        for(int i = 0; i <= 8; i++){ //discovering all empty squares in the current local board being played
            System.out.println(currentBoard.squares[i]);
            if((currentBoard.squares[i] == 'd') || (currentBoard.squares[i] == 'o')){
                
            }
            else {
                emptySquares.add(i);
            }
        }
    
        System.out.println("indices of empty squares in current local board, board = " + currentBoard);
        for(int i = 0; i < emptySquares.size(); i++){ //discovering all empty squares in the current local board being played
            System.out.println(emptySquares.get(i));
        }

        return emptySquares;
    }



}