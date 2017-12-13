package hu.ait.android.aitlastchances;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import hu.ait.android.aitlastchances.data.ConnectionMatch;

public class ConnectionsActivity extends AppCompatActivity {
    public static final int REQUEST_NEW_CONNECTION = 101;
    private String myUsername;
    private ConnectionMatchAdapter adapter;
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

        initConnectionsListener();
    }

    private void initConnectionsListener() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("registered").child(myUsername).child("sent");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ConnectionMatch conn = dataSnapshot.getValue(ConnectionMatch.class);



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
            Toast.makeText(ConnectionsActivity.this, "Oops, you already connected with this person!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Find ConnectionMatch of person you want to connect with in registered tree, and add it to my "sent" list

        ref.child("registered").child(nameToConnectWith).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // datasnapshot contains person's ConnectionMatch
                ConnectionMatch newConnectionToSend = dataSnapshot.child("connectionmatch").getValue(ConnectionMatch.class);

                if (newConnectionToSend == null) {
                    newConnectionToSend = new ConnectionMatch(nameToConnectWith);
                }


                // now add this to my list of "sent" connections
                String key = ref.child(myUsername).child("sent").push().getKey();
                ref.child("registered").child(myUsername).child("sent").child(key).setValue(newConnectionToSend);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Now add my ConnectionMatch to new connection's "received" list

        ref.child("registered").child(myUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // datasnapshot should contain my ConnectionMatch object
                ConnectionMatch myConnectionData = dataSnapshot.child("connectionmatch").getValue(ConnectionMatch.class);
                String key = ref.child(nameToConnectWith).child("received").push().getKey();
                ref.child("registered").child(nameToConnectWith).child("received").child(key).setValue(myConnectionData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ConnectionsActivity.this, "Your connection was sent!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}

