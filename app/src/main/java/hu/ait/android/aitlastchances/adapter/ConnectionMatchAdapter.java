package hu.ait.android.aitlastchances.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import hu.ait.android.aitlastchances.R;
import hu.ait.android.aitlastchances.data.ConnectionMatch;


public class ConnectionMatchAdapter extends RecyclerView.Adapter<ConnectionMatchAdapter.ViewHolder>{

    private Context context;
    private List<ConnectionMatch> connectionMatchList;
    private List<String> postKeys;
    private List<String> names;
    private DatabaseReference postsRef;

    public ConnectionMatchAdapter(Context context) {
        this.context = context;

        connectionMatchList = new ArrayList<ConnectionMatch>();
        postKeys = new ArrayList<String>();
        names = new ArrayList<String>();
        postsRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_connection_match, parent, false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ConnectionMatch connectionMatch = connectionMatchList.get(holder.getAdapterPosition());
        holder.tvName.setText(connectionMatch.getName());
        FirebaseStorage.getInstance().getReference().child("images").child(connectionMatch.getName()+".jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(holder.ivImage);
                    }

                    });

    }


    public void removeConnectionMatchByKey(String key) {
        int index = postKeys.indexOf(key);
        if (index != -1) {
            connectionMatchList.remove(index);
            names.remove(index);
            postKeys.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public int getItemCount() {
        return connectionMatchList.size();
    }

    public void addConnectionMatch(ConnectionMatch connectionMatch, String key) {
        connectionMatchList.add(connectionMatch);
        postKeys.add(key);
        names.add(connectionMatch.getName());
        notifyDataSetChanged();

    }

    public boolean containsConnectionMatchByName(String name) {
        return (names.contains(name));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvName;
        public ImageView ivImage;


        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivImage = itemView.findViewById(R.id.ivUserImg);

        }
    }
}
