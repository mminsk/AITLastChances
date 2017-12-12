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

    private void addConnection(String connection) {
        final String nameToConnectWith = connection;
        final String key = ref.child("registered").child(myUsername).push().getKey();
        ConnectionMatch toConnection = new ConnectionMatch(connection);

        FirebaseDatabase.getInstance().getReference().child("registered").child(myUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConnectionMatch fromConnection = dataSnapshot.getValue(ConnectionMatch.class);
                if (fromConnection != null) {
                    FirebaseDatabase.getInstance().getReference().child("registered").child(nameToConnectWith).child("received").child(key).setValue(fromConnection);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("registered").child(nameToConnectWith).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConnectionMatch toConnection = dataSnapshot.getValue(ConnectionMatch.class);

                if (toConnection != null) {
                    if (dataSnapshot.child("image").getValue(String.class) != null && toConnection.getImageUrl() == null) {
                        toConnection.setImageUrl(Uri.parse(dataSnapshot.child("image").getValue(String.class)));
                    }
                    FirebaseDatabase.getInstance().getReference().child("registered").child(myUsername).child("sent").child(key).setValue(toConnection).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ConnectionsActivity.this, "Connection created", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }


}

