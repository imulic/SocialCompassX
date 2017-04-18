package com.snipsystems.irfanmulic.socialcompassx;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    DatabaseReference database;

    private FirebaseUser user;
    private ProgressBar progressBar;

    private SensorManager mSensorManager;

    TextView tvHeading;
    private LocationManager mLocationManager;
    private Location myGPSLocation;

    List<Person> locationsTracked = new ArrayList<Person>();

    List<Person> person_locations = new ArrayList<>();

    Person myLocation = new Person("irfan", 32.65702666004866d, -116.9703197479248d, "San Diego","");

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {

            myGPSLocation = location;
            Log.i("IRFAN",location.toString());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance().getReference();

        setContentView(R.layout.activity_main);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        user = FirebaseAuth.getInstance().getCurrentUser();

        populateDefaultLocations();

        readLocations();

        TextView t = (TextView)findViewById(R.id.text1);
        if (user != null)
           t.setText(user.getEmail()+": "+user.getUid());

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        Button readLocationsBtn = (Button)findViewById(R.id.button1);

        readLocationsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("ASJA", "Clicked button");
             //   readLocations();
            }
        });

        // our compass image
        image = (ImageView) findViewById(R.id.imageViewCompass);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

/*        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2*60*1000,
                10, mLocationListener); */

        //final DatabaseReference locationRef = database.getReference("lastGPSLocation");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("ASJA","trying to read value");
                String key = dataSnapshot.getKey();
                Log.i("ASJA","value is "+key);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ASJA",databaseError.toString());
            }
        });

        final Handler h1 = new Handler();
        Runnable pushCurrentLocation = new Runnable() {
            @Override
            public void run() {
                if (myGPSLocation != null)
                    database.child(user.getUid()).setValue(myGPSLocation);
                h1.postDelayed(this,10000);
            }
        };

        h1.post(pushCurrentLocation);

        // Refresh myGPSLocation data that we are interested in from the server
        final Handler h2 = new Handler();
        Runnable runData = new Runnable() {
            @Override
            public void run() {

                Log.i("IRFAN","Refreshing locations");
                refreshFavoriteLocations();
                h2.postDelayed(this, 10000);
            }
        };

        h2.post(runData);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    private void clearDefaultLocations(){

    }

    protected void refreshFavoriteLocations() {

        String momUrl = "http://localhost:3000/api/important_locations?filter=%7B%22id%22%3A1%7D";
        String all_locations = "http://01e7e9b9.ngrok.io/api/important_locations";

        JsonArrayRequest jsonRequest = new JsonArrayRequest
                (Request.Method.GET, all_locations, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        person_locations.clear();

                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject person = (JSONObject) response
                                        .get(i);

                                String name = person.getString("name"),
                                        lon = person.getString("lon"),
                                        lat = person.getString("lat"),
                                        city = person.getString("city");

                                person_locations.add(new Person(name, Double.parseDouble(lat), Double.valueOf(lon), city,user.getUid()));

                                //Log.i("PERSON", p.toString());
                                System.out.println("Response from the server: Name: " + name + "\nCity: " + city + "\nLon" + lon + "\nLat" + lat);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(this).add(jsonRequest);
    }


    private void populateDefaultLocations() {
        Person a = new Person("azra", 32.65702666004866d, -116.9703197479248d, "San Diego",user.getUid());
        Person b = new Person("asja", 32.6570266600489d, -116.9703197479291d, "San Diego",user.getUid());

        locationsTracked.add(a);
        locationsTracked.add(b);

        database.child("locations").child(user.getUid()).setValue(locationsTracked);
    }

    private void readLocations(){

    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
