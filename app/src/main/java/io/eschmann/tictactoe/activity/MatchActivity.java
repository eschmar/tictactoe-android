package io.eschmann.tictactoe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import io.eschmann.tictactoe.R;
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
    private static final String MATCHMAKING_SERVER_URL = "ws://tic-tac-toe-lobby.herokuapp.com/connect";
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        // Extract username from intent
        Intent intent = getIntent();
        username = intent.getStringExtra(MainActivity.INTENT_EXTRA_USERNAME);

        // Attempt connection to server
        if (!connectToMatchmakingServer()) {
            Toast.makeText(getApplicationContext(), "Unable to connect to matchmaking server.", Toast.LENGTH_LONG).show();
            finish();
        }

        // set up game
        findViewById(R.id.loadingScreen).setVisibility(View.GONE);
        findViewById(R.id.matchView).setVisibility(View.VISIBLE);

        tempText = (TextView) findViewById(R.id.logInput);
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(getApplicationContext(), "MatchActivity got destroyed", Toast.LENGTH_SHORT).show();
        if (websocket != null) {
            websocket.close(NORMAL_CLOSURE_STATUS, "Quit.");
        }

        super.onDestroy();
    }

    private final class MatchWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output("Receiving : " + text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(MatchActivity.NORMAL_CLOSURE_STATUS, null);
            toast("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            toast("Error : " + t.getMessage());
        }
    }

    private boolean connectToMatchmakingServer() {
        client = new OkHttpClient();
        Request request = new Request.Builder().url(MATCHMAKING_SERVER_URL).build();
        MatchWebSocketListener listener = new MatchWebSocketListener();
        websocket = client.newWebSocket(request, listener);
        websocket.send("Send a text.");
        client.dispatcher().executorService().shutdown();
        return true;
    }

    private void output(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempText.setText(tempText.getText().toString() + "\n\n" + txt);
            }
        });
    }

    private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
