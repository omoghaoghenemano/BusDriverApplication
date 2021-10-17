package com.bustracking.myschool.bustracker.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.bustracking.myschool.bustracker.Activities.NavigationActivity;
import com.bustracking.myschool.bustracker.R;

public class LocationShareService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    NotificationManagerCompat nmc;
    Notification.Builder builder;


    public LocationShareService() {
    }

    GoogleApiClient client;
    LocationRequest request;
    LatLng latLngCurrent;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser user;


    public final int uniqueId = 654321;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.


        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        reference = FirebaseDatabase.getInstance().getReference().child("Drivers");
        auth = FirebaseAuth.getInstance();


        user = auth.getCurrentUser();
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);

        showNotifications();
    }

    private void showNotifications() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel("channelid1", "001", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("This is description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            builder = new Notification.Builder(getApplicationContext(), notificationChannel.getId());
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle("CIU Bus Tracker");
            builder.setContentText("You are sharing Bus location.!");
            builder.setSmallIcon(R.drawable.share_location)
                    .setPriority(Notification.PRIORITY_DEFAULT);

            nmc = NotificationManagerCompat.from(this);
            nmc.notify(uniqueId, builder.build());
        }

        else {
            builder = new Notification.Builder(getApplicationContext());
            builder.setSmallIcon(R.mipmap.ic_launcher);

                    builder.setContentTitle("CIU Bus Tracker");

            builder.setContentText("You are sharing Bus location.!").
                    setPriority(Notification.PRIORITY_DEFAULT);

            nmc = NotificationManagerCompat.from(this);
            nmc.notify(uniqueId, builder.build());
        }

        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        nm.notify(uniqueId,builder.build());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        latLngCurrent = new LatLng(location.getLatitude(), location.getLongitude());
        shareLocation();
    }

    public void shareLocation()
    {
        try
        {

            reference.child(user.getUid()).child("lat").setValue(Double.valueOf((latLngCurrent.latitude)));
            reference.child(user.getUid()).child("lng").setValue(Double.valueOf((latLngCurrent.longitude)))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful())
                            {
                                Toast.makeText(getApplicationContext(),"Could not share Location.",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Only drivers can share their location",Toast.LENGTH_SHORT).show();

        }

    }


    @Override
    public void onDestroy() {
        LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        client.disconnect();

        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(uniqueId);



    }
}
