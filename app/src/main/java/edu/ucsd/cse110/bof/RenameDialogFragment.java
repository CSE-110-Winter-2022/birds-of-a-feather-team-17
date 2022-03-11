package edu.ucsd.cse110.bof;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.DialogFragment;

public class RenameDialogFragment extends DialogFragment {
    private View view;

    public View getView() {
        return this.view;
    }

    public interface renameDialogListener {
        void onDialogConfirmed(RenameDialogFragment dialog);
        void onDialogCanceled();
    }

    renameDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        this.view = inflater.inflate(R.layout.dialog, null);

        builder.setTitle("Name this session: ")
                .setView(this.view)
                .setPositiveButton("Confirm", (dialog, id) -> {
                    // Send the positive button event back to the host activity
                    listener.onDialogConfirmed(RenameDialogFragment.this);
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        listener.onDialogCanceled();
        Log.d("Dialog: ", "Canceled");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (renameDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}