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
    static boolean exitGracefully = false;

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

        Thread thread = new Thread(){
            public void run() {
                try {
                    sleep(9000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Dobby.exitGracefully = true;
            }
        };
        thread.start();

        int chosenMove = 0; // square in local board -- we will pick this through minimax with alpha-beta pruning
        int currentBoard2 = currentBoard; 
        int bestScore = Integer.MIN_VALUE;

        // if currentBoard is won, pick a new local board to play in
        if(globalWinners.squares[currentBoard] == 'd') { //board was won by Dobby -- need to pick a different board to play in
            System.out.println("Dobby won currentBoard = "+currentBoard+", choosing currentBoard2");
            currentBoard2 = gamePathTree(globalWinners).getFirst(); //TODO: discuss how to pick this
            System.out.println("currentBoard2 = "+currentBoard2);
        }
        else if(globalWinners.squares[currentBoard] == 'o') { //board was won by opponent -- need to pick a different board to play in
            System.out.println("opponnent won currentBoard = "+currentBoard+", choosing currentBoard2");
            currentBoard2 = gamePathTree(globalWinners).getFirst(); //TODO: discuss how to pick this
            System.out.println("currentBoard2 = "+currentBoard2);
        }
        else { //board is not won by either player
            System.out.println("current board has not yet been won (if(winner == 0))");
        } 

        //get and store move from minimax
        chosenMove = minimaxUtil(currentBoard2, bestScore);
        String moveFileInput = "Dobby " + currentBoard2 + " " + chosenMove;
        Board.markBoardWithMove(moveFileInput, ourMoveChar, globalBoard, globalWinners); 
           
        //write move to referee
        try {
            Path movePath = Paths.get("move_file");
            Files.write(movePath, moveFileInput.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
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
        


        //start the minimax loop
        for(int i = 0; i < emptySquares.size(); i++){ 
            currentSquares[emptySquares.get(i)] = 'd';
            int score = minimax(globalBoardCopy, globalWinnersCopy, board, 0, false, 0, 10);
            currentSquares[emptySquares.get(i)] = ' ';
            if(score > bestScore){
                bestScore = score;
                chosenMove = emptySquares.get(i); //create a parallel linkedlist to emptySquares of scores
            } 
        }

        //TODO choose a score from parallel list -- based on heuristic function to minimize tree expansion
        //is there a strategy to send the opponent to a particular local board?
        
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
        
        if(localDepth == Dobby.depth || exitGracefully){ 
            exitGracefully = false;   
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
                int score = minimax(globalBoardCopy2, globalWinnersCopy2, test, localDepth + 1, true, alpha, beta); 
                
                //reset copy of board
                globalBoardCopy2[board].squares[emptySquares.get(i)] = ' ';

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
     * pointsWon: utility function for determining number of points won by current player
     * Input: 
     *      Board terminalConfig: terminal board configuration
     * Output:
     *      int points: number of points won by current player
     */
    static int pointsWon(Board[] globalBoardCopy){ //TODO write scoring function in here
        int score = 0;

        int[] boardsWon = new int[globalBoardCopy.length];
        for (int i = 0; i < globalBoardCopy.length; i++) {
            globalBoardCopy[i].display();
            boardsWon[i] = globalBoardCopy[i].isBoardAvailable();
        }

        int[] row1 = new int[3]; //{0,1,2}
        for (int i = 0; i < 3; i++) {
            row1[i] = boardsWon[i];
            System.out.println(row1);
        }
        score += countdo(row1);

        int row2index = 0;
        int[] row2 = new int[3];  //{3,4,5}
        for (int i = 3; i < 6; i++) {
            row2[row2index] = boardsWon[i];
            row2index++;
        }
        score += countdo(row2);
        
        int row3index = 0;
        int[] row3 = new int[3];  //{6,7,8}
        for (int i = 6; i < 9; i++) {
            row3[row3index] = boardsWon[i];
            row3index++;
        }
        score += countdo(row3);

        //calculate column scores

        int col1index = 0;
        int[] col1 = new int[3];  //{0,3,6}
        for (int i = 0; i < 7; i += 3) {
            col1[col1index] = boardsWon[i];
            col1index++;
        }
        score += countdo(col1);

        int col2index = 0;
        int[] col2 = new int[3];  //{1,4,7}
        for (int i = 1; i < 8; i += 3) {
            col2[col2index] = boardsWon[i];
            col2index++;
        }
        score += countdo(col2);

        int col3index = 0;
        int[] col3 = new int[3];  //{2,5,8}
        for (int i = 2; i < 9; i += 3) {
            col3[col3index] = boardsWon[i];
            col3index++;
        }
        score += countdo(col3);


        //calculate diagonal scores

        int diag1index = 0;
        int[] diag1 = new int[3]; //{0,4,8}
        for (int i = 0; i < 9; i+= 4) {
            diag1[diag1index] = boardsWon[i];
            diag1index++;
        }
        score += countdo(diag1);

        int diag2index = 0;
        int[] diag2 = new int[3];  //{2,4,6}
        for (int i = 2; i < 7; i+= 2) {
            diag2[diag2index] = boardsWon[i];
            diag2index++;
        }
        score += countdo(diag2);


        return score;
    }

    /*
     * countdo: utility function for assigning a score to each row, column and diagonal in the global board
     */
    //      returns 0 if the board is not yet won by any player
    //      returns 1 if the board is won by Dobby
    //      returns 2 if the board is won by the opponent

    static int countdo(int[] boardsWon){
        int dcount = 0;
        int ocount = 0;
        int score = 0;


        for (int i = 0; i<boardsWon.length; i++){
            if (boardsWon[i] == 1){
                dcount +=5;
            }
            else if (boardsWon[i] == 2){
                ocount -=5;
            }
        }

        if (ocount == 0){
            score = dcount;
        }

        else if (dcount == 0){
            score = ocount;

        }

        return score;
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
        if (end) {
            System.out.println("Game has ended");
        }
        return end;
    }

    /*
     * input: localBoard (determined by last move played by opponent)
     */
    static LinkedList<Integer> gamePathTree(Board currentBoard) { //list of empty squares in input localBoard-- possible squares to play
        LinkedList<Integer> emptySquares = new LinkedList<Integer>();
        System.out.println("displaying currentBoard = " + currentBoard);
        currentBoard.display();
        System.out.println("-----------------");
        
        //printing for debugging purposes
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