package maderski.bluetoothautoplaymusic.controls.wakelockcontrol;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

/**
 * Created by Jason on 2/7/16.
 */
public class SunriseSunset {

    private static final String TAG = SunriseSunset.class.getName();

    private String sunriseTime;
    private String sunsetTime;

    //Get Sunrise and Sunset Times as Strings
    public SunriseSunset(Context context){
        Calendar today = Calendar.getInstance();
        CurrentLocation currentLocation = new CurrentLocation(context);
        Location location = new Location(currentLocation.getLatitude(), currentLocation.getLongitude());
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, Calendar.getInstance().getTimeZone());
        Log.d(TAG, "Lat " + currentLocation.getLatitude() + " Long " + currentLocation.getLongitude());
        sunriseTime = calculator.getCivilSunriseForDate(today);
        sunsetTime = calculator.getCivilSunsetForDate(today);
        Log.d(TAG, "sunrise: " + sunriseTime + " sunset: " + sunsetTime);
    }

    //Return Sunrise time as a single integer number
    public int getSunrise(){
        return getTime(sunriseTime);
    }

    //Return Sunrise time as a single integer number
    public int getSunset(){
        return getTime(sunsetTime);
    }

    //Convert Time String into a single integer number
    private int getTime(String inputTime){
        String[] split = inputTime.split(":");
        int outputTime = (Integer.parseInt(split[0]) * 100) + Integer.parseInt(split[1]);
        Log.d(TAG, Integer.toString(outputTime));
        return outputTime;
    }
}