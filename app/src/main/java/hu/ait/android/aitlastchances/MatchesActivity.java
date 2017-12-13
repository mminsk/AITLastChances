package hu.ait.android.aitlastchances;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hu.ait.android.aitlastchances.adapter.ConnectionMatchAdapter;
import hu.ait.android.aitlastchances.data.ConnectionMatch;

public class MatchesActivity extends AppCompatActivity {

    private String myUsername;
    private ConnectionMatchAdapter recAdapter;
    private ConnectionMatchAdapter sentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

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

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("registered").child(myUsername).child("received");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ConnectionMatch conn = dataSnapshot.getValue(ConnectionMatch.class);
                if (sentAdapter.containsConnectionMatchByName(conn.getName())) {
                    Toast.makeText(MatchesActivity.this, "You have a new match!", Toast.LENGTH_SHORT);
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

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("registered").child(myUsername).child("sent");
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

