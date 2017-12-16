package io.eschmann.tictactoe;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    String username;
    Button joinButton;
    TextView usernameInput;

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

                findViewById(R.id.usernameScreen).setVisibility(View.GONE);
                findViewById(R.id.loadingScreen).setVisibility(View.VISIBLE);
            }
        });
    }
}
