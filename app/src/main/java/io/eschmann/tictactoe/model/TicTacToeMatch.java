package io.eschmann.tictactoe.model;

/**
 * Created by marcel on 2017-12-16.
 */

public class TicTacToeMatch {
    private int score = 0;
    private String opponent = "Unknown";
    private String[] state;

    public static final String GAME_PLAYER_MARKER = "X";
    public static final String GAME_OPPONENT_MARKER = "O";

    public void opponentMakeMove(int row, int col) {
        makeMove(row, col, GAME_OPPONENT_MARKER);
    }

    public void playerMakeMove(int row, int col) {
        makeMove(row, col, GAME_PLAYER_MARKER);
    }

    private void makeMove(int row, int col, String marker) {
        int pos = coordToPos(row, col);
        if (!this.state[pos].equals("")) throw new IllegalStateException("Illegal move detected!");

        this.state[pos] = marker;
        checkGameState();
    }

    private void checkGameState() {
        // check if game was won
    }

    private int coordToPos(int row, int col) {
        return 3 * (row - 1) + col;
    }

    public TicTacToeMatch() {
        this.state = new String[9];
    }

    public int getScore() {
        return score;
    }

    public String getOpponent() {
        return opponent;
    }

    public String[] getState() {
        return state;
    }

    public String getState(int row, int col) {
        return this.state[coordToPos(row, col)];
    }
}
