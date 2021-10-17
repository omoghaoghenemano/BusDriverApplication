package com.bustracking.myschool.bustracker.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bustracking.myschool.bustracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {


    @BindView(R.id.editTextUserEmail) EditText editTextUserEmail;
    @BindView(R.id.editTextUserPassword) EditText editTextUserPassword;
    @BindView(R.id.userToolbar) Toolbar toolbar;


    FirebaseAuth auth;
    ProgressDialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        toolbar.setTitle("Login");
        setSupportActionBar(toolbar);
        auth = FirebaseAuth.getInstance();


        dialog = new ProgressDialog(this);

    }


    public void login(View v)
    {

        dialog.setMessage("Logging in. Please wait.");
        dialog.show();

            if(editTextUserEmail.getText().toString().equals("") || editTextUserPassword.getText().toString().equals(""))
            {
                Toast.makeText(LoginActivity.this,"Blank fields are not allowed.",Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
            else
            {
                String newEmail = editTextUserEmail.getText().toString();

                auth.signInWithEmailAndPassword(newEmail,editTextUserPassword.getText().toString())
                        .addOnCompleteListener(task -> {
                                    if(task.isSuccessful())
                                    {
                                        dialog.dismiss();
                                        Intent loginIntent = new Intent(LoginActivity.this, NavigationActivity.class);
                                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(loginIntent);
                                        finish();


                                    }
                                    else
                                    {
                                        Toast.makeText(LoginActivity.this,
                                                "Wrong email/password combination. Try again.",
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                        });
            }
    }

}
