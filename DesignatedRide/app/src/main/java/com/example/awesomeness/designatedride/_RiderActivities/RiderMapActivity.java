package com.example.awesomeness.designatedride._RiderActivities;

import android.Manifest;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import com.example.awesomeness.designatedride.R;
import com.example.awesomeness.designatedride.util.Constants;
import com.example.awesomeness.designatedride.util.Checker;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeolocationApiRequest;
import com.google.maps.PendingResult;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApiRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.TravelMode;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;


public class RiderMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = "RiderMapActivity";
    private static final String API_KEY = "AIzaSyBQGYYk0KmrijL1JHdn8iu8X_lXWp9IPh4";
    private static final long DES_TIME = 10000; //milliseconds
    private static final long EXP_TIME = 5000;
    private static final Double radius = 900.0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;

    // Maps
    private Boolean mapLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mapFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Marker marker;

    //Widgets
    Button setPickupBtn;
    Button acceptButton;
    Button declineButton;
    TextView txt;
    TextView title;
    TextView name;
    View text_box;
    EditText time;
    EditText destination;
    EditText pickUp;
    Guideline guideline;
    //LinearLayout box1;
    //LinearLayout box2;
    //LinearLayout box3;

    //List
    private ArrayList<Marker> availableDrivers;
    private List<Address> pickUpAddress;
    private List<Address> destinationAddress;
    private ArrayList<String> toTime;
    private ArrayList<String> fromTime;
    private Address location;
    private double Longitude;
    private double Latitude;

    //Marker
    private Marker driver;
    private Marker driverMarker;
    private Marker locMarker;
    private Marker pickUpMarker;
    private Marker destinationMarker;

    //FireBase
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private String userid;

    private String key;
    private String riderRating;
    private String driverRating;
    private String driverKey;
    private Integer seqAck;
    private Integer filter;
    private Integer temp;
    private Double distance;
    private String preferedTime;
    private String isAvailable;
    private String mPushKey;
    private String pairKey;
    private String destinationLocation;
    private String pickUpLocation;
    private String timeItOccurs;
    private String appointmentDate;
    private String appointmentMonth;
    private String online;

    //Alert Box
    AlertDialog.Builder confirmation;
    AlertDialog dialogBox;
    private ProgressDialog mProgressDialog;
    Timer timer;

    Calendar toTimeCalendar;
    Calendar fromTimeCalendar;

    //Map
    private Map writeInfo;
    private Map exchangeInfo;

    //GeoFire
    private GeoQuery mGeoQuery;
    private GeoFire mAvailableGeoFire;
    private GeoFire mGeoFire;
    private GeoFire mLocation;
    private GeoFire mPreferencedGeoFire;
    private DatabaseReference mAvailableGeoLocationRef;
    private DatabaseReference mGeoLocationRef;
    private DatabaseReference mChildAvailable;
    private DatabaseReference mChildLocation;
    private DatabaseReference mPreference;
    private DatabaseReference mLocationRef;
    private DatabaseReference mChildDropOff;
    private DatabaseReference mPreferencedLocation;
    private DatabaseReference mDriverReference;
    private ChildEventListener childEventListener;
    private ChildEventListener driverEventListener;
    private DistanceMatrixApiRequest distanceMatrixApiRequest;
    private GeocodingApiRequest geocodingApiRequestDestination;
    private GeocodingApiRequest geocodingApiRequestPickUp;
    private DistanceMatrix distanceMatrix;
    private GeocodingResult geocodingDestinationResult[];
    private GeocodingResult geocodingPickUpResult[];
    private GeoApiContext context;

    private Query obtainKey;
    private Query obtainRating;
    private Query obtainDriverKey;
    private Query obtainPairKey;
    private Query obtainColor;
    private Query obtainDistance;
    private Query obtainTime;
    private Query obtainIsAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_map);

        mapFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(DES_TIME);
        mLocationRequest.setFastestInterval(EXP_TIME);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        confirmation = new AlertDialog.Builder(RiderMapActivity.this);

        callBack();

        mDatabase = FirebaseDatabase.getInstance();

        mDatabaseReference = mDatabase.getReference();
        mChildLocation = mDatabase.getReference();
        mChildAvailable = mDatabase.getReference();
        mChildDropOff = mDatabase.getReference();
        mPreference = mDatabase.getReference();

        mAvailableGeoLocationRef = mChildAvailable.child(Constants.AVAILABLE_GEOLOCATION);
        mGeoLocationRef = mChildLocation.child(Constants.GEO_LOCATION);
        mLocationRef = mChildDropOff.child(Constants.LOCATION);
        mPreferencedLocation = mPreference.child(Constants.PREFERENCE_LOCATION);

        mAvailableGeoFire = new GeoFire(mAvailableGeoLocationRef);
        mPreferencedGeoFire = new GeoFire(mPreferencedLocation);
        mGeoFire = new GeoFire(mGeoLocationRef);
        mLocation = new GeoFire(mLocationRef);

        mAuth = FirebaseAuth.getInstance();

        //ToDO: Token error
        userid = mAuth.getCurrentUser().getUid();

        availableDrivers = new ArrayList<>();

        initWidgets();

        //removeFields();

        context = new GeoApiContext.Builder().apiKey(API_KEY).build();

        setPickupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (checkPickUp(pickUp, destination, time)) {

                        if(destinationMarker != null)
                            destinationMarker.remove();

                        if(pickUpMarker != null)
                            pickUpMarker.remove();

                        getDeviceLocation();

                        geocodingApiRequestDestination = GeocodingApi.newRequest(context);
                        geocodingApiRequestPickUp = GeocodingApi.newRequest(context);

                        geocodingApiRequestDestination.address(destinationLocation);
                        geocodingApiRequestPickUp.address(pickUpLocation);

                        geocodingDestinationResult = geocodingApiRequestDestination.awaitIgnoreError();
                        geocodingPickUpResult = geocodingApiRequestPickUp.awaitIgnoreError();

                        if (checkAddress(geocodingDestinationResult,destinationLocation) && checkTime(timeItOccurs)) {
                            Toast.makeText(RiderMapActivity.this, "Requested A Ride!", Toast.LENGTH_SHORT).show();

                            Longitude = geocodingDestinationResult[0].geometry.location.lng;
                            Latitude = geocodingDestinationResult[0].geometry.location.lat;

                            // RRuG N RPkPa
                            // N iCm & WPmRak & WPkPam
                            // UN RRkUR WTmURr
                            // N Q PdS
                            //<---> N RRkUR WtmURr & N Cm & WPmRak & WPkPam
                            //<---> N Q PdS & FDQ

                            obtainKey = mDatabaseReference.child(Constants.RIDER).child(userid).child(Constants.GEOKEY);
                            obtainKey.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    key = dataSnapshot.getValue(String.class);
                                    if(mPushKey != null)
                                        wipeAllText();

                                    obtainRating = mDatabaseReference.child(Constants.RIDER).child(userid).child(Constants.USER_RATING);
                                    obtainRating.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            riderRating = dataSnapshot.getValue(String.class);

                                            mPushKey = FirebaseDatabase.getInstance().getReference(Constants.TEXT_BOX + "/").push().getKey();
                                            mDatabaseReference.child(Constants.TEXT_BOX).child(mPushKey).child(Constants.USER_RATING).setValue(riderRating);
                                            mDatabaseReference.child(Constants.TEXT_BOX).child(mPushKey).child(Constants.LOCATION).setValue(destinationLocation);
                                            mDatabaseReference.child(Constants.PAIR).child(mPushKey).child(Constants.RIDER_KEY).setValue(key);
                                            mDatabaseReference.child(Constants.PAIR).child(key).child(Constants.PAIR_KEY).setValue(mPushKey);
                                            mDatabaseReference.child(Constants.PAIR).child(mPushKey).child(Constants.IS_ADVANCED_BOOKING).setValue("false");

                                            mGeoFire.setLocation(mPushKey, new GeoLocation(Latitude, Longitude), new GeoFire.CompletionListener() {
                                                @Override
                                                public void onComplete(String key, DatabaseError error) {

                                                }
                                            });

                                            killText();

                                            mGeoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                                                @Override
                                                public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {

                                                    driverKey = dataSnapshot.getKey();
                                                    Longitude = location.longitude;
                                                    Latitude = location.latitude;

                                                    obtainColor = mDatabaseReference.child(Constants.PACKET).child(driverKey).child(Constants.SEQ_ACK);
                                                    obtainColor.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            filter = dataSnapshot.getValue(Integer.class);

                                                            if (filter != null) {
                                                                obtainDistance = mDatabaseReference.child(Constants.PREFERENCE).child(driverKey).child(Constants.DISTANCE);
                                                                obtainDistance.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                                        distance = dataSnapshot.getValue(Double.class);

                                                                        if(distance != null) {
                                                                            if(distance >= radius){

                                                                                if (filter == 0) {
                                                                                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car_white)));
                                                                                } else
                                                                                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));

                                                                                marker.setTag(driverKey);
                                                                                availableDrivers.add(marker);

                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }

                                                    });

                                                }

                                                @Override
                                                public void onDataExited(DataSnapshot dataSnapshot) {

                                                }

                                                @Override
                                                public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                                                }

                                                @Override
                                                public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

                                                }

                                                @Override
                                                public void onGeoQueryReady() {

                                                }

                                                @Override
                                                public void onGeoQueryError(DatabaseError error) {

                                                }
                                        });

                                            // RPcdS WPcdS6
                                            // UN RPcdUR N ceL Out WPdPam

                                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                                @Override
                                                public boolean onMarkerClick(Marker marker) {
                                                    driverMarker = marker;

                                                    driverKey = (String) marker.getTag();
                                                    mDriverReference = mDatabaseReference.child(Constants.PACKET).child(driverKey).child(Constants.SEQ_ACK);

                                                    transactions(0,6);
                                                    //ToDo: Don't force a change
                                                    seqAck = 6;
                                                    driverMarker.setVisible(false);

                                                    obtainRating = mDatabaseReference.child(Constants.PACKET).child(driverKey).child(Constants.USER_RATING);
                                                    obtainRating.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            driverRating = dataSnapshot.getValue(String.class);
                                                            String message = driverRating;
                                                            txt.setText(message);
                                                            message = "Choose This Driver?";
                                                            title.setText(message);
                                                            message = "";
                                                            name.setText(message);
                                                            confirmation.setView(text_box);
                                                            dialogBox = confirmation.create();
                                                            dialogBox.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                            dialogBox.show();

                                                            acceptButton.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    childEventListener = mDatabaseReference.child(Constants.PACKET).child(driverKey).addChildEventListener(new ChildEventListener() {
                                                                        @Override
                                                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                                        }

                                                                        @Override
                                                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                                                            if (dataSnapshot.getKey().equals(Constants.SEQ_ACK)) {

                                                                                mProgressDialog.dismiss();
                                                                                seqAck = dataSnapshot.getValue(Integer.class);

                                                                                if (seqAck == 0 || seqAck == 4) {
                                                                                    Toast.makeText(RiderMapActivity.this, "Driver is current unavailable", Toast.LENGTH_LONG).show();

                                                                                    mDatabaseReference.child(Constants.PACKET).child(driverKey).removeEventListener(childEventListener);
                                                                                    mDatabaseReference.child(Constants.PAIR).child(driverKey).removeValue();

                                                                                    availableDrivers.remove(driverMarker);
                                                                                    driverMarker.remove();

                                                                                    if(availableDrivers.size() == 0){
                                                                                        returnFields();
                                                                                    }

                                                                                } else if (seqAck == 2) {

                                                                                    mDatabaseReference.child(Constants.PACKET).child(Constants.PAIR_KEY).setValue(mPushKey);
                                                                                    mDatabaseReference.child(Constants.TEXT_BOX).child(mPushKey).removeValue();

                                                                                    transactions(seqAck,3);

                                                                                } else if (seqAck == 3) {
                                                                                    saveText();
                                                                                    removeFields();

                                                                                    mDatabaseReference.child(Constants.PAIR).child(mPushKey).child(Constants.DRIVER_KEY).setValue(driverKey);

                                                                                    for (int j = 0; j < availableDrivers.size(); j++) {
                                                                                        driver = availableDrivers.get(j);
                                                                                        driver.remove();
                                                                                    }

                                                                                    Toast.makeText(RiderMapActivity.this, "Connected with Driver", Toast.LENGTH_LONG).show();

                                                                                    mGeoFire.getLocation(driverKey, new com.firebase.geofire.LocationCallback() {
                                                                                        @Override
                                                                                        public void onLocationResult(String key, GeoLocation location) {
                                                                                            driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                                                                                            driverMarker.setTag(driverKey);
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                        }
                                                                                    });

                                                                                    driverEventListener = mDatabaseReference.child(Constants.GEO_LOCATION).child(driverKey).addChildEventListener(new ChildEventListener() {
                                                                                        @Override
                                                                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                                                                        }

                                                                                        @Override
                                                                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                                                                            mGeoFire.getLocation(driverKey, new com.firebase.geofire.LocationCallback() {
                                                                                                @Override
                                                                                                public void onLocationResult(String key, GeoLocation location) {
                                                                                                    driverMarker.remove();
                                                                                                    driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                                                                                                }

                                                                                                @Override
                                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                                }
                                                                                            });
                                                                                        }

                                                                                        @Override
                                                                                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                                                                                            mDatabaseReference.child(Constants.PACKET).child(driverKey).removeEventListener(childEventListener);
                                                                                        }

                                                                                        @Override
                                                                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                } else if(seqAck == 5){

                                                                                    Toast.makeText(RiderMapActivity.this,"Driver is currently busy",Toast.LENGTH_LONG).show();

                                                                                    mDatabaseReference.child(Constants.PACKET).child(driverKey).removeEventListener(childEventListener);
                                                                                    mDatabaseReference.child(Constants.PAIR).child(driverKey).removeValue();

                                                                                    driverMarker.setVisible(true);

                                                                                } else if(seqAck == 7){

                                                                                    obtainPairKey = mDatabaseReference.child(Constants.PACKET).child(driverKey).child(Constants.PAIR_KEY);
                                                                                    obtainPairKey.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                            if(dataSnapshot.getValue(String.class) == null) {

                                                                                                Toast.makeText(RiderMapActivity.this,"Driver disconnected from app",Toast.LENGTH_LONG).show();

                                                                                                //ToDO: Needs correction
                                                                                                mDatabaseReference.child(Constants.PAIR).child(driverKey).removeValue();
                                                                                                mDatabaseReference.child(Constants.PACKET).child(driverKey).removeEventListener(childEventListener);
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                }
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

                                                                    mDatabaseReference.child(Constants.PAIR).child(driverKey).child(Constants.PAIR_KEY).setValue(mPushKey);

                                                                    transactions(6,1);

                                                                    mProgressDialog.setMessage("Confirming Driver Availability...");
                                                                    mProgressDialog.show();
                                                                    dialogBox.dismiss();
                                                                    if (text_box.getParent() != null) {
                                                                        ((ViewGroup) text_box.getParent()).removeView(text_box);
                                                                    }
                                                                }
                                                            });

                                                            declineButton.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    if (text_box.getParent() != null) {
                                                                        ((ViewGroup) text_box.getParent()).removeView(text_box);
                                                                    }

                                                                    driverMarker.setVisible(true);

                                                                    transactions(6,0);
                                                                    //ToDO: Don't force
                                                                    seqAck = 0;

                                                                    dialogBox.dismiss();
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                    return false;
                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        else if(checkAddress(geocodingDestinationResult,destinationLocation) && checkAddress(geocodingPickUpResult,pickUpLocation) && !checkTime(timeItOccurs)){
                            Intent intent = new Intent("SYN");

                            Toast.makeText(RiderMapActivity.this, "Requested Advanced Booking!", Toast.LENGTH_SHORT).show();

                            PlaceDetailsRequest placeDetailsRequest = PlacesApi.placeDetails(context,geocodingDestinationResult[0].placeId);
                            PlaceDetails placeDetails = placeDetailsRequest.awaitIgnoreError();

                            intent.putExtra("name", placeDetails.name);
                            intent.putExtra("address",geocodingDestinationResult[0].formattedAddress);
                            intent.putExtra("time",timeItOccurs);
                            intent.putExtra("date",appointmentDate + "-" + appointmentMonth);
                            intent.putExtra("status","yes");
                            intent.putExtra("notes","no additional notes");
                            sendBroadcast(intent);

                            obtainKey = mDatabaseReference.child(Constants.RIDER).child(userid).child(Constants.GEOKEY);
                            obtainKey.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    key = dataSnapshot.getValue(String.class);
                                    mPushKey = FirebaseDatabase.getInstance().getReference(Constants.TEXT_BOX + "/" + Constants.PAIR_KEY + "/").push().getKey();
                                    mDatabaseReference.child(Constants.PAIR).child(mPushKey).child(Constants.RIDER_KEY).setValue(key);
                                    mDatabaseReference.child(Constants.PAIR).child(mPushKey).child(Constants.IS_ADVANCED_BOOKING).setValue("true");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            mGeoFire.setLocation(mPushKey, new GeoLocation(geocodingPickUpResult[0].geometry.location.lat, geocodingPickUpResult[0].geometry.location.lng), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }
                            });

                            mGeoQuery = mPreferencedGeoFire.queryAtLocation(new GeoLocation(geocodingPickUpResult[0].geometry.location.lat, geocodingPickUpResult[0].geometry.location.lng), radius);

                            mGeoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                                @Override
                                public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {

                                    driverKey = dataSnapshot.getKey();
                                    Longitude = location.longitude;
                                    Latitude = location.latitude;

                                    obtainDistance = mDatabaseReference.child(Constants.PREFERENCE).child(driverKey).child(Constants.DISTANCE);
                                    obtainDistance.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            distance = dataSnapshot.getValue(Double.class);

                                            if(distance != null){
                                                if(distance >= radius){

                                                    obtainTime = mDatabaseReference.child(Constants.PREFERENCE).child(driverKey).child(Constants.TIME).child(Constants.TO);
                                                    obtainTime.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                                                if (childDataSnapshot.getKey() != null) {
                                                                    toTime.add(childDataSnapshot.getValue(String.class));
                                                                }
                                                            }

                                                            obtainTime = mDatabaseReference.child(Constants.PREFERENCE).child(driverKey).child(Constants.TIME).child(Constants.FROM);
                                                            obtainTime.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                                    for(DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                                                                        if (childDataSnapshot.getKey() != null){
                                                                            fromTime.add(childDataSnapshot.getValue(String.class));
                                                                        }
                                                                    }

                                                                    if (toTime != null && fromTime != null && toTime.size() > 0 && fromTime.size() < 0) {

                                                                        distanceMatrixApiRequest = DistanceMatrixApi.newRequest(context);

                                                                        com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(Latitude,Longitude);
                                                                        com.google.maps.model.LatLng destOrigin = new com.google.maps.model.LatLng(geocodingDestinationResult[0].geometry.location.lat,geocodingDestinationResult[0].geometry.location.lng);

                                                                        distanceMatrix = distanceMatrixApiRequest.origins(origin,destOrigin).destinations(pickUpLocation).language("en-US").mode(TravelMode.DRIVING).avoid(DirectionsApi.RouteRestriction.TOLLS).avoid(DirectionsApi.RouteRestriction.FERRIES).awaitIgnoreError();

                                                                        long RtoDTime = distanceMatrix.rows[0].elements[0].duration.inSeconds;
                                                                        long StoRTime = distanceMatrix.rows[1].elements[0].duration.inSeconds;

                                                                        if (availableTimes(toTime, fromTime, RtoDTime, StoRTime)) {

                                                                            obtainColor = mDatabaseReference.child(Constants.ONLINE).child(driverKey).child(Constants.CONNECTED);
                                                                            obtainColor.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                                                    online = dataSnapshot.getValue(String.class);

                                                                                    if (online != null) {

                                                                                        //ToDo: Obtain Color once again
                                                                                        if (online.equals("true")) {
                                                                                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car_white)));
                                                                                            marker.setTitle(online);
                                                                                        } else
                                                                                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));

                                                                                        marker.setTag(driverKey);
                                                                                        availableDrivers.add(marker);
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }

                                                                            });
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });
                                                        }


                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @Override
                                public void onDataExited(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                                }

                                @Override
                                public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

                                }

                                @Override
                                public void onGeoQueryReady() {

                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {

                                }
                            });

                            if(availableDrivers.isEmpty()){
                                Toast.makeText(RiderMapActivity.this,"No Drivers Available",Toast.LENGTH_LONG).show();
                            }
                            else
                                removeFields();

                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    driverMarker = marker;

                                    driverKey = (String) marker.getTag();

                                    mDriverReference = mDatabaseReference.child(Constants.PREFERENCE).child(driverKey).child(Constants.TIME);
                                    transactions(12,6);
                                    //ToDo: Don't force
                                    seqAck = 6;

                                    driverMarker.setVisible(false);

                                    obtainRating = mDatabaseReference.child(Constants.DRIVER).child(driverKey).child(Constants.USER_RATING);
                                    obtainRating.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            driverRating = dataSnapshot.getValue(String.class);
                                            String message = driverRating + "\n" + "Choose this Driver?";
                                            txt.setText(message);
                                            confirmation.setView(text_box);
                                            dialogBox = confirmation.create();
                                            dialogBox.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            dialogBox.show();

                                            acceptButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    childEventListener = mDatabaseReference.child(Constants.PREFERENCE).child(driverKey).child(Constants.TIME).addChildEventListener(new ChildEventListener() {
                                                        @Override
                                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                        }

                                                        @Override
                                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                                            if (dataSnapshot.getKey().equals(Constants.SEQ_ACK)) {

                                                                mProgressDialog.dismiss();
                                                                seqAck = dataSnapshot.getValue(Integer.class);

                                                                //ToDo: Fix listener, writing to incorrect nodes.
                                                                if (seqAck == 12 || seqAck == 4) {

                                                                    Toast.makeText(RiderMapActivity.this, "Driver is current unavailable", Toast.LENGTH_LONG).show();

                                                                    mDatabaseReference.child(Constants.PREFERENCE).child(driverKey).child(Constants.TIME).removeEventListener(childEventListener);

                                                                    availableDrivers.remove(driverMarker);
                                                                    driverMarker.remove();

                                                                    if(availableDrivers.size() == 0){
                                                                        returnFields();
                                                                    }

                                                                } else if (seqAck == 13) {

                                                                    mDatabaseReference.child(Constants.TEXT_BOX).child(mPushKey).removeValue();

                                                                    transactions(seqAck,14);

                                                                } else if (seqAck == 14) {
                                                                    saveText();

                                                                    mDatabaseReference.child(Constants.PAIR).child(mPushKey).child(Constants.DRIVER_KEY).setValue(driverKey);

                                                                    for (int j = 0; j < availableDrivers.size(); j++) {
                                                                        driver = availableDrivers.get(j);
                                                                        driver.remove();
                                                                    }

                                                                    Toast.makeText(RiderMapActivity.this, "Connected with Driver", Toast.LENGTH_LONG).show();

                                                                } else if(seqAck == 5){

                                                                    Toast.makeText(RiderMapActivity.this,"Driver is currently busy",Toast.LENGTH_LONG).show();

                                                                    mDatabaseReference.child(Constants.PAIR).child(driverKey).removeValue();

                                                                    driverMarker.setVisible(true);

                                                                } else if(seqAck == 7){

                                                                }
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

                                                    mDatabaseReference.child(Constants.PAIR).child(driverKey).child(Constants.PAIR_KEY).setValue(mPushKey);

                                                    transactions(6,13);

                                                    mProgressDialog.setMessage("Confirming Driver Availability...");
                                                    mProgressDialog.show();
                                                    dialogBox.dismiss();
                                                    if (text_box.getParent() != null) {
                                                        ((ViewGroup) text_box.getParent()).removeView(text_box);
                                                    }
                                                }
                                            });

                                            declineButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (text_box.getParent() != null) {
                                                        ((ViewGroup) text_box.getParent()).removeView(text_box);
                                                    }

                                                    driverMarker.setVisible(true);
                                                    transactions(6,12);
                                                    //ToDo: Don't Force
                                                    seqAck = 12;
                                                    dialogBox.dismiss();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    return false;
                                }
                            });

                        }
                    }
                } catch(IllegalArgumentException e){
                    Toast.makeText(RiderMapActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start location updates. Will ask for permissions if necessary
        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mapLocationPermissionsGranted) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Location permissions not granted!", Toast.LENGTH_SHORT).show();
        }

        obtainKey = mDatabaseReference.child(Constants.RIDER).child(userid).child(Constants.GEOKEY);
        obtainKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                key = dataSnapshot.getValue(String.class);

                obtainPairKey = mDatabaseReference.child(Constants.PAIR).child(key).child(Constants.PAIR_KEY);
                obtainPairKey.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        pairKey = dataSnapshot.getValue(String.class);

                        if (pairKey != null) {

                            obtainDriverKey = mDatabaseReference.child(Constants.PAIR).child(pairKey).child(Constants.DRIVER_KEY);
                            obtainDriverKey.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    driverKey = dataSnapshot.getValue(String.class);

                                    if (driverKey != null) {

                                        obtainIsAvailable = mDatabaseReference.child(Constants.PAIR).child(driverKey).child(Constants.IS_ADVANCED_BOOKING);
                                        obtainIsAvailable.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                isAvailable = dataSnapshot.getValue(String.class);

                                                if(isAvailable != null){

                                                    removeFields();

                                                    if(isAvailable.equals("false")){

                                                        mGeoFire.getLocation(driverKey, new com.firebase.geofire.LocationCallback() {
                                                            @Override
                                                            public void onLocationResult(String key, GeoLocation location) {
                                                                driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                                                                driverMarker.setTag(driverKey);

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                        mLocation.getLocation(pairKey, new com.firebase.geofire.LocationCallback() {
                                                            @Override
                                                            public void onLocationResult(String key, GeoLocation location) {
                                                                if (location != null) {
                                                                    locMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                                                                    locMarker.setTag(pairKey);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                        driverEventListener = mDatabaseReference.child(Constants.GEO_LOCATION).child(driverKey).addChildEventListener(new ChildEventListener() {
                                                            @Override
                                                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                                            }

                                                            @Override
                                                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                                                mGeoFire.getLocation(driverKey, new com.firebase.geofire.LocationCallback() {
                                                                    @Override
                                                                    public void onLocationResult(String key, GeoLocation location) {
                                                                        if(driverMarker != null) {
                                                                            driverMarker.remove();
                                                                        }

                                                                        driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_blue)));
                                                                        driverMarker.setTag(driverKey);

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
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
                                                    else {

                                                        childEventListener = mDatabaseReference.child(Constants.PACKET).child(driverKey).addChildEventListener(new ChildEventListener() {
                                                            @Override
                                                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                                            }

                                                            @Override
                                                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                                                if(dataSnapshot.getKey().equals(Constants.SEQ_ACK)){

                                                                    seqAck = dataSnapshot.getValue(Integer.class);

                                                                    //ToDo: Turn seqAck 3 into function
                                                                    if(seqAck == 3){

                                                                        mGeoFire.getLocation(driverKey, new com.firebase.geofire.LocationCallback() {
                                                                            @Override
                                                                            public void onLocationResult(String key, GeoLocation location) {
                                                                                driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                                                                                driverMarker.setTag(driverKey);

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                        mLocation.getLocation(pairKey, new com.firebase.geofire.LocationCallback() {
                                                                            @Override
                                                                            public void onLocationResult(String key, GeoLocation location) {
                                                                                if (location != null) {
                                                                                    locMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                                                                                    locMarker.setTag(pairKey);
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                        driverEventListener = mDatabaseReference.child(Constants.GEO_LOCATION).child(driverKey).addChildEventListener(new ChildEventListener() {
                                                                            @Override
                                                                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                                                            }

                                                                            @Override
                                                                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                                                                mGeoFire.getLocation(driverKey, new com.firebase.geofire.LocationCallback() {
                                                                                    @Override
                                                                                    public void onLocationResult(String key, GeoLocation location) {
                                                                                        if(driverMarker != null) {
                                                                                            driverMarker.remove();
                                                                                        }

                                                                                        driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_blue)));
                                                                                        driverMarker.setTag(driverKey);

                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });
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

                                                                    else if(seqAck == 7){
                                                                        //ToDO: Reset Connection
                                                                    }

                                                                    else if(seqAck == 10){
                                                                        mDatabaseReference.child(Constants.PACKET).child(driverKey).child(Constants.SEQ_ACK).setValue(11);
                                                                    }
                                                                    else if(seqAck == 11){
                                                                        mDatabaseReference.child(Constants.PAIR).child(mPushKey).child(Constants.IS_ADVANCED_BOOKING).setValue("false");
                                                                    }
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
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getDeviceLocation();
    }

    // Prevent battery drain when activity is not in focus
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

        if(seqAck != null){
            if(seqAck == 6){
                wipeText();
            }
        }

        obtainKey = mDatabaseReference.child(Constants.RIDER).child(userid).child(Constants.GEOKEY);
        obtainKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                key = dataSnapshot.getValue(String.class);

                if(key != null) {

                    obtainPairKey = mDatabaseReference.child(Constants.PAIR).child(key).child(Constants.PAIR_KEY);
                    obtainPairKey.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            pairKey = dataSnapshot.getValue(String.class);

                            if (pairKey != null) {

                                obtainDriverKey = mDatabaseReference.child(Constants.PAIR).child(pairKey).child(Constants.DRIVER_KEY);
                                obtainDriverKey.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        driverKey = dataSnapshot.getValue(String.class);

                                        if (driverKey != null) {
                                            if (driverEventListener != null) {
                                                mDatabaseReference.child(Constants.GEO_LOCATION).child(driverKey).removeEventListener(driverEventListener);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: STOPPED LOCATION UPDATES");
        if (mapFusedLocationProviderClient != null) {
            if (mLocationCallback != null) {
                mapFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            }
        }
    }

    private void startLocationUpdates() {
        if (mapLocationPermissionsGranted) {
            try {
                Log.d(TAG, "startLocationUpdates: STARTED LOCATION UPDATES");
                mapFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            } catch (SecurityException e) {
                Log.d(TAG, "startLocationUpdates: " + e.getMessage());
            }
        } else {
            getLocationPermissions();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(timeItOccurs != null) {
            if(checkTime(timeItOccurs)) {
                mGeoFire.setLocation(key, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });
            }
        }

        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        //mMap.animateCamera(cameraUpdate);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        padGoogleMap();
        // Add a marker in Sydney and move the camera (Default thing from google maps)
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (mapLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(true);

            //mMap.getUiSettings().setMyLocationButtonEnabled(false); // Hides the "locate me" button
        }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        destinationMarker = null;
        pickUpMarker = null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mapLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: Permissions not granted!");
                            return;
                        }
                    }

                    mapLocationPermissionsGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: Permissions granted!");
                    initializeMap();
                    startLocationUpdates();
                }
            }
        }
    }

    private void initializeMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getDeviceLocation() {

        try {
            if (mapLocationPermissionsGranted) {

                final Task location = mapFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location currentLocation = (Location) task.getResult();
                            Log.d(TAG, "onComplete: found location. ");
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);


                            if(timeItOccurs != null) {
                                if(checkTime(timeItOccurs)) {

                                    mGeoFire.setLocation(key, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoFire.CompletionListener() {
                                        @Override
                                        public void onComplete(String key, DatabaseError error) {

                                        }
                                    });

                                    mGeoQuery = mAvailableGeoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), radius);
                                }
                            }

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(RiderMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }

    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat:" + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getLocationPermissions() {
        String[] permissions = {FINE_LOCATION, COURSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mapLocationPermissionsGranted = true;
                initializeMap();
                Log.d(TAG, "getLocationPermissions: Have permissions. Starting Updates");
                startLocationUpdates();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initWidgets() {

        setPickupBtn = (Button) findViewById(R.id.setPickupBtn_ridermap);
        text_box = getLayoutInflater().inflate(R.layout
                .text_box, null);
        acceptButton = (Button) text_box.findViewById(R.id.acceptButton);
        declineButton = (Button) text_box.findViewById(R.id.declineButton);
        txt = (TextView) text_box.findViewById(R.id.rider_name);
        title = (TextView) text_box.findViewById(R.id.message);
        name = (TextView) text_box.findViewById(R.id.inc_request_hospital_name);
        destination = findViewById(R.id.destination);
        time = findViewById(R.id.time);
        pickUp = findViewById(R.id.pickUp);
        guideline = findViewById(R.id.riderMapGuideline);
        //box1 = findViewById(R.id.box1);
        //box2 = findViewById(R.id.box2);
        //box3 = findViewById(R.id.box3);
        mProgressDialog = new ProgressDialog(RiderMapActivity.this);

        pickUp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!pickUp.getText().toString().isEmpty()) {

                    pickUpLocation = pickUp.getText().toString().trim();

                    geocodingApiRequestPickUp = GeocodingApi.newRequest(context);
                    geocodingApiRequestPickUp.address(pickUpLocation);
                    geocodingPickUpResult = geocodingApiRequestPickUp.awaitIgnoreError();

                    if (checkAddress(geocodingPickUpResult, pickUpLocation)) {
                        if (mMap != null) {
                            pickUpMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(geocodingPickUpResult[0].geometry.location.lat, geocodingPickUpResult[0].geometry.location.lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_blue)));
                            LatLng latLng = new LatLng(geocodingPickUpResult[0].geometry.location.lat, geocodingPickUpResult[0].geometry.location.lng);
                            moveCamera(latLng, mMap.getCameraPosition().zoom);
                        }
                    }
                }
            }
        });

        destination.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!destination.getText().toString().isEmpty()) {

                    destinationLocation = destination.getText().toString().trim();

                    geocodingApiRequestDestination = GeocodingApi.newRequest(context);
                    geocodingApiRequestDestination.address(destinationLocation);
                    geocodingDestinationResult = geocodingApiRequestDestination.awaitIgnoreError();

                    if (checkAddress(geocodingDestinationResult, destinationLocation)) {
                        if (mMap != null) {
                            destinationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(geocodingDestinationResult[0].geometry.location.lat, geocodingDestinationResult[0].geometry.location.lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_red)));
                            LatLng latLng = new LatLng(geocodingDestinationResult[0].geometry.location.lat, geocodingDestinationResult[0].geometry.location.lng);
                            moveCamera(latLng, mMap.getCameraPosition().zoom);
                        }
                    }
                }
            }
        });



        // Add <- arrow on actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(RiderMapActivity.this, RiderActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }



    // Pad map appropriately to not obscure google logo/copyright info
    // This is a generic function, wont look nice on most devices
    // probably needs some math to calculate padding size (its in pixels)
    private void padGoogleMap() {
        //    //int[] locationOnScreen; // [x, y]
        //    //findViewById(R.id.setPickupBtn_ridermap).getLocationOnScreen(locationOnScreen);

        //    // left, top, right, bottom
        mMap.setPadding(0, 0, 0, 0);

    }

    private void callBack() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if(timeItOccurs != null) {
                    if(checkTime(timeItOccurs)) {

                        mGeoFire.setLocation(key, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {

                            }
                        });

                        mGeoQuery = mAvailableGeoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), radius);
                    }
                }

                //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                //mMap.animateCamera(cameraUpdate);

            }
        };
    }

    private void removeFields(){
        setPickupBtn.setVisibility(View.GONE);
        //box1.setVisibility(View.GONE);
        //box2.setVisibility(View.GONE);
        //box3.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)guideline.getLayoutParams();
        params.guideBegin=-10;
        guideline.setLayoutParams(params);
    }

    private void returnFields(){
        setPickupBtn.setVisibility(View.VISIBLE);
        //box1.setVisibility(View.VISIBLE);
        //box2.setVisibility(View.VISIBLE);
        //box3.setVisibility(View.VISIBLE);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)guideline.getLayoutParams();
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, r.getDisplayMetrics());
        params.guideBegin=(int)px;
        guideline.setLayoutParams(params);
    }

    private boolean checkPickUp(EditText pickUpField, EditText destinationField, EditText timeField){

        boolean flag = true;

        pickUpLocation = pickUpField.getText().toString().trim();
        destinationLocation = destinationField.getText().toString().trim();
        timeItOccurs = timeField.getText().toString().trim();

        if(pickUpLocation.isEmpty()) {
            pickUpField.setError("Pick up location required");
            flag = false;
        }
        if(destinationLocation.isEmpty()) {
            destinationField.setError("Destination required");
            flag = false;
        }
        if(timeItOccurs.isEmpty()) {
            timeField.setError("Time to be picked up required");
            flag = false;
        }

        return flag;
    }

    private boolean checkTime(String time){
        if(time.length() == 6) {

            /*
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hhmmaa", Locale.ENGLISH);

            try {
                Date format = simpleDateFormat.parse(time);
                Calendar formatAgain = Calendar.getInstance();
                formatAgain.setTime(format);

                int currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int hourDiff = formatAgain.get(Calendar.HOUR_OF_DAY) - currentTime;
                int currentMinutes = Calendar.getInstance().get(Calendar.MINUTE);
                int minuteDiff = formatAgain.get(Calendar.MINUTE) - currentMinutes;

                hourDiff = Math.abs(hourDiff);

                if(hourDiff == 0 || hourDiff == 1 || hourDiff == 23){
                    appointmentDate = Calendar.DATE + "";
                    appointmentMonth = Calendar.MONTH + "";
                    return true;
                }

                if(currentTime > formatAgain.get(Calendar.HOUR_OF_DAY)){
                    appointmentDate =  (Calendar.DATE + 1) + "";
                    appointmentMonth = Calendar.MONTH + "";
                    return false;
                }

                else{
                    appointmentDate = (Calendar.DATE + 1) + "";
                    appointmentMonth = Calendar.MONTH + "";
                    return false;
                }


            } catch (ParseException e) {
                Toast.makeText(RiderMapActivity.this, "Time format entered incorrectly", Toast.LENGTH_LONG).show();
            }
            return false;
        }else{
            Toast.makeText(RiderMapActivity.this,"Time format entered incorrectly",Toast.LENGTH_LONG).show();
            return false;
            */
        }
        return true;
    }

    private boolean checkAddress(GeocodingResult[] address, String addressLocation){

        if((address == null || address.length == 0) && !addressLocation.isEmpty()){
            Toast.makeText(RiderMapActivity.this,addressLocation + " is not a valid address",Toast.LENGTH_LONG).show();
            return false;
        }

        if(addressLocation.isEmpty())
            return false;

        return true;
    }

    private void killText(){
        mDatabaseReference.child(Constants.TEXT_BOX).child(mPushKey).onDisconnect().removeValue();
        mDatabaseReference.child(Constants.PAIR).child(mPushKey).onDisconnect().removeValue();
        mDatabaseReference.child(Constants.PAIR).child(key).child(Constants.PAIR_KEY).onDisconnect().removeValue();
        mDatabaseReference.child(Constants.GEO_LOCATION).child(mPushKey).onDisconnect().removeValue();
        mDatabaseReference.child(Constants.GEO_LOCATION).child(key).onDisconnect().removeValue();
    }

    private void killRaceCondition(){
        mDatabaseReference.child(Constants.PACKET).child(driverKey).removeEventListener(childEventListener);
        mDatabaseReference.child(Constants.PAIR).child(driverKey).removeValue();
    }

    private void wipeAllText(){
        mDatabaseReference.child(Constants.TEXT_BOX).child(mPushKey).removeValue();
        mDatabaseReference.child(Constants.PAIR).child(mPushKey).removeValue();
        mDatabaseReference.child(Constants.PAIR).child(key).child(Constants.PAIR_KEY).removeValue();
        mDatabaseReference.child(Constants.GEO_LOCATION).child(mPushKey).removeValue();
        mDatabaseReference.child(Constants.GEO_LOCATION).child(key).removeValue();
    }

    private void wipeText(){
        mDatabaseReference.child(Constants.TEXT_BOX).child(mPushKey).removeValue();
        mDatabaseReference.child(Constants.PAIR).child(mPushKey).removeValue();
        mDatabaseReference.child(Constants.PAIR).child(key).child(Constants.PAIR_KEY).removeValue();
    }

    private void saveText(){
        mDatabaseReference.child(Constants.PAIR).child(mPushKey).onDisconnect().cancel();
        mDatabaseReference.child(Constants.PAIR).child(key).child(Constants.PAIR_KEY).onDisconnect().cancel();
        mDatabaseReference.child(Constants.GEO_LOCATION).child(key).onDisconnect().cancel();
        mDatabaseReference.child(Constants.GEO_LOCATION).child(mPushKey).onDisconnect().cancel();
    }

    //ToDO: do conversion for all times by adding the minutes of RtoD and StoR to fromTimeCalendar before if statement
    private boolean availableTimes(List<String> to, List<String> from, Long RtoD, Long StoR){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hhmmaa", Locale.ENGLISH);
        try {
            Date formatTime = simpleDateFormat.parse(timeItOccurs);
            Calendar format = Calendar.getInstance();
            format.setTime(formatTime);

            Date toTimeDate;
            Date fromTimeDate;
            toTimeCalendar = Calendar.getInstance();
            fromTimeCalendar = Calendar.getInstance();

            for (int i = 0; i < to.size(); i++) {

                toTimeDate = simpleDateFormat.parse(to.get(i));
                toTimeCalendar.setTime(toTimeDate);

                fromTimeDate = simpleDateFormat.parse(from.get(i));
                fromTimeCalendar.setTime(fromTimeDate);

                if(format.after(fromTimeCalendar) && format.before(toTimeCalendar)){

                    obtainTime = mDatabaseReference.child(Constants.PREFERENCE).child(driverKey).child(Constants.TIME).child(Constants.TO);
                    obtainTime.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                                if (childDataSnapshot.getKey() != null) {
                                    if(childDataSnapshot.getKey().equals(toTimeCalendar.toString())) {
                                        mDatabaseReference.child(Constants.PAIR).child(key).child(Constants.PAIR_KEY).child(mPushKey).setValue(toTimeCalendar);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    return true;
                }

            }
        }catch(ParseException e){}

        return false;
    }

    private void transactions(final int seqNumber, final int value){
            mDriverReference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    if (mutableData.getValue() != null) {
                        long data = (long) mutableData.getValue();
                        if (seqNumber == data) {
                            mutableData.setValue(value);
                        }
                        else if(data == 0 || data == 6){
                            mutableData.setValue(value);
                        }
                        else if(data == 4 || data == 5 || data == 7){
                            Toast.makeText(RiderMapActivity.this,"Driver is no longer available",Toast.LENGTH_LONG).show();
                            killRaceCondition();
                        }
                        else if(((data == 2 || data == 3) && value == 1) || (value == 6 && (data == 1 || data == 2 || data == 3))){
                            Toast.makeText(RiderMapActivity.this,"Driver has already been paired with another rider",Toast.LENGTH_LONG).show();
                            killRaceCondition();
                        }
                        else if((value == 2 || value == 3) && data == 1){
                            mutableData.setValue(value);
                        }
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    if(databaseError != null)
                        Log.wtf(TAG,databaseError.getMessage());
                }
            });

    }
}










