package com.geoape.backgroundlocationexample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;
import java.util.Date;

public class BackgroundService extends Service {
  private final LocationServiceBinder binder = new LocationServiceBinder();
  private final String TAG = "BackgroundService";
  private LocationListener mLocationListener;
  private GoogleApiClient mGoogleApiClient;
  private LocationRequest locationRequest;
  LocationListener connectionCallbacks;
  LocationListener connectionFailedListener;
  private final int LOCATION_INTERVAL = 0;

  @Override public IBinder onBind(Intent intent) {
    return binder;
  }

  private class LocationListener implements com.google.android.gms.location.LocationListener,
      GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = "LocationListener";

    public LocationListener() {
    }

    @Override public void onLocationChanged(Location location) {
      Log.e(TAG, "LocationChanged: " + location.getLatitude());
      Intent intent = new Intent("result");
      intent.putExtra("lat", location.getLatitude());
      sendBroadcast(intent);
    }

    @Override public void onConnected(Bundle bundle) {
      startLocationUpdates();
    }

    @Override public void onConnectionSuspended(int i) {

    }

    @Override public void onConnectionFailed(ConnectionResult connectionResult) {

    }
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    return START_NOT_STICKY;
  }

  @Override public void onCreate() {
    Log.i(TAG, "onCreate");
    createLocationRequest();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForeground(12345678, getNotification());
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
  }

  public synchronized void buildGoogleApiClient() {
    connectionCallbacks = connectionFailedListener = mLocationListener = new LocationListener();
    mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
        .addConnectionCallbacks(connectionCallbacks)
        .addOnConnectionFailedListener(connectionFailedListener)
        .build();
    mGoogleApiClient.connect();
  }

  protected void startLocationUpdates() {
    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest,
        mLocationListener);
  }

  protected void createLocationRequest() {
    locationRequest = new LocationRequest();
    locationRequest.setInterval(LOCATION_INTERVAL);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
  }

  public void stopTracking() {
    this.onDestroy();
  }

  @RequiresApi(api = Build.VERSION_CODES.O) private Notification getNotification() {

    NotificationChannel channel =
        new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(channel);

    Notification.Builder builder =
        new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);
    return builder.build();
  }

  public class LocationServiceBinder extends Binder {
    public BackgroundService getService() {
      return BackgroundService.this;
    }
  }
}
