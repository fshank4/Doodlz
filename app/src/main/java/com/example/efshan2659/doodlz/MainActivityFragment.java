// MainActivityFragment.java
// Fragment in which the DoodleView is displayed
package com.example.efshan2659.doodlz;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.jar.Manifest;

public class MainActivityFragment extends Fragment {
    private DoodleView doodleView; // handles touch events and draws
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private boolean dialogOnScreen = false;

    // value used to determine whether the user shook the device to erase
    private static final int ACCELERATION_THRESHOLD = 100000;

    // used to identify the request for using external storage, which
    // the save image feature needs
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =
                inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true); // this fragment has menu items to display

        // get reference to the DoodleView
        doodleView = (DoodleView) view.findViewById(R.id.doodleView);

        // initialize acceleration values
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
        return view;
    }

    // start listening for sensor events
    @Override
    public void onResume() {
        super.onResume();
        enableAccelerometerListening(); // listen for shake event
    }

    // enable listening for accelerometer events
    private void enableAccelerometerListening() {
        // get the SensorManager
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(
                        Context.SENSOR_SERVICE);

        // register to listen for accelerometer events
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    // stop listening for accelerometer events
    @Override
    public void onPause() {
        super.onPause();
        disableAccelerometerListening(); // stop listening for shake
    }

    // disable listening for accelerometer events
    private void disableAccelerometerListening() {
        // get the SensorManager
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(
                        Context.SENSOR_SERVICE);

        // stop listening for accelerometer events
        sensorManager.unregisterListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    // event handler for accelerometer events
    private final SensorEventListener sensorEventListener =
            new SensorEventListener() {
                // use accelerometer to determine whether user shook device
                @Override
                public void onSensorChanged(SensorEvent event) {
                    // ensure that other dialogs are not displayed
                    if (!dialogOnScreen) {
                        // get x, y, and z values for the SensorEvent
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];

                        // save previous acceleration value
                        lastAcceleration = currentAcceleration;

                        // calculate the current acceleration
                        currentAcceleration = x * x + y * y + z * z;

                        // calculate the change in acceleration
                        acceleration = currentAcceleration *
                                (currentAcceleration - lastAcceleration);

                        // if the acceleration is above a certain threshold
                        if (acceleration > ACCELERATION_THRESHOLD)
                            confirmErase();
                    }
                }

                // required method of interface SensorEventListener
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };

    // confirm whether image should be erased
    private void confirmErase() {
        EraseImageDialogFragment fragment = new EraseImageDialogFragment();
        fragment.show(getFragmentManager(), "erase dialog");
    }

    // displays the fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.doodle_fragment_menu, menu);
    }

    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // switch based on the MenuItem id
        switch (item.getItemId()) {
            case R.id.color:
                ColorDialogFragment colorDialog = new ColorDialogFragment();
                colorDialog.show(getFragmentManager(), "color dialog");
                return true; // consume the menu event
            case R.id.line_width:
                LineWidthDialogFragment widthDialog =
                        new LineWidthDialogFragment();
                widthDialog.show(getFragmentManager(), "line width dialog");
                return true; // consume the menu event
            case R.id.delete_drawing:
                confirmErase(); // confirm before erasing image
                return true; // consume the menu event
            case R.id.save:
                saveImage(); // check permission and save current image
                return true; // consume the menu event
            case R.id.print:
                doodleView.printImage(); // print the current images
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
