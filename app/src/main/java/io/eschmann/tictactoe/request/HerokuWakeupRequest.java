package io.eschmann.tictactoe.request;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by marcel on 2018-01-27.
 *
 * Attempt to wake up free heroku dyno.
 */
public class HerokuWakeupRequest {
    private final OkHttpClient client = new OkHttpClient();
    private static final String SERVER_URL = "https://tic-tac-toe-lobby.herokuapp.com";

    public void run() {
        Request request = new Request.Builder()
                .url(SERVER_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                // e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                // unused
            }
        });
    }
}
