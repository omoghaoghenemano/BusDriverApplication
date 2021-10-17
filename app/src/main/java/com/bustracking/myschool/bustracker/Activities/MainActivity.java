package com.bustracking.myschool.bustracker.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.bustracking.myschool.bustracker.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

        if (user == null) {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
            checkPermissions();


        } else {
            Intent myIntent = new Intent(MainActivity.this, NavigationActivity.class);
            startActivity(myIntent);
            finish();

        }


    }







    public void registerAsDriver(View v) {

        Intent myIntent = new Intent(getApplicationContext(), DriverRegistrationActivity.class);
        startActivity(myIntent);

    }

    public void login(View v) {
        Intent myIntent = new Intent(this, LoginActivity.class);
        startActivity(myIntent);

    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    100);
            return false;
        }
        return true;
    }






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            for (int i = 0; i < permissions.length; i++) {

                int grantResult = grantResults[i];

                if (grantResult == PackageManager.PERMISSION_GRANTED) {

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission
                                .READ_EXTERNAL_STORAGE}, 100);
                    }
                }

            }

        }

    }
}
