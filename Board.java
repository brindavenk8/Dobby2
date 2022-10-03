public class Board{
    char[] squares = new char[9];



    public void display() {
        System.out.println(squares[0] + "-" + squares[1] + "-" + squares[2]);
        System.out.println(squares[3] + "-" + squares[4] + "-" + squares[5]);
        System.out.println(squares[6] + "-" + squares[7] + "-" + squares[8]);
    }

    public char[] getSquares() {
        return this.squares;
    }

    /*
     * markBoardWithMove: to keep track of the current state of the game
     * Input: 
     *      String move: a line in either move_file or first_four_moves
     */
    int markBoardWithMove(String move, Character ourMoveChar, Board[] globalBoard, boolean on) {

        // ---- OLD REFEREE CODE ----
        // char marker = move.charAt(0); //first character is X or O
        // int i = Character.getNumericValue(move.charAt(2)); //second character is local board to make the move on
        // int j = Character.getNumericValue(move.charAt(4)); //third character is square in the local board to mark

        // V4 referee update
        String moves[] = move.split(" ");
        String player = moves[0];
        Character marker = ourMoveChar;
        int i = Character.getNumericValue(moves[1].charAt(0)); //board
        int j = Character.getNumericValue(moves[2].charAt(0)); //square
        System.out.println(player + " " + i + " " + j);
        if(!player.equals("Dobby")){
            System.out.println("player not Dobby");
            marker = 'o';
            on = false;
        }
        System.out.println("outside if");
        Board localBoard = globalBoard[i];
        
        System.out.println("defined localBoard");
        System.out.println(marker);
        localBoard.getSquares()[j] = marker; 
        System.out.println("marked board");

        localBoard.display(); //TODO make display more intuitive
        System.out.println("next board should be "+j);

        int check = globalBoard[i].isBoardAvailable();
        if(check == 1) { //check for any new local wins
            Dobby.globalWinners.squares[i] = 'd';
        }
        else if (check == 2) {
            Dobby.globalWinners.squares[i] = 'o';
        }

        return j;

    }

    /*
     * checks if a local board is available to play on (not yet won by any player)
     * returns 0 if the board is not yet won by any player
     * returns 1 if the board is won by Dobby
     * returns 2 if the board is won by the opponent
     */
    public int isBoardAvailable() {
        int available = 0;

        if((available = isBoardAvailableUtil(squares[0], squares[1], squares[2])) != 0) {
            //row win
            return available;
        }
        else if ((available = isBoardAvailableUtil(squares[3], squares[4], squares[5])) != 0) {
            //row win
            return available;
        }
        else if ((available = isBoardAvailableUtil(squares[6], squares[7], squares[8])) != 0) {
            //row win
            return available;
        }
        else if ((available = isBoardAvailableUtil(squares[0], squares[3], squares[6])) != 0) {
            //col win
            return available;
        }
        else if ((available = isBoardAvailableUtil(squares[1], squares[4], squares[7])) != 0) {
            //col win
            return available;
        }
        else if ((available = isBoardAvailableUtil(squares[2], squares[5], squares[8])) != 0) {
            //col win
            return available;
        }
        else if ((available = isBoardAvailableUtil(squares[0], squares[4], squares[8])) != 0) {
            //diag win
            return available;
        }
        else if ((available = isBoardAvailableUtil(squares[2], squares[4], squares[6])) != 0) {
            //diag win
            return available;
        }

        return available;
    }

    /*
     * utility function for isBoardAvailable
     * Input: the characters in a three-in-a-row
     * Output:
        * returns 0 if the board is not yet won by any player
        * returns 1 if the board is won by Dobby
        * returns 2 if the board is won by the opponent
     */
    public int isBoardAvailableUtil(char a, char b, char c) {
        int winner = 0;

        String sequence = new StringBuilder().append(a).append(b).append(c).toString();

        if (sequence.equals("ddd")) {
            winner = 1;
        }
        else if (sequence.equals("ooo")){
            winner = 2;
        }

        return winner;
    }

    /*
     * utility function to make a copy of a local board
     * used in minimax to manipulate future possible board configurations without altering static global variables
     */
    public Board makeBoardCopy() {
        Board copy = new Board();
        char[] squaresCopy = squares;

        for(int i = 0; i < 9; i++) {
            squaresCopy[i] = squares[i];
        }
        copy.squares = squaresCopy;

        return copy;
    }


}