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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.android.aitlastchances.data.ConnectionMatch;

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
    private String username;

    private ProgressDialog progressDialog;
    private ArrayList<String> registeredUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        firebaseAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        registeredUsers = loadRegisteredUsers(ref);

    }

    private ArrayList<String> loadRegisteredUsers(DatabaseReference ref) {

        ArrayList<String> users = new ArrayList<String>();
        users.add("Emma Kennelly");
        users.add("Madison Minsk");
        users.add("Isaac Gluck");
        users.add("Anders Bando-Hess");
        users.add("Ellen Smalley");
        users.add("Neerja Thakkar");
        users.add("Aishu Nallapillai");
        users.add("Elizabeth Hart");

        for (String user : users) {
            ref.child("registered").child(user).child("connectionmatch").setValue(new ConnectionMatch(user));
        }

        return users;
    }


    @OnClick(R.id.btnLogin)
    void loginClick() {

        if (!isFormValid()) {
            return;
        }

        showProgressDialog();
        final String username = etFirstName.getText().toString() + " " + etLastName.getText().toString();

        if (!registeredUsers.contains(username)) {
            Toast.makeText(LoginActivity.this,
                    R.string.not_registered,
                    Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(
                etEmail.getText().toString(),
                etPassword.getText().toString()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();

                if (task.isSuccessful()) {
                    final String username = etFirstName.getText().toString() + " " + etLastName.getText().toString();
                    FirebaseUser fbUser = task.getResult().getUser();
                    fbUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build());
                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class));

                } else {
                    Toast.makeText(LoginActivity.this,
                            getString(R.string.error)+task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.wait_for_it));
        }

        progressDialog.show();
    }


    private boolean isFormValid() {
        if (TextUtils.isEmpty(etEmail.getText())) {
            etEmail.setError(getString(R.string.cannot_be_empty));
            return false;
        }

        if (TextUtils.isEmpty(etPassword.getText())) {
            etPassword.setError(getString(R.string.pswd_cannot_be_empty));
            return false;
        }

        if (TextUtils.isEmpty(etFirstName.getText())) {
            etFirstName.setError(getString(R.string.fnam_cannot_be_empty));
            return false;
        }

        if (TextUtils.isEmpty(etLastName.getText())) {
            etLastName.setError(getString(R.string.lname_cannot_be_empty));
            return false;
        }

        return true;
    }


}
