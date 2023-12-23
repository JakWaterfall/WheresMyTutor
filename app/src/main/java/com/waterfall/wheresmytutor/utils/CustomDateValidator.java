package com.waterfall.wheresmytutor.utils;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.google.android.material.datepicker.CalendarConstraints;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/*
* Class Inspiration from https://github.com/material-components/material-components-android/blob/master/catalog/java/io/material/catalog/datepicker/DateValidatorWeekdays.java
* By Material Components at https://github.com/material-components
* */
public class CustomDateValidator implements CalendarConstraints.DateValidator {

    private static List<Integer> availabilityDaysOfTheWeek;
    private final Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    public static final Creator<CustomDateValidator> CREATOR =
            new Creator<CustomDateValidator>() {
                @Override
                public CustomDateValidator createFromParcel(Parcel source) {
                    return new CustomDateValidator(availabilityDaysOfTheWeek);
                }

                @Override
                public CustomDateValidator[] newArray(int size) {
                    return new CustomDateValidator[size];
                }
            };

    public CustomDateValidator(List<Integer> availabilityDaysOfTheWeek) {
        CustomDateValidator.availabilityDaysOfTheWeek = availabilityDaysOfTheWeek;

    }

    @Override
    public boolean isValid(long date) {
        utc.setTimeInMillis(date);
        int dayOfWeek = utc.get(Calendar.DAY_OF_WEEK);
        return availabilityDaysOfTheWeek.contains(dayOfWeek);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

    }
}
