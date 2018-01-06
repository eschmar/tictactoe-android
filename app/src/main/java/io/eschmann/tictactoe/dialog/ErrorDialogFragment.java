package io.eschmann.tictactoe.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import io.eschmann.tictactoe.R;

/**
 * Created by marcel on 2018-01-02.
 */

public class ErrorDialogFragment extends DialogFragment {
    public interface ErrorDialogListener {
        void onErrorDialogDismiss(DialogFragment dialog);
    }

    public ErrorDialogListener dialogListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        dialogListener = (ErrorDialogListener) getActivity();

        // default error messages
        String title = getString(R.string.dialog_error_default_title);
        String message = getString(R.string.dialog_error_default_message);

        // use specific error messages if available
        if (getArguments().containsKey("responseCode")) {
            switch (getArguments().getInt("responseCode")) {
                case 503:
                    title = getString(R.string.dialog_error_503_title);
                    message = getString(R.string.dialog_error_503_message);
                    break;
            }
        }

        builder.setTitle(title).setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogListener.onErrorDialogDismiss(ErrorDialogFragment.this);
            }
        });

        return builder.create();
    }
}