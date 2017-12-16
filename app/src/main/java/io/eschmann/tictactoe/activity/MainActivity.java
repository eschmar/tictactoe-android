package io.eschmann.tictactoe.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.eschmann.tictactoe.R;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends Activity {
    String username;
    Button joinButton;
    TextView usernameInput;

    public static final String INTENT_EXTRA_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init view components
        joinButton = (Button) findViewById(R.id.joinButton);
        usernameInput = (TextView) findViewById(R.id.usernameInput);

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = usernameInput.getText().toString();
                if (username.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please enter a name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // clean up
                usernameInput.clearFocus();
                hideKeyboard(view);

                // start match
                Intent intent = new Intent(MainActivity.this, MatchActivity.class);
                intent.putExtra(INTENT_EXTRA_USERNAME, username);
                startActivity(intent);
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
}
