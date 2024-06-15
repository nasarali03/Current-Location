package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private AppDatabase db;
    private FavoriteLocationDao favoriteLocationDao;
    private RecyclerView recyclerView;
    private FavoriteLocationAdapter adapter;
    private List<FavoriteLocation> favoriteLocations;

    private LatLng currentLocation;
    private LatLng selectedLocation1;
    private LatLng selectedLocation2;

    private EditText nameEditText;
    private Button searchButton;
    private Button saveButton;
    private Button distanceButton;
    private Button currentLocationButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000000000);
        locationRequest.setFastestInterval(50000000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "favorite-locations").allowMainThreadQueries().build();
        favoriteLocationDao = db.favoriteLocationDao();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteLocations = favoriteLocationDao.getAll();
        adapter = new FavoriteLocationAdapter(favoriteLocations, this::onFavoriteLocationClick, this::onFavoriteLocationEdit, this::onFavoriteLocationDelete);
        recyclerView.setAdapter(adapter);

        nameEditText = findViewById(R.id.name_edit_text);
        searchButton = findViewById(R.id.search_button);
        saveButton = findViewById(R.id.save_button);
        distanceButton = findViewById(R.id.distance_button);

        searchButton.setOnClickListener(v -> searchLocation());
        saveButton.setOnClickListener(v -> saveCurrentLocation());
        distanceButton.setOnClickListener(v -> calculateDistanceBetweenSelectedPoints());

        ImageButton currentLocButton = findViewById(R.id.currentLocButton);
        currentLocButton.setOnClickListener(v -> moveToCurrentLocation());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void updateLocation(Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                Toast.makeText(this, "Current Address: " + address, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchLocation() {
        String locationName = nameEditText.getText().toString();
        if (locationName.isEmpty()) {
            Toast.makeText(this, "Please enter a location name", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                currentLocation = latLng;
                Toast.makeText(this, "Location found: " + locationName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in searching location", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentLocation() {
        String name = nameEditText.getText().toString();
        if (name.isEmpty() || currentLocation == null) {
            Toast.makeText(this, "Please enter a name and ensure location is available", Toast.LENGTH_SHORT).show();
            return;
        }
        FavoriteLocation favoriteLocation = new FavoriteLocation();
        favoriteLocation.name = name;
        favoriteLocation.latitude = currentLocation.latitude;
        favoriteLocation.longitude = currentLocation.longitude;
        favoriteLocationDao.insert(favoriteLocation);

        favoriteLocations.add(favoriteLocation);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Location saved: " + name, Toast.LENGTH_SHORT).show();
    }

    private void onFavoriteLocationClick(FavoriteLocation favoriteLocation) {
        LatLng favoriteLatLng = new LatLng(favoriteLocation.latitude, favoriteLocation.longitude);
        mMap.addMarker(new MarkerOptions().position(favoriteLatLng).title(favoriteLocation.name));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(favoriteLatLng, 15));

        if (selectedLocation1 == null) {
            selectedLocation1 = favoriteLatLng;
            Toast.makeText(this, "Selected first point", Toast.LENGTH_SHORT).show();
        } else if (selectedLocation2 == null) {
            selectedLocation2 = favoriteLatLng;
            Toast.makeText(this, "Selected second point", Toast.LENGTH_SHORT).show();
        } else {
            selectedLocation1 = favoriteLatLng;
            selectedLocation2 = null;
            Toast.makeText(this, "Reset selection. Selected first point again", Toast.LENGTH_SHORT).show();
        }
    }

    private void onFavoriteLocationEdit(FavoriteLocation favoriteLocation) {
        nameEditText.setText(favoriteLocation.name);
        currentLocation = new LatLng(favoriteLocation.latitude, favoriteLocation.longitude);

        favoriteLocation.name = nameEditText.getText().toString();
        favoriteLocationDao.update(favoriteLocation);
        adapter.notifyDataSetChanged();
    }

    private void onFavoriteLocationDelete(FavoriteLocation favoriteLocation) {
        favoriteLocationDao.delete(favoriteLocation);
        favoriteLocations.remove(favoriteLocation);
        adapter.notifyDataSetChanged();
    }



    private void calculateDistanceBetweenSelectedPoints() {
        if (selectedLocation1 != null && selectedLocation2 != null) {
            double distance = calculateDistance(selectedLocation1, selectedLocation2);
            double distanceInKilometers = distance / 1000; // Convert meters to kilometers
            DecimalFormat decimalFormat = new DecimalFormat("#.##"); // Format to two decimal places
            String formattedDistance = decimalFormat.format(distanceInKilometers);
            Toast.makeText(this, "Distance: " + formattedDistance + " kilometers", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please select two points", Toast.LENGTH_SHORT).show();
        }
    }


    private double calculateDistance(LatLng point1, LatLng point2) {
        float[] results = new float[1];
        Location.distanceBetween(point1.latitude, point1.longitude, point2.latitude, point2.longitude, results);
        return results[0];
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }
    }
    private void moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                Toast.makeText(this, "Moved to Current Location", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
