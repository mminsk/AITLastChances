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
import hu.ait.android.aitlastchances.data.ConnectionMatch;
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

        ArrayList<String> users = new ArrayList<String>();
        users.add("Emma Kennelly");
        users.add("Madison Minsk");
        users.add("Test User1");
        users.add("Test User2");
        users.add("Test User3");
        for (String user : users) {
            ref.child("registered").child(user).child("connectionmatch").setValue(new ConnectionMatch(user));
        }


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
                            final FirebaseUser fbUser = task.getResult().getUser();

                            ref.child("registered").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        if (snapshot.child("email").getValue() == null) {
                                            ref.child("registered").child(username).child("email").setValue(fbUser.getEmail());
                                        }
                                        else {
                                            Toast.makeText(LoginActivity.this,
                                                    "You are already registered for AIT Last Chances!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Toast.makeText(LoginActivity.this,
                                                "Congratulations, you are registered!", Toast.LENGTH_SHORT).show();

                                        //user exists, do something
                                    } else {
                                        Toast.makeText(LoginActivity.this,
                                                "User is not registered for AIT Last Chances!", Toast.LENGTH_SHORT).show();

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }


                            });
                            fbUser.updateProfile(new UserProfileChangeRequest.Builder().
                                    setDisplayName(username).build());

//                            Toast.makeText(LoginActivity.this,
//                                    "Registration ok", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: "+
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @OnClick(R.id.btnLogin)
    void loginClick() {

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
                    // open profile Activity
                    final String username = etFirstName.getText().toString() + " " + etLastName.getText().toString();
                    FirebaseUser fbUser = task.getResult().getUser();
                    fbUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build());
                    Toast.makeText(LoginActivity.this, "changed display name", Toast.LENGTH_SHORT);
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
