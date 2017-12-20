package io.eschmann.tictactoe.model;

import java.util.Arrays;

/**
 * Created by marcel on 2017-12-16.
 */

public class TicTacToeMatch {
    private int score = 0;
    private String opponent = "Unknown";
    private String[] state;

    public static final int GAME_SIZE = 3;
    public static final String GAME_PLAYER_MARKER = "X";
    public static final String GAME_OPPONENT_MARKER = "O";

    public TicTacToeMatch(String opponentName) {
        this.state = new String[9];
        Arrays.fill(this.state, "");
        this.opponent = opponentName;
    }

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
        checkGameState(row, col, marker);
    }

    private void checkGameState(int row, int col, String marker) {
        // check row for win
        for (int i = 0; i < GAME_SIZE; i++) {
            if (!this.state[coordToPos(row, i)].equals(marker)) break;
            if (i == GAME_SIZE - 1) {
                resetGameAfterWin(marker);
                return;
            }
        }

        // check column for win
        for (int i = 0; i < GAME_SIZE; i++) {
            if (!this.state[coordToPos(i, col)].equals(marker)) break;
            if (i == GAME_SIZE - 1) {
                resetGameAfterWin(marker);
                return;
            }
        }

        // check diagonal
        if (row == col) {
            for (int i = 0; i < GAME_SIZE; i++) {
                if (!this.state[coordToPos(i, i)].equals(marker)) break;
                if (i == GAME_SIZE - 1) {
                    resetGameAfterWin(marker);
                    return;
                }
            }
        }

        //check anti diagonal
        if (row + col == GAME_SIZE - 1) {
            for (int i = 0; i < GAME_SIZE; i++) {
                if (!this.state[coordToPos(i, GAME_SIZE - 1 - i)].equals(marker)) break;
                if (i == GAME_SIZE - 1) {
                    resetGameAfterWin(marker);
                    return;
                }
            }
        }
    }

    private void resetGameAfterWin(String winner) {
        if (winner.equals(GAME_PLAYER_MARKER)) this.score++;
        this.state = new String[9];
    }

    private int coordToPos(int row, int col) {
        return 3 * row + col;
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
