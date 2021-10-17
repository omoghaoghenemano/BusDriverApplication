package com.bustracking.myschool.bustracker.Activities;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bustracking.myschool.bustracker.R;
import com.bustracking.myschool.bustracker.Services.LocationShareService;
import com.bustracking.myschool.bustracker.Utils.DirectionAsync;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback
        , GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
        , LocationListener,GoogleMap.OnMarkerClickListener, ResultCallback {


    GoogleMap mMap;
    GoogleApiClient client;
    LocationRequest request;
    LatLng latLngCurrentuserLocation;
    FirebaseAuth auth;
    HashMap<String,Marker> hashMap;

    boolean driver_profile = false;

    boolean user_profile = false;
    LatLng updateLatLng;
    DatabaseReference referenceDrivers,referenceUsers,scheduleReference;

    TextView textName,textEmail;

    RequestQueue requestQueue;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_navigation);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        auth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        textName = (TextView) header.findViewById(R.id.title_text);
        textEmail = (TextView) header.findViewById(R.id.email_text);




        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        referenceDrivers = FirebaseDatabase.getInstance().getReference().child("Drivers");
        referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        scheduleReference = FirebaseDatabase.getInstance().getReference().child("uploads").child("0");
        hashMap = new HashMap<>();

        referenceDrivers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = auth.getCurrentUser();
                if(dataSnapshot.child(user.getUid()).child("lat").exists())
                {
                    driver_profile= true;
                    String driver_name = dataSnapshot.child(user.getUid()).child("name").getValue(String.class);
                    String driver_email = dataSnapshot.child(user.getUid()).child("email").getValue(String.class);
                    textName.setText(driver_name);
                    textEmail.setText(driver_email);

                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.driver_menu);



                }
                else
                {
                    user_profile = true;


                    referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            FirebaseUser user1 = auth.getCurrentUser();
                            String user_name = dataSnapshot.child(user1.getUid()).child("name").getValue(String.class);
                            String user_email = dataSnapshot.child(user1.getUid()).child("email").getValue(String.class);
                            textName.setText(user_name);
                            textEmail.setText(user_email);
                            FirebaseMessaging.getInstance().subscribeToTopic("news");

                            navigationView.getMenu().clear();
                            navigationView.inflateMenu(R.menu.user_menu);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });






        referenceDrivers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                try
                {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    Double lat = Double.parseDouble(dataSnapshot.child("lat").getValue(String.class));
                    Double lng = Double.parseDouble(dataSnapshot.child("lng").getValue(String.class));


                    String vehicle_number = dataSnapshot.child("vehiclenumber").getValue(String.class);
                    LatLng latlng = new LatLng(lat,lng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title(name);
                    markerOptions.snippet("Van number: "+vehicle_number);
                    markerOptions.position(latlng);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mynewbusicon));

                    Marker myMarker = mMap.addMarker(markerOptions);

                    hashMap.put(myMarker.getTitle(),myMarker);
                }catch(Exception e)
                {
                    e.printStackTrace();

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                try
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    Double lat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                    Double lng = Double.parseDouble(dataSnapshot.child("lng").getValue().toString());

                    updateLatLng = new LatLng(lat,lng);

                    final Marker marker = hashMap.get(name);

                    if(marker!= null)
                    {
                        marker.setPosition(updateLatLng);
                    }







                }catch(Exception e)
                {
                    e.printStackTrace();
                }




            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        // Add a marker in Sydney and move the camera
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

        client.connect();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng marker_Pos = marker.getPosition();

        double distance = CalculationByDistance(latLngCurrentuserLocation,marker_Pos);
        DecimalFormat df = new DecimalFormat("#.##");
        String dist = df.format(distance);

        Toast.makeText(getApplicationContext(),dist + " KM far.",Toast.LENGTH_SHORT).show();

        //      marker.setSnippet(dist + " KM far.");

        StringBuilder sb;
        Object[] dataTransfer = new Object[5];

        sb = new StringBuilder();
        sb.append("https://maps.googleapis.com/maps/api/directions/json?");
        sb.append("origin=" + marker_Pos.latitude + "," + marker_Pos.longitude);
        sb.append("&destination=" + latLngCurrentuserLocation.latitude + "," + latLngCurrentuserLocation.longitude);
        sb.append("&key=" + "AIzaSyCsThl1-hAeG2EscPb69ii0hdSXkUJ6-x0");



        DirectionAsync getDirectionsData = new DirectionAsync(getApplicationContext());
        dataTransfer[0] = mMap;
        dataTransfer[1] = sb.toString();
        dataTransfer[2] = new LatLng(marker_Pos.latitude, marker_Pos.longitude);
        dataTransfer[3] = new LatLng(latLngCurrentuserLocation.latitude, latLngCurrentuserLocation.longitude);
        dataTransfer[4] = marker;

        getDirectionsData.execute(dataTransfer);

        return true;
    }

    private double CalculationByDistance(LatLng start, LatLng end)
    {
        int Radius=6371;//radius of earth in Km
        double lat1 = start.latitude;
        double lat2 = end.latitude;
        double lon1 = start.longitude;
        double lon2 = end.longitude;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult= Radius*c;
        double km=valueResult/1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec =  Integer.valueOf(newFormat.format(km));
        double meter=valueResult%1000;
        int  meterInDec= Integer.valueOf(newFormat.format(meter));


        return meter;

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(driver_profile)
        {
            if (id == R.id.nav_signout) {
                if (auth != null) {
                    auth.signOut();
                    finish();
                    Intent myIntent = new Intent(NavigationActivity.this, MainActivity.class);
                    startActivity(myIntent);
                }

            }

            else if(id == R.id.nav_share_Location)
            {
                if(isServiceRunning(getApplicationContext(), LocationShareService.class))
                {
                    Toast.makeText(getApplicationContext(),"You are already sharing your location.",Toast.LENGTH_SHORT).show();
                }
                else if(driver_profile)
                {
                    Intent myIntent = new Intent(NavigationActivity.this,LocationShareService.class);
                    startService(myIntent);


                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Only driver can share location",Toast.LENGTH_SHORT).show();
                }

            }
            else if(id == R.id.nav_stop_Location)
            {

                Intent myIntent2 = new Intent(NavigationActivity.this,LocationShareService.class);
                stopService(myIntent2);
            }
//            else if(id == R.id.nav_send_fcm)
//            {
//                if(driver_profile)
//                {
//             //       openDialog();
//                }
//                else
//                {
//                    Toast.makeText(getApplicationContext(),"Only drivers can send notifications",Toast.LENGTH_LONG).show();
//                }
//            }

        }

        else
        {
            if(id == R.id.nav_signout_user)
            {
                if (auth != null) {
                    auth.signOut();
                    finish();
                    Intent myIntent = new Intent(NavigationActivity.this, MainActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(myIntent);
                }
            }
        }




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }






    public boolean isServiceRunning(Context c, Class<?> serviceClass)
    {
        ActivityManager activityManager = (ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);


        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);



        for(ActivityManager.RunningServiceInfo runningServiceInfo : services)
        {
            if(runningServiceInfo.service.getClassName().equals(serviceClass.getName()))
            {
                return true;
            }
        }

        return false;


    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);
        builder.setAlwaysShow(true);
        PendingResult result =
                LocationServices.SettingsApi.checkLocationSettings(
                        client,
                        builder.build()
                );
        result.setResultCallback(this);  // dialog for location

        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.removeLocationUpdates(client,this);

        if(location == null)
        {
            Toast.makeText(this,"Could not find location",Toast.LENGTH_SHORT).show();
        }
        else
        {
            latLngCurrentuserLocation = new LatLng(location.getLatitude(),location.getLongitude());

            mMap.addMarker(new MarkerOptions().position(latLngCurrentuserLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))).setVisible(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCurrentuserLocation, 15));
        }
    }
    @Override
    public void onResult(@NonNull Result result) {
        final Status status = result.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // NO need to show the dialog;

                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //  GPS turned off, Show the user a dialog
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().

                    status.startResolutionForResult(NavigationActivity.this, 202);

                } catch (IntentSender.SendIntentException e) {

                    //failed to show dialog
                }
                break;



            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are unavailable so not possible to show any dialog now
                break;
        }
    }

}
