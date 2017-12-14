package hu.ait.android.aitlastchances;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import hu.ait.android.aitlastchances.adapter.ConnectionMatchAdapter;
import hu.ait.android.aitlastchances.data.ConnectionMatch;

public class ConnectionsActivity extends AppCompatActivity {
    public static final int REQUEST_NEW_CONNECTION = 101;
    private String myUsername;
    private ConnectionMatchAdapter adapter;

    private ConnectionMatchAdapter recAdapter;
    private ConnectionMatchAdapter matchesAdapter;

    ArrayList<ConnectionMatch> connections = null;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);

        myUsername = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        ref = FirebaseDatabase.getInstance().getReference();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPosts);
        adapter = new ConnectionMatchAdapter(this);
        recAdapter = new ConnectionMatchAdapter(this);
        matchesAdapter = new ConnectionMatchAdapter(this);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateConnectionActivity();
            }
        });
        initReceivedConnectionsListener();
        initConnectionsListener();
    }

    private void initConnectionsListener() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("registered").child(myUsername).child("sent");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ConnectionMatch conn = dataSnapshot.getValue(ConnectionMatch.class);
                if (recAdapter.containsConnectionMatchByName(conn.getName()) && !matchesAdapter.containsConnectionMatchByName(conn.getName())) {
                    Toast.makeText(ConnectionsActivity.this, R.string.you_have_new_match, Toast.LENGTH_SHORT);
                    matchesAdapter.addConnectionMatch(conn, dataSnapshot.getKey());
                }
                adapter.addConnectionMatch(conn, dataSnapshot.getKey());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.removeConnectionMatchByKey(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initReceivedConnectionsListener() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("registered").child(myUsername).child("received");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ConnectionMatch conn = dataSnapshot.getValue(ConnectionMatch.class);
                recAdapter.addConnectionMatch(conn, dataSnapshot.getKey());
                if (adapter.containsConnectionMatchByName(conn.getName())) {
                    Toast.makeText(ConnectionsActivity.this, R.string.you_have_new_match, Toast.LENGTH_LONG);
                    matchesAdapter.addConnectionMatch(conn, dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                recAdapter.removeConnectionMatchByKey(dataSnapshot.getKey());
                matchesAdapter.removeConnectionMatchByKey(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void showCreateConnectionActivity() {
        Intent intentStart = new Intent(ConnectionsActivity.this,
                CreateConnectionActivity.class);
        startActivityForResult(intentStart, REQUEST_NEW_CONNECTION);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                Bundle values = data.getExtras();

                String newConnection = ((String) values.get(CreateConnectionActivity.NAME));
                addConnection(newConnection);

                break;
            case RESULT_CANCELED:

                break;
        }
    }

    private void addConnection(final String nameToConnectWith) {
        if (adapter.containsConnectionMatchByName(nameToConnectWith)) {
            Toast.makeText(ConnectionsActivity.this, R.string.already_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (recAdapter.containsConnectionMatchByName(nameToConnectWith)) {
            Toast.makeText(ConnectionsActivity.this, R.string.you_have_new_match, Toast.LENGTH_SHORT);
        }

        ref.child("registered").child(nameToConnectWith).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConnectionMatch newConnectionToSend = dataSnapshot.child("connectionmatch").getValue(ConnectionMatch.class);

                if (newConnectionToSend == null) {
                    newConnectionToSend = new ConnectionMatch(nameToConnectWith);
                }

                String key = ref.child(myUsername).child("sent").push().getKey();
                ref.child("registered").child(myUsername).child("sent").child(key).setValue(newConnectionToSend);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ref.child("registered").child(myUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConnectionMatch myConnectionData = dataSnapshot.child("connectionmatch").getValue(ConnectionMatch.class);
                String key = ref.child(nameToConnectWith).child("received").push().getKey();
                ref.child("registered").child(nameToConnectWith).child("received").child(key).setValue(myConnectionData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (recAdapter.containsConnectionMatchByName(nameToConnectWith)) {
                            Toast.makeText(ConnectionsActivity.this, R.string.you_have_new_match, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(ConnectionsActivity.this, R.string.connection_sent, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}

