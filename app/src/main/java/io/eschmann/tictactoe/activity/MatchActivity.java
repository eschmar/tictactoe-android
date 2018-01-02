package io.eschmann.tictactoe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.eschmann.tictactoe.R;
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

public class MatchActivity extends Activity {
    private String username;
    private OkHttpClient client;
    private WebSocket websocket;
    private TextView tempText;
    private TextView opponentLabel;
    private TextView scoreLabel;
    private Gson gson;

    private TicTacToeMatch ticTacToeMatch;

    //    private static final String MATCHMAKING_SERVER_URL = "ws://tic-tac-toe-lobby.herokuapp.com/connect";
    private static final String MATCHMAKING_SERVER_URL = "ws://tictactoe-temp.herokuapp.com/connect";
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    // array of references to the 9 buttons (tiles) of the game board
    int[] gameButtons = {R.id.gameButton11, R.id.gameButton12, R.id.gameButton13,
            R.id.gameButton21, R.id.gameButton22, R.id.gameButton23,
            R.id.gameButton31, R.id.gameButton32, R.id.gameButton33};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        gson = new Gson();

        // Extract username from intent
        Intent intent = getIntent();
        username = intent.getStringExtra(MainActivity.INTENT_EXTRA_USERNAME);

        // Attempt connection to server
        if (!connectToMatchmakingServer()) {
            Toast.makeText(getApplicationContext(), "Unable to connect to matchmaking server.", Toast.LENGTH_LONG).show();
            finish();
        }

        // set up view components
        tempText = (TextView) findViewById(R.id.logInput);
        opponentLabel = (TextView) findViewById(R.id.opponentLabel);
        scoreLabel = (TextView) findViewById(R.id.scoreLabel);

        setupBoardButtons();

        //
        //
        //  TODO: AsyncTask
        //
        //
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(getApplicationContext(), "MatchActivity got destroyed", Toast.LENGTH_SHORT).show();
        if (websocket != null) {
            websocket.close(NORMAL_CLOSURE_STATUS, "Quit.");
        }

        super.onDestroy();
    }

    private void setupBoardButtons() {
        for (int i = 0; i < gameButtons.length; i++) {
            final Button tile = (Button) findViewById(gameButtons[i]);
            final int position = i;

            tile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // make the move in the TicTacToeMatch object at given position.
                    // if true is returned the player has won the game
                    boolean playerWon = ticTacToeMatch.playerMakeMove(position / 3, position % 3);

                    // mark position by setting text and disabling button
                    tile.setText(TicTacToeMatch.GAME_PLAYER_MARKER);
                    tile.setEnabled(false);

                    // disable all untouched buttons since it is opponent's turn
                    disableButtonsWithPattern(ticTacToeMatch.getState());

                    // send the move as a message to the opponent
                    Message move = new Message(Message.TYPE_MOVE, String.valueOf(position));
                    websocket.send(gson.toJson(move));

                    if (playerWon) {
                        toast("You won the match!");
                        scoreLabel.setText(String.valueOf(ticTacToeMatch.getScore()));
                        clearAllButtons();
                        enableAllButtons();
                    }
                }
            });
        }
    }

    private final class MatchWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output("Receiving : " + text);

            try {
                handleMessage(gson.fromJson(text, Message.class));
            } catch (IllegalStateException e) {
                Log.i(MatchActivity.class.toString(), "Not a gson obj.");
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(MatchActivity.NORMAL_CLOSURE_STATUS, reason);
            toast("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
//            Log.e(MatchActivity.class, t.getMessage());
//            Log.e(MatchActivity.class.toString(), t.getStackTrace().toString());
            toast("Error : " + t.getMessage());
        }
    }

    private boolean connectToMatchmakingServer() {
        // configure client to have no timeouts
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MINUTES)
                .connectTimeout(0, TimeUnit.MINUTES)
                .writeTimeout(0, TimeUnit.MINUTES)
                .build();

        Request request = new Request.Builder().url(MATCHMAKING_SERVER_URL).build();
        MatchWebSocketListener listener = new MatchWebSocketListener();
        websocket = client.newWebSocket(request, listener);

        // send start message
        Message temp = new Message(Message.TYPE_START, this.username);
        websocket.send(gson.toJson(temp));

        client.dispatcher().executorService().shutdown();
        return true;
    }

    private void output(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempText.setText(tempText.getText().toString() + "\n" + txt);
            }
        });
    }

    private void handleMessage(final Message message) {
        if (message.getType().equals(Message.TYPE_START)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    opponentLabel.setText("Opponent: " + message.getPayload());
                    scoreLabel.setText("Score: 0");
                    findViewById(R.id.loadingScreen).setVisibility(View.GONE);
                    findViewById(R.id.matchView).setVisibility(View.VISIBLE);
                    ticTacToeMatch = new TicTacToeMatch(message.getPayload());
                }
            });
        } else if (message.getType().equals(Message.TYPE_ACTOR_PATH)) {
            websocket.send(gson.toJson(message));
        } else if (message.getType().equals(Message.TYPE_MOVE)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int position = Integer.parseInt(message.getPayload());

                    // make the move in the TicTacToeMatch object at given position.
                    // if true is returned the opponent has won the game
                    boolean opponentWon = ticTacToeMatch.opponentMakeMove(position / 3, position % 3);

                    // mark position by setting text and disabling button
                    final Button tile = (Button) findViewById(gameButtons[position]);
                    tile.setText(TicTacToeMatch.GAME_OPPONENT_MARKER);
                    tile.setEnabled(false);

                    // enable all untouched buttons since the opponent made its move
                    enableButtonsWithPattern(ticTacToeMatch.getState());

                    if (opponentWon) {
                        toast("Your opponent won the match!");
                        clearAllButtons();
                        enableAllButtons();
                    }
                }
            });
        }
    }

    private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /* Enable all 9 buttons */
    private void enableAllButtons() {
        setButtonsEnabled(true, new String[]{"X","X","X","X","X","X","X","X","X"});
    }

    /* Disable buttons according to an array of 9 strings. */
    private void disableButtonsWithPattern(String[] pattern) {
        setButtonsEnabled(false, pattern);
    }

    /* Enable buttons according to an array of 9 strings. */
    private void enableButtonsWithPattern(String[] pattern) {
        setButtonsEnabled(true, pattern);
    }

    /*
    Set 'enabled' property of buttons according to an array of 9 strings.
    If the string is empty the button property is changed.
    */
    private void setButtonsEnabled(boolean enabled, String[] pattern) {
        for (int i = 0; i < gameButtons.length; i++) {
            if (pattern[i].equals("")) findViewById(gameButtons[i]).setEnabled(enabled);
        }
    }

    /* Clear the text of all 9 buttons */
    private void clearAllButtons() {
        for (int gameButton : gameButtons) {
            final Button tile = (Button) findViewById(gameButton);
            tile.setText("");
        }
    }
}
