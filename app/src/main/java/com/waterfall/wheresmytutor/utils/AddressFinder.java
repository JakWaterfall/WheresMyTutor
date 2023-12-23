package com.waterfall.wheresmytutor.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;

import com.waterfall.wheresmytutor.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddressFinder {
    static final private int MAX_RESULTS = 1;
    static final private int FIRST_POSITION_INDEX = 0;

    static public void getAddress(Context context, double latitude, double longitude, DatabaseController.StringCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor(); // fetch address tread
        Handler handler = new Handler(Looper.getMainLooper());          // UI thread

        executor.execute(() -> {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(context, Locale.getDefault());

            String address;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, MAX_RESULTS);
                if (addresses.isEmpty())
                {
                    address = context.getString(R.string.no_address_found_text);
                }
                else
                {
                    address = addresses.get(FIRST_POSITION_INDEX).getAddressLine(FIRST_POSITION_INDEX);
                }

            }
            catch (IOException e)
            {
                address = context.getString(R.string.no_address_found_text);
            }

            String finalAddress = address;
            handler.post(() -> callback.onCallback(finalAddress)); // Callback should be sent on the UI thread in case the address needs to be placed into a UI element
        });
    }
}


