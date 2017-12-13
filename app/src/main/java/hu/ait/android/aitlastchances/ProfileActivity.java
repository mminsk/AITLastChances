package hu.ait.android.aitlastchances;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.android.aitlastchances.adapter.ConnectionMatchAdapter;
import hu.ait.android.aitlastchances.data.ConnectionMatch;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.btnUpload)
    Button btnUpload;
    @BindView(R.id.ivUploadImg)
    ImageView imgUpload;
    @BindView(R.id.tvConnectingWithYou)
    TextView tvConnectingWithYou;
    @BindView(R.id.tvSentConnections)
    TextView tvSentConnections;
    @BindView(R.id.tvMatches)
    TextView tvMatches;
    @BindView(R.id.tvName)
    TextView tvName;

    private String myUsername;

    private ConnectionMatchAdapter recAdapter;
    private ConnectionMatchAdapter sentAdapter;
    private ConnectionMatchAdapter matchesAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        requestNeededPermission();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            myUsername = currentUser.getDisplayName();
            tvName.setText("Hi, " + myUsername + "!");
            final Uri myImageUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
            if (myImageUrl != null) {
                Glide.with(ProfileActivity.this).load(myImageUrl).into(imgUpload);
                uploadProfileImage(myImageUrl.toString());
            }

            sentAdapter = new ConnectionMatchAdapter(this);
            recAdapter = new ConnectionMatchAdapter(this);
            matchesAdapter = new ConnectionMatchAdapter(this);
            initSentConnectionsListener();
            initReceivedConnectionsListener();
        }

        else {
            tvName.setText("Current user is null");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        return true;
    }


    private void requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {
                // Toast...
            }

            ActivityCompat.requestPermissions(this, new String[]{
                            android.Manifest.permission.CAMERA},
                    101);
        } else {
            // we have the permission
            btnUpload.setVisibility(View.VISIBLE);

        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_matches:
                    startActivity(new Intent(ProfileActivity.this, MatchesActivity.class));

                    return true;
                case R.id.navigation_connections:
                    startActivity(new Intent(ProfileActivity.this, ConnectionsActivity.class));

                    return true;
            }
            return false;
        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                return true;

            case R.id.action_help:
                startActivity(new Intent(ProfileActivity.this, HelpActivity.class));
            default:
                return true;
        }
    }


    private void uploadImage() throws Exception {
        imgUpload.setDrawingCacheEnabled(true);
        imgUpload.buildDrawingCache();
        Bitmap bitmap = imgUpload.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInBytes = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String newImage = myUsername+".jpg";
        StorageReference newImageRef = storageRef.child(newImage);
        StorageReference newImageImagesRef = storageRef.child("images/"+newImage);
        newImageRef.getName().equals(newImageImagesRef.getName());    // true
        newImageRef.getPath().equals(newImageImagesRef.getPath());    // false

        UploadTask uploadTask = newImageImagesRef.putBytes(imageInBytes);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(ProfileActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                uploadProfileImage(downloadUrl.toString());
            }
        });
    }

    private void uploadProfileImage(final String... imageUrl) {
        final String myImageUrl = imageUrl[0];
        if (imageUrl != null && imageUrl.length>0) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(myImageUrl))
                    .build();
            user.updateProfile(profileUpdates);
        }

    }

    @OnClick(R.id.btnUpload)
    public void uploadClicked() {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentCamera, 101);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101 && resultCode == RESULT_OK) {
            Bitmap img = (Bitmap) data.getExtras().get("data");
            imgUpload.setImageBitmap(img);
            imgUpload.setVisibility(View.VISIBLE);
            try {
                uploadImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initReceivedConnectionsListener() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("registered").child(myUsername).child("received");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ConnectionMatch conn = dataSnapshot.getValue(ConnectionMatch.class);
                recAdapter.addConnectionMatch(conn, dataSnapshot.getKey());
                tvConnectingWithYou.setText("You have " + Integer.toString(recAdapter.getItemCount()) + " people who want to connect with you.");
                if (sentAdapter.containsConnectionMatchByName(conn.getName())) {
                    Toast.makeText(ProfileActivity.this, "You have a new match!", Toast.LENGTH_SHORT);
                    matchesAdapter.addConnectionMatch(conn, dataSnapshot.getKey());
                    tvMatches.setText("You have " + Integer.toString(matchesAdapter.getItemCount()) + " matches!");
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

    private void initSentConnectionsListener() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("registered").child(myUsername).child("sent");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ConnectionMatch conn = dataSnapshot.getValue(ConnectionMatch.class);

                sentAdapter.addConnectionMatch(conn, dataSnapshot.getKey());
                tvSentConnections.setText("You have sent " + Integer.toString(sentAdapter.getItemCount()) + " connections.");
                if (recAdapter.containsConnectionMatchByName(conn.getName())) {
                    Toast.makeText(ProfileActivity.this, "You have a new match!", Toast.LENGTH_SHORT);
                    matchesAdapter.addConnectionMatch(conn, dataSnapshot.getKey());
                    tvMatches.setText("You have " + Integer.toString(matchesAdapter.getItemCount()) + " matches!");
                }
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
