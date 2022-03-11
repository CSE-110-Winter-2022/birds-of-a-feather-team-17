package edu.ucsd.cse110.bof.homepage;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Session;

public class SessionsViewAdapter extends RecyclerView.Adapter<SessionsViewAdapter.ViewHolder> {

    private static final String TAG = "SessionsViewAdapterLog";

    private final List<Session> sessions;
    private final AppDatabase db;

    public SessionsViewAdapter(List<Session> sessions, AppDatabase db) {
        super();
        this.db = db;
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public SessionsViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.saved_session_row, parent, false);

        return new ViewHolder(view, this.db);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionsViewAdapter.ViewHolder holder, int position) {
        holder.setSession(sessions.get(position));
    }

    @Override
    public int getItemCount() {
        return this.sessions.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView sessionNameView;
        private final Button renameButton;
        private final AppDatabase db;
        private Session session;

        public ViewHolder(View itemView, AppDatabase db) {
            super(itemView);
            this.db = db;
            this.sessionNameView = itemView.findViewById(R.id.session_info);
            this.renameButton = itemView.findViewById(R.id.rename_button);
            this.renameButton.setOnClickListener(view -> {
                this.session.setDispName(sessionNameView.getText().toString());
                this.db.sessionsDao().updateDispName(this.session.getSessionID(), sessionNameView.getText().toString());
            });
            itemView.setOnClickListener(this);
        }

        public void setSession(Session session){
            this.session = session;
            this.sessionNameView.setText(session.toString());
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            Intent intent = new Intent(context, SessionDetailActivity.class);

            int session_id = this.session.getSessionID();

            intent.putExtra("session_id", this.session.getSessionID());
            context.startActivity(intent);
        }
    }

}
