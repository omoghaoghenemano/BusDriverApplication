package com.bustracking.myschool.bustracker.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.bustracking.myschool.bustracker.Models.Driver;
import com.bustracking.myschool.bustracker.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DriverRegistrationActivity extends AppCompatActivity {

    @BindView(R.id.editTextUserName) EditText editTextDriverName;
    @BindView(R.id.editTextUserEmail) EditText editTextDriverEmail;
    @BindView(R.id.editTextUserPassword) EditText editTextDriverPassword;
    @BindView(R.id.editTextDriverBus) EditText editTextDriverBus;
    @BindView(R.id.driverToolbar) Toolbar toolbar;


    FirebaseAuth auth;
    ProgressDialog dialog;
    FirebaseUser user;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);
        ButterKnife.bind(this);
        toolbar.setTitle("Driver Register");
        setSupportActionBar(toolbar);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

    }

    public void registerDriver(View v) {
        dialog.setTitle("Creating account");
        dialog.setMessage("Please wait");
        dialog.show();

        final String name = editTextDriverName.getText().toString();
        final String email = editTextDriverEmail.getText().toString();
        final String password = editTextDriverPassword.getText().toString();


        if (name.equals("") && email.equals("") && password.equals("")) {
            Toast.makeText(this, "Please enter correct details", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        } else {
            doAllStuff();
        }


    }


    public void doAllStuff() {

        auth.createUserWithEmailAndPassword(editTextDriverEmail.getText().toString(),
                editTextDriverPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {
                            Driver driver = new Driver(editTextDriverName.getText().toString(),
                                    editTextDriverEmail.getText().toString(),
                                    editTextDriverPassword.getText().toString(),
                                    editTextDriverBus.getText().toString(),
                                    33.652037, 73.156598);

                            user = auth.getCurrentUser();
                            databaseReference = FirebaseDatabase.getInstance().
                                    getReference().child("Drivers").child(user.getUid());

                            databaseReference.setValue(driver)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                dialog.dismiss();
                                                Toast.makeText(DriverRegistrationActivity.this,
                                                        "Account created successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                                Intent myIntent = new Intent(DriverRegistrationActivity.this, NavigationActivity.class);
                                                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(myIntent);
                                            }
                                            else
                                            {
                                                Toast.makeText(DriverRegistrationActivity.this,
                                                        "Could not register driver",Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    });

                        }
                        else
                        {
                            Toast.makeText(DriverRegistrationActivity.this,
                                    "Could not register. "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                });


    }


}
