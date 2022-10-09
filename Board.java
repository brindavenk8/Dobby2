public class Board{
    char[] squares = new char[9];


    /*
     * utility function for debugging purposes
     * display the AI's version of any local Board
     */
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
    static int markBoardWithMove(String move, Character ourMoveChar, Board[] globalBoardCopy, Board globalWinnersCopy) {

        String moves[] = move.split(" ");
        String player = moves[0];
        Character marker = ourMoveChar;
        int i = Character.getNumericValue(moves[1].charAt(0)); //board
        int j = Character.getNumericValue(moves[2].charAt(0)); //square
        System.out.println(player + " " + i + " " + j);
        /*
        if(!player.equals("Dobby")){
            System.out.println("player not Dobby");
            marker = 'o';
        }
        */

        if(!player.equals(Dobby.aiPlayerName)) {
            System.out.println("Player is not " + Dobby.aiPlayerName);
            marker = 'o';
        }

        System.out.println("outside if");
        Board localBoard = globalBoardCopy[i];
        
        System.out.println("defined localBoard");
        System.out.println(marker);
        localBoard.getSquares()[j] = marker; 
        System.out.println("marked board");

        localBoard.display(); 
        System.out.println("next board should be "+j);

        int check = globalBoardCopy[i].isBoardAvailable();
        //check for any new local wins
        if(check == 1) { // AI won
            globalWinnersCopy.squares[i] = 'd';
        }
        else if (check == 2) { // opponent won
            globalWinnersCopy.squares[i] = 'o';
        }
        else if (check == 3) { // draw
            globalWinnersCopy.squares[i] = 'w';
        }

        return j;

    }

    /*
     * checks if a local board is available to play on (not yet won by any player)
     * returns 0 if the board is not yet won by any player
     * returns 1 if the board is won by Dobby
     * returns 2 if the board is won by the opponent
     * returns 3 if the board was a draw
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
        else if (Dobby.gamePathTree(this).isEmpty()) {
            // have not found a win and there are no empty squares
            available = 3;
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