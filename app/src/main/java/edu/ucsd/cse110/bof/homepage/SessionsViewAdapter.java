package edu.ucsd.cse110.bof.homepage;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.db.Session;

public class SessionsViewAdapter extends RecyclerView.Adapter<SessionsViewAdapter.ViewHolder> {

    private static final String TAG = "SessionsViewAdapterLog";

    private final List<Session> sessions;

    public SessionsViewAdapter(List<Session> sessions) {
        super();
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public SessionsViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.saved_session_row, parent, false);

        return new ViewHolder(view);
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
        private Session session;

        public ViewHolder(View itemView) {
            super(itemView);
            this.sessionNameView = itemView.findViewById(R.id.session_info);
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

            intent.putExtra("session_id", this.session.getSessionID());
            context.startActivity(intent);
        }
    }

}
