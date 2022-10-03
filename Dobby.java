import java.io.*;
import java.util.*;
import java.nio.file.*;

public class Dobby {
    static Board[] globalBoard = new Board[9];
    static char ourMoveChar = 'd';
    static File dobbyGo = new File("Dobby.go");
    static boolean on;
    public static Board globalWinners = new Board();
    static int depth = 1;

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
                            nextBoard = globalBoard[0].markBoardWithMove(lastMove, ourMoveChar, globalBoard, on); //mark our board with opponent move
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
        System.out.println("Dobby is trying to pick a move to play!");

        int chosenMove = 0; // square in local board -- we will pick this through minimax with alpha-beta pruning
        int currentBoard2 = currentBoard; 
        int bestScore = Integer.MIN_VALUE;

        Board ourBoard = globalBoard[currentBoard];
       
        int winner = ourBoard.isBoardAvailable();

        // if currentBoard is won, pick a new local board to play in
        if(winner == 0) { //board is not won by either player
            /*
            LinkedList<Integer> emptySquares = gamePathTree(globalBoard[currentBoard]);
            chosenMove = emptySquares.getFirst();
            */
            minimaxUtil(currentBoard, bestScore);
        }
        else if(winner == 1) { //board was won by Dobby -- need to pick a different board to play in
            // globalWinners.squares[currentBoard] = 'd';
            currentBoard2 = gamePathTree(globalWinners).getFirst();
            chosenMove = gamePathTree(globalBoard[currentBoard2]).getFirst();
            //call minimaxUtil
        }
        else if(winner == 2) { //board was won by opponent -- need to pick a different board to play in
            // globalWinners.squares[currentBoard] = 'o';
            currentBoard2 = gamePathTree(globalWinners).getFirst();
            chosenMove = gamePathTree(globalBoard[currentBoard2]).getFirst();
            //call minimaxUtil
        }
       

/*
 * 1. find the list of possible locations to play inside the local board < 9
 * 2. within loop
 * 3. make board copy, assign yourself to each location 
 * 4. assess each board-copy - 
 * 3. have a list of values
 * 4. 
 */
        
        /*
        // build the future game path tree based on current board configuration
        while(!isGameOver()) {
            if(emptySquares.isEmpty()){
                //the board is full -- it's a draw
                //stop expanding this path
                //utility function for score because terminal state
            }
            else {
    
                //evaluation function for score because non-terminal state
            }
        }
        */
        String moveFileInput = "Dobby " + currentBoard2 + " " + chosenMove;

        globalBoard[currentBoard2].markBoardWithMove(moveFileInput, ourMoveChar, globalBoard, on); 
       
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
        int chosenMove = -1;

        // make copies of static global objects
        Board[] globalBoardCopy = new Board[9];
        for(int j = 0; j < globalBoard.length; j++) {
            globalBoardCopy[j] = globalBoard[j].makeBoardCopy();
        }
        Board globalWinnersCopy = globalWinners.makeBoardCopy();

        LinkedList<Integer> emptySquares = gamePathTree(globalBoardCopy[board]); // empty squares on local board
        char[] currentSquares = globalBoardCopy[board].squares; // array of all squares in local board

        // bestScore = minimax(globalBoardCopy, globalWinnersCopy, board, depth, false);
        
        //start the minimax loop
        for(int i = 0; i < emptySquares.size(); i++){ 
            // System.out.println("error = " +currentSquares[emptySquares.get(i)]);
            currentSquares[emptySquares.get(i)] = 'd';
            // ourBoard.squares[i] = 'd';
            int score = minimax(globalBoardCopy, globalWinnersCopy, board, 0, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            currentSquares[emptySquares.get(i)] = ' ';
            // bestScore = Math.max(score, bestScore);
            if(score > bestScore){
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
        int chosenMove = 0;
        
        // boolean end = isGameOver(); //TODO determine ending configuration, not end of game
        //isgameover will take the passed in winners 

        if(localDepth == Dobby.depth){    // TODO consider winning positions, DEPTH, and other terminal cases
            return pointsWon(chosenMove, board);
        }

        if(isMaximizing) { //Dobby is playing
            int bestScore = Integer.MIN_VALUE;
            LinkedList<Integer> emptySquares = gamePathTree(globalBoardCopy[board]);

            for(int i = 0; i <= emptySquares.size(); i++) {
                //set i to be new board
                globalBoardCopy[board].squares[emptySquares.get(i)] = 'd';

                //make copies of globalBoard and globalBoardWinners
                Board[] globalBoardCopy2 = new Board[9];
                for(int j = 0; j < globalBoardCopy.length; j++) {
                    globalBoardCopy2[j] = globalBoardCopy[j].makeBoardCopy();
                }
                Board globalWinnersCopy2 = globalWinnersCopy.makeBoardCopy();

                //get opponent's next step score
                int score = minimax(globalBoardCopy2, globalWinnersCopy2, i, localDepth + 1, false, alpha, beta); 
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

            for(int i = 0; i <= emptySquares.size(); i++) {
                //set i to be new board
                globalBoardCopy[board].squares[i] = 'd';

                //make copies of globalBoard and globalBoardWinners
                Board[] globalBoardCopy2 = new Board[9];
                for(int j = 0; j < globalBoardCopy.length; j++) {
                    globalBoardCopy2[j] = globalBoardCopy[j].makeBoardCopy();
                }
                Board globalWinnersCopy2 = globalWinnersCopy.makeBoardCopy();

                //get Dobby's next step score
                int score = minimax(globalBoardCopy2, globalWinnersCopy2, i, localDepth + 1, true, alpha, beta); 
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
                            nextBoard = globalBoard[0].markBoardWithMove(move, ourMoveChar, globalBoard, on); //update our board with the first four moves provided   
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
    static int pointsWon(int square, int localBoard){ //TODO write scoring function in here
        int score = 0;

        // LinkedList<Integer> emptySquares = gamePathTree(square);

        // int[] points = new int[9]; //storing points for each square in terminalConfig

        // points[4] = 5; //middle square is best
        switch(square) {
            case 0:
                score = 0;
            case 1:
                score = 1;
            case 2:
                score = 2;
            case 3:
                score = 3;
            case 4:
                score = 4;
            case 5:
                score = 5;
            case 6:
                score = 6;
            case 7:
                score = 7;
            case 8:
                score = 8;

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

    //IRRELEVANT FUNCTION
    static void getFakeMoves() throws IOException {
         // ---- FOR TESTING BEFORE TRYING MINIMAX -----
         String fakeDobbyMove = "";
                
         try { //get our FAKE move
             System.out.println("waiting for the team to input a move");
             File fakeMoves = new File("FakeDobbyMoves.txt");
             Scanner scanner = new Scanner(fakeMoves);
             fakeDobbyMove = scanner.nextLine(); //get our FAKE move
             System.out.println(fakeDobbyMove); 
             new FileOutputStream(fakeMoves).close();
             scanner.close();         
           } 
           catch (FileNotFoundException e) {
             System.out.println("An error occurred while reading in our FAKE move.");
             e.printStackTrace();
           }

         globalBoard[0].markBoardWithMove(fakeDobbyMove, ourMoveChar, globalBoard, on); //mark our board with our FAKE move

          // ---- DELETE UNTIL HERE AFTER MINIMAX IS IMPLEMENTED ----

    }

    /*
     * input: localBoard (determined by last move played by opponent)
     */
    static LinkedList<Integer> gamePathTree(Board currentBoard) { //list of empty squares in input localBoard-- possible squares to play
        
        //loop to get to the end 
        //TODO: eventually implement heuristic to determine when to stop
        LinkedList<Integer> emptySquares = new LinkedList<Integer>();
        // Board currentBoard = globalBoard[localBoard];
        currentBoard.display();
        for(int i = 0; i <= 8; i++){ //discovering all empty squares in the current local board being played
            System.out.println(currentBoard.squares[i]);
            if((currentBoard.squares[i] == 'd') || (currentBoard.squares[i] == 'o')){
                
            }
            else {
                emptySquares.add(i);
            }
        }
    
        for(int i = 0; i < emptySquares.size(); i++){ //discovering all empty squares in the current local board being played
            System.out.println(emptySquares.get(i));
        }

        return emptySquares;
    }



}