package hu.ait.android.aitlastchances;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import hu.ait.android.aitlastchances.data.ConnectionMatch;

public class MatchesActivity extends AppCompatActivity {

    private String myUsername;
    private ConnectionMatchAdapter recAdapter;
    private ConnectionMatchAdapter sentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);

        myUsername = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();


        RecyclerView recyclerView = findViewById(R.id.recyclerViewPosts);
        sentAdapter = new ConnectionMatchAdapter(this);
        recAdapter = new ConnectionMatchAdapter(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recAdapter);

        initSentConnectionsListener();
        initReceivedConnectionsListener();

    }

    private void initReceivedConnectionsListener() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("connections").child(myUsername).child("received");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ConnectionMatch conn = dataSnapshot.getValue(ConnectionMatch.class);
                if (sentAdapter.containsConnectionMatchByName(conn.getName())) {

                    recAdapter.addConnectionMatch(conn, dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                recAdapter.removeConnectionMatchByKey(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void initSentConnectionsListener() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("connections").child(myUsername).child("sent");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ConnectionMatch conn = dataSnapshot.getValue(ConnectionMatch.class);
                sentAdapter.addConnectionMatch(conn, dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                sentAdapter.removeConnectionMatchByKey(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }





}

