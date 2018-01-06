package io.eschmann.tictactoe.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eschmann.tictactoe.R;
import io.eschmann.tictactoe.dialog.ErrorDialogFragment;
import io.eschmann.tictactoe.model.GameState;
import io.eschmann.tictactoe.model.Message;
import io.eschmann.tictactoe.model.TicTacToeMatch;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by marcel on 2017-12-16.
 */

public class MatchActivity extends Activity implements ErrorDialogFragment.ErrorDialogListener {
    private OkHttpClient client;
    private WebSocket websocket;
    private Gson gson;

    private static final String LOG_TAG = MatchActivity.class.toString();
    private static final String LOG_TAG_WEBSOCKET = MatchActivity.MatchWebSocketListener.class.toString();
    private static final String MATCHMAKING_SERVER_URL = "ws://tic-tac-toe-lobby.herokuapp.com/connect";
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    @BindView(R.id.playerLabel) TextView playerLabel;
    @BindView(R.id.playerScoreLabel) TextView playerScoreLabel;
    @BindView(R.id.opponentLabel) TextView opponentLabel;
    @BindView(R.id.opponentScoreLabel) TextView opponentScoreLabel;

    private TicTacToeMatch ticTacToeMatch;
    final Integer[] gameButtons = {
            R.id.gameButton11, R.id.gameButton12, R.id.gameButton13,
            R.id.gameButton21, R.id.gameButton22, R.id.gameButton23,
            R.id.gameButton31, R.id.gameButton32, R.id.gameButton33
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        ButterKnife.bind(this);
        gson = new Gson();

        // Extract username from intent
        Intent intent = getIntent();
        playerLabel.setText(intent.getStringExtra(MainActivity.INTENT_EXTRA_USERNAME));

        // Attempt connection to server
        if (!connectToMatchmakingServer()) {
            Toast.makeText(getApplicationContext(), "Unable to connect to matchmaking server.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onDestroy();
    }

    @Override
    protected void onDestroy() {
        quitGame(null);
        super.onDestroy();
        finish();
    }

    /**
     * Inform opponent about quitting and close game session.
     * @param reason
     */
    protected void quitGame(String reason) {
        if (websocket == null) return;

        // inform opponent about quitting
        Message quit = new Message(Message.TYPE_QUIT);
        websocket.send(gson.toJson(quit));

        // close socket
        if (websocket == null) return;
        websocket.close(NORMAL_CLOSURE_STATUS, reason == null ? "Quit game." : reason);
    }

    @Override
    public void onErrorDialogDismiss(DialogFragment dialog) {
        onDestroy();
    }

    /**
     * Establishes websocket connection to server.
     * @return
     */
    private boolean connectToMatchmakingServer() {
        // configure client to have no timeouts
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MINUTES)
                .connectTimeout(0, TimeUnit.MINUTES)
                .writeTimeout(0, TimeUnit.MINUTES)
                .pingInterval(30, TimeUnit.SECONDS)
                .build();

        // open new websocket
        Request request = new Request.Builder().url(MATCHMAKING_SERVER_URL).build();
        websocket = client.newWebSocket(request, new MatchWebSocketListener());

        // send start message
        Message temp = new Message(Message.TYPE_START, this.playerLabel.getText().toString());
        websocket.send(gson.toJson(temp));

        client.dispatcher().executorService().shutdown();
        return true;
    }

    /**
     * Updates view to reflect latest move.
     * @param position
     * @param marker
     * @param latestOutcome
     */
    protected void updateGameView(int position, String marker, GameState latestOutcome) {
        final Button tile = findViewById(gameButtons[position]);
        boolean isPlayerMove = marker.equals(TicTacToeMatch.GAME_PLAYER_MARKER);

        // mark position by setting text and disabling button
        tile.setEnabled(false);
        tile.setText(marker);

        int colorCode = isPlayerMove ? getColor(R.color.playerColor) : getColor(R.color.opponentColor);
        tile.getBackground().setColorFilter(colorCode, PorterDuff.Mode.MULTIPLY);


        if (isPlayerMove) {
            // disable all untouched buttons since it is opponent's turn
            disableButtonsWithPattern(ticTacToeMatch.getState());
        } else {
            // enable all untouched buttons since the opponent made its move
            enableButtonsWithPattern(ticTacToeMatch.getState());
        }

        if (latestOutcome.equals(GameState.UNFINISHED)) return;

        // show game ending notification
        switch (latestOutcome) {
            case WON:
                toast(TicTacToeMatch.NOTIFICATION_WON);
                break;
            case TIE:
                toast(TicTacToeMatch.NOTIFICATION_TIE);
                break;
            case LOST:
                toast(TicTacToeMatch.NOTIFICATION_LOST);
                break;
        }

        // update scores
        opponentScoreLabel.setText(String.valueOf(ticTacToeMatch.getOpponentScore()));
        playerScoreLabel.setText(String.valueOf(ticTacToeMatch.getScore()));

        // reset buttons
        clearAllButtons();
        enableAllButtons();
    }

    /**
     * Move button handler.
     * @param view
     */
    public void onPlayButtonClick(View view) {
        int position = Arrays.asList(gameButtons).indexOf(view.getId());
        final Button tile = findViewById(view.getId());

        GameState result = ticTacToeMatch.makeMove(position / 3, position % 3, TicTacToeMatch.GAME_PLAYER_MARKER);
        updateGameView(position, TicTacToeMatch.GAME_PLAYER_MARKER, result);

        // send the move as a message to the opponent
        Message move = new Message(Message.TYPE_MOVE, String.valueOf(position));
        websocket.send(gson.toJson(move));
    }

    /**
     * Displays a toast message.
     * @param message
     */
    private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Enable all 9 buttons
     */
    private void enableAllButtons() {
        setButtonsEnabled(true, new String[]{"X","X","X","X","X","X","X","X","X"});
    }

    /**
     * Disable buttons according to an array of 9 strings.
     * @param pattern
     */
    private void disableButtonsWithPattern(String[] pattern) {
        setButtonsEnabled(false, pattern);
    }

    /**
     * Enable buttons according to an array of 9 strings.
     * @param pattern
     */
    private void enableButtonsWithPattern(String[] pattern) {
        setButtonsEnabled(true, pattern);
    }

    /**
     * Set 'enabled' property of buttons according to an array of 9 strings.
     * If the string is empty the button property is changed.
     *
     * @param enabled
     * @param pattern
     */
    private void setButtonsEnabled(boolean enabled, String[] pattern) {
        for (int i = 0; i < gameButtons.length; i++) {
            if (pattern[i].equals("")) findViewById(gameButtons[i]).setEnabled(enabled);
        }
    }

    /**
     * Clear the text of all 9 buttons
     */
    private void clearAllButtons() {
        for (int gameButton : gameButtons) {
            final Button tile = findViewById(gameButton);
            tile.getBackground().clearColorFilter();
            tile.setText("");
        }
    }

    /**
     * Websocket message handler.
     * @param message
     */
    private void handleMessage(final Message message) {
        switch (message.getType()) {
            case Message.TYPE_START:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // show opponent name
                        opponentLabel.setText(message.getPayload());
                        opponentScoreLabel.setText("0");

                        // show game view
                        findViewById(R.id.loadingScreen).setVisibility(View.GONE);
                        findViewById(R.id.gameGrid).setVisibility(View.VISIBLE);
                        findViewById(R.id.opponentBar).setVisibility(View.VISIBLE);
                        findViewById(R.id.playerBar).setVisibility(View.VISIBLE);
                        ticTacToeMatch = new TicTacToeMatch(message.getPayload());
                    }
                });
                break;
            case Message.TYPE_ACTOR_PATH:
                websocket.send(gson.toJson(message));
                break;
            case Message.TYPE_MOVE:
                final int position = Integer.parseInt(message.getPayload());
                final GameState result = ticTacToeMatch.makeMove(position / 3, position % 3, TicTacToeMatch.GAME_OPPONENT_MARKER);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateGameView(position, TicTacToeMatch.GAME_OPPONENT_MARKER, result);
                    }
                });
                break;
            case Message.TYPE_SERVER_ABORT:
                throw new IllegalArgumentException("Server detected illegal messages.");
            case Message.TYPE_QUIT:
                toast("Opponent just left the game!");
                this.onDestroy();
                break;
        }
    }

    /**
     * Websocket handler
     */
    private final class MatchWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            // connected to server, waiting for player.
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.i(LOG_TAG_WEBSOCKET, "Receiving : " + text);
            handleMessage(gson.fromJson(text, Message.class));
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Log.i(LOG_TAG_WEBSOCKET, "Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            quitGame(reason);
            Log.i(LOG_TAG_WEBSOCKET, "Closing : " + code + " / " + reason);

            websocket = null;
            onDestroy();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            // write to log
            if (t.getMessage() != null) Log.e(LOG_TAG_WEBSOCKET, t.getMessage());
            if (Log.getStackTraceString(t) != null) Log.e(LOG_TAG_WEBSOCKET, Log.getStackTraceString(t));

            // detect error
            Bundle dialogArgs = new Bundle();
            if (response != null && !response.isSuccessful()) {
                dialogArgs.putInt("responseCode", response.code());
            }else if (t instanceof IllegalArgumentException) {
                dialogArgs.putInt("exception", ErrorDialogFragment.EXCEPTION_ILLEGAL);
            }

            // inform user about failure
            ErrorDialogFragment errorDialog = new ErrorDialogFragment();
            errorDialog.setArguments(dialogArgs);
            errorDialog.show(getFragmentManager(), "errorDialog");
        }
    }
}
