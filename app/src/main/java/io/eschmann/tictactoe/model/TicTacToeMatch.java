package io.eschmann.tictactoe.model;

import java.util.Arrays;

/**
 * Created by marcel on 2017-12-16.
 */

public class TicTacToeMatch {
    private int score = 0;
    private Boolean madeFirstMove;
    private int opponentScore = 0;
    private String opponent = "Unknown";
    private String[] state;

    public static final int GAME_SIZE = 3;
    public static final String GAME_PLAYER_MARKER = "X";
    public static final String GAME_OPPONENT_MARKER = "O";

    public static final String NOTIFICATION_WON = "You won!";
    public static final String NOTIFICATION_TIE = "It's a tie.";
    public static final String NOTIFICATION_LOST = "You lost.";

    public TicTacToeMatch(String opponentName) {
        this.state = new String[9];
        this.opponent = opponentName;
        resetGame();
    }

    public GameState makeMove(int row, int col, String marker) throws IllegalArgumentException {
        if (madeFirstMove == null) {
            madeFirstMove = marker.equals(GAME_PLAYER_MARKER) ? true : false;
        }

        int pos = coordToPos(row, col);
        if (!this.state[pos].equals("")) {
            throw new IllegalArgumentException("Illegal move detected!");
        }

        this.state[pos] = marker;
        return checkGameState(row, col, marker);
    }

    private boolean orderOfMovesIsValid() {
        int ownMoves = 0;
        int opponentMoves = 0;

        for (String field : this.state) {
            if (field.equals(GAME_PLAYER_MARKER)) ownMoves++;
            else if (field.equals(GAME_OPPONENT_MARKER)) opponentMoves++;
        }

        if (ownMoves == opponentMoves) return true;
        if (madeFirstMove && ownMoves > opponentMoves && ownMoves - opponentMoves == 1) return true;
        if (!madeFirstMove && opponentMoves > ownMoves && opponentMoves - ownMoves == 1) return true;

        return false;
    }

    private GameState checkGameState(int row, int col, String marker) throws IllegalArgumentException {
        if (!orderOfMovesIsValid()) {
            throw new IllegalArgumentException("Illegal move detected!");
        }

        // check row for win
        for (int i = 0; i < GAME_SIZE; i++) {
            if (!this.state[coordToPos(row, i)].equals(marker)) break;
            if (i == GAME_SIZE - 1) {
                resetGameAfterRound(marker);
                return marker.equals(GAME_OPPONENT_MARKER) ? GameState.LOST : GameState.WON;
            }
        }

        // check column for win
        for (int i = 0; i < GAME_SIZE; i++) {
            if (!this.state[coordToPos(i, col)].equals(marker)) break;
            if (i == GAME_SIZE - 1) {
                resetGameAfterRound(marker);
                return marker.equals(GAME_OPPONENT_MARKER) ? GameState.LOST : GameState.WON;
            }
        }

        // check diagonal
        if (row == col) {
            for (int i = 0; i < GAME_SIZE; i++) {
                if (!this.state[coordToPos(i, i)].equals(marker)) break;
                if (i == GAME_SIZE - 1) {
                    resetGameAfterRound(marker);
                    return marker.equals(GAME_OPPONENT_MARKER) ? GameState.LOST : GameState.WON;
                }
            }
        }

        //check anti diagonal
        if (row + col == GAME_SIZE - 1) {
            for (int i = 0; i < GAME_SIZE; i++) {
                if (!this.state[coordToPos(i, GAME_SIZE - 1 - i)].equals(marker)) break;
                if (i == GAME_SIZE - 1) {
                    resetGameAfterRound(marker);
                    return marker.equals(GAME_OPPONENT_MARKER) ? GameState.LOST : GameState.WON;
                }
            }
        }

        // check if moves are left to be made
        for (int i = 0; i < this.state.length; i++) {
            if (this.state[i].equals("")) {
                return GameState.UNFINISHED;
            }
        }

        // it's a tie, reset fields
        resetGame();
        return GameState.TIE;
    }

    private void resetGame() {
        Arrays.fill(this.state, "");
        madeFirstMove = null;
    }

    private void resetGameAfterRound(String winner) {
        if (winner.equals(GAME_PLAYER_MARKER)) this.score++;
        else this.opponentScore++;
        resetGame();
    }

    private int coordToPos(int row, int col) {
        return 3 * row + col;
    }

    public int getScore() {
        return score;
    }

    public int getOpponentScore() {
        return opponentScore;
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
