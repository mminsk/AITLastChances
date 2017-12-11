package hu.ait.android.aitlastchances;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.android.aitlastchances.data.User;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.etEmail)
    EditText etEmail;

    @BindView((R.id.etPassword))
    EditText etPassword;

    @BindView(R.id.etFirstName)
    EditText etFirstName;

    @BindView((R.id.etLastName))
    EditText etLastName;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference ref;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        firebaseAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();

        loadRegisteredUsers(ref);

    }

    private void loadRegisteredUsers(DatabaseReference ref) {

        Map<String, String> usersToID = new HashMap<String, String>();

        usersToID.put("Emma Kennelly", "evvzpn4CeZY0OhBJPrh0PU19ZVs1");
        usersToID.put("Madison Minsk", "1HcwuH7LtDat8xypQTJI5SagTsx1");
        for (String user : usersToID.keySet()) {
            ref.child("registered").child(user).setValue(usersToID.get(user));
        }
        List<String> sentconnections = Arrays.asList("connection1", "connection2", "Madison Minsk");
        List<String> recconnections = Arrays.asList("Madison Minsk");
//        ref.child("connections").child("Emma Kennelly").child("sent").setValue(sentconnections);
//        ref.child("connections").child("Emma Kennelly").child("received").setValue(recconnections);
//        ref.child("connections").child("Madison Minsk").child("sent").setValue(new ArrayList<String>());


    }

    @OnClick(R.id.btnRegister)
    void registerClick() {

        if (!isFormValid()) {
            return;
        }

        showProgressDialog();

        firebaseAuth.createUserWithEmailAndPassword(etEmail.getText().toString(),
                etPassword.getText().toString()).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();


                        if (task.isSuccessful()) {
                            final String username = etFirstName.getText().toString() + " " + etLastName.getText().toString();
                            FirebaseUser fbUser = task.getResult().getUser();
                            final String uid = fbUser.getUid();
                            ref.child("registered").child(username).setValue(uid);

                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.child("registered").hasChild(username)) {

                                        //user exists, do something
                                    } else {
                                        Toast.makeText(LoginActivity.this,
                                                "user not in registered", Toast.LENGTH_SHORT).show();

                                        ref.child("registered").child(username).setValue(uid);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }


                            });
                            fbUser.updateProfile(new UserProfileChangeRequest.Builder().
                                    setDisplayName(username).build());

                            Toast.makeText(LoginActivity.this,
                                    "Registration ok", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: "+
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @OnClick(R.id.btnLogin)
    void loginClick() {
        startActivity(new Intent(LoginActivity.this, ProfileActivity.class));

        if (!isFormValid()) {
            return;
        }

        showProgressDialog();

        firebaseAuth.signInWithEmailAndPassword(
                etEmail.getText().toString(),
                etPassword.getText().toString()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();

                if (task.isSuccessful()) {
                    // open messages Activity
                    FirebaseUser fbUser = task.getResult().getUser();
                    final String username = etFirstName.getText().toString() + " " + etLastName.getText().toString();
                    final String uid = fbUser.getUid();
                    if (ref.child("active_users").child(uid).getRoot() == null) {
                                ref.child("active_users").child(uid).setValue(username);
                                //user doesnt exist, do something
                            }





                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class));

                } else {
                    Toast.makeText(LoginActivity.this,
                            "Error: "+task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Wait for it...");
        }

        progressDialog.show();
    }


    private boolean isFormValid() {
        if (TextUtils.isEmpty(etEmail.getText())) {
            etEmail.setError("The email can not be empty");
            return false;
        }

        if (TextUtils.isEmpty(etPassword.getText())) {
            etPassword.setError("The password can not be empty");
            return false;
        }

        if (TextUtils.isEmpty(etFirstName.getText())) {
            etFirstName.setError("The first name can not be empty");
            return false;
        }

        if (TextUtils.isEmpty(etLastName.getText())) {
            etLastName.setError("The last name can not be empty");
            return false;
        }

        return true;
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }
}
