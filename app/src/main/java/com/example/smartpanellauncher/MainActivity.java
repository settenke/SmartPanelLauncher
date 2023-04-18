package com.example.smartpanellauncher;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements GestureDetector.OnGestureListener {
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast?latitude={{latitude}}&longitude={{longitude}}&current_weather=true";
    private float x1, x2, y1, y2;
    private static final int MIN_DISTANCE = 150;
    private GestureDetector gestureDetector;
    private SharedPreferences sharedPref;
    private int weatherBackground = R.color.black;
    private boolean isIdle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load settings
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // load gesture detector
        this.gestureDetector = new GestureDetector(MainActivity.this, this);

        // set clock
        TextClock textClock = (TextClock) findViewById(R.id.digitalClock);
        textClock.setFormat12Hour("kk:mm");

        // get temp container
        TextView currentTemp = (TextView) findViewById(R.id.temperature);
        ImageView currentWeatherIcon = (ImageView) findViewById(R.id.weatherIcon);
        ConstraintLayout mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);

        // create request queue
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);

        // register timer
        Timer timer = new Timer();

        // get current weather
        JsonObjectRequest jsonObjectRequest = registerWeatherApiRequest(currentTemp, currentWeatherIcon, mainLayout);
        requestQueue.add(jsonObjectRequest);

        // secs offset
        int secsUntilOnTheMinute = 60-Calendar.getInstance().get(Calendar.SECOND);

        // update current date every hours
        TimerTask updateWeather = getWeatherUpdater(currentTemp, currentWeatherIcon, mainLayout, requestQueue);
        timer.schedule(updateWeather, secsUntilOnTheMinute*1000,60 * 60 * 1000);

        // set current date
        String formattedDate = getFormattedDate();

        TextView currentDate = (TextView) findViewById(R.id.currentDate);
        currentDate.setText(formattedDate);

        // update current date every minutes
        TimerTask updateDate = getDateUpdater(currentDate);
        timer.schedule(updateDate, secsUntilOnTheMinute*1000,60 * 1000);
    }

    @Override
    protected void reactToIdleState() {
        super.reactToIdleState();
        ConstraintLayout mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        mainLayout.setBackgroundResource(R.color.black);
        isIdle = true;
    }

    @Override
    protected void myCustomOnClick(View view) {
        super.myCustomOnClick(view);
        ConstraintLayout mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        mainLayout.setBackgroundResource(weatherBackground);
        isIdle = false;
    }

    @NonNull
    private TimerTask getDateUpdater(TextView currentDate) {
        return new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String newDate = getFormattedDate();
                        currentDate.setText(newDate);
                    }
                });
            }
        };
    }

    @NonNull
    private TimerTask getWeatherUpdater(TextView currentTemp, ImageView currentWeatherIcon, ConstraintLayout mainLayout, RequestQueue requestQueue) {
        return new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JsonObjectRequest jsonObjectRequest = registerWeatherApiRequest(currentTemp, currentWeatherIcon, mainLayout);
                        requestQueue.add(jsonObjectRequest);
                    }
                });
            }
        };
    }

    @NonNull
    private JsonObjectRequest registerWeatherApiRequest(TextView currentTemp, ImageView currentWeatherIcon, ConstraintLayout mainLayout) {
        String latitude = this.sharedPref.getString("tempLatitude", "47.49");
        String longitude = this.sharedPref.getString("tempLongitude", "19.04");

        String finalWeatherUrl = WEATHER_URL.replace("{{latitude}}", latitude).replace("{{longitude}}", longitude);

        return new JsonObjectRequest(Request.Method.GET,
                finalWeatherUrl, null, new Response.Listener<JSONObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String currentTempValue = response.getJSONObject("current_weather").getString("temperature");
                    int currentWeatherCode = response.getJSONObject("current_weather").getInt("weathercode");
                    Log.d("myapp", "The response is " + currentTempValue);
                    // set current temp
                    currentTemp.setText(currentTempValue + " °C");
                    // set current weather icon
                    int weatherIcon = getWeatherIconByCode(currentWeatherCode);
                    currentWeatherIcon.setImageResource(weatherIcon);
                    // set weather background
                    weatherBackground = getWeatherBackgroundByCode(currentWeatherCode);
                    if (!isIdle) {
                        mainLayout.setBackgroundResource(weatherBackground);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("myapp", "Something went wrong");
            }
        });
    }

    private int getWeatherIconByCode(int currentWeatherCode) {
        switch (currentWeatherCode) {
            case 1:
            case 2:
            case 3:
                return R.drawable.wi_day_cloudy;
            case 45:
            case 48:
                return R.drawable.wi_fog;
            case 51:
            case 53:
            case 55:
            case 81:
            case 82:
            case 83:
                return R.drawable.wi_day_showers;
            case 56:
            case 57:
                return R.drawable.wi_day_rain_mix;
            case 61:
            case 63:
            case 65:
                return R.drawable.wi_day_rain;
            case 66:
            case 67:
                return R.drawable.wi_day_sleet;
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
                return R.drawable.wi_day_snow;
            case 95:
            case 96:
            case 99:
                return R.drawable.wi_day_thunderstorm;
            case 0:
            default:
                return R.drawable.wi_day_sunny;
        }
    }

    private int getWeatherBackgroundByCode(int currentWeatherCode) {
        switch (currentWeatherCode) {
            case 1:
            case 2:
            case 3:
                return R.drawable.cloudy;
            case 45:
            case 48:
                return R.drawable.mist;
            case 51:
            case 53:
            case 55:
            case 81:
            case 82:
            case 83:
            case 56:
            case 57:
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
                return R.drawable.rain;
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
                return R.drawable.snow;
            case 95:
            case 96:
            case 99:
                return R.drawable.storm;
            case 0:
            default:
                return R.drawable.sun;
        }
    }

    @SuppressLint("SimpleDateFormat")
    @NonNull
    private String getFormattedDate() {
        return new SimpleDateFormat("EEEE, MMMM d.").format(Calendar.getInstance().getTime());
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        isIdle = false;
        gestureDetector.onTouchEvent(motionEvent);
        boolean webDashboardIsEnabled = sharedPref.getBoolean("websiteEnabler", false);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = motionEvent.getX();
                y1 = motionEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = motionEvent.getX();
                y2 = motionEvent.getY();

                float valueX = x2 - x1;

                float valueY = y2 - y1;

                Class<?> selectedActivity = MainActivity.class;

                if (Math.abs(valueX) > MIN_DISTANCE) {
                    if (x2 > x1) {
                        // right
                        selectedActivity = WidgetsActivity.class;
                    } else if (webDashboardIsEnabled) {
                        // left
                        selectedActivity = WebViewActivity.class;
                    }
                } else if (Math.abs(valueY) > MIN_DISTANCE) {
                    if (y2 > y1) {
                        // bottom
                        selectedActivity = SettingsActivity.class;
                    } else {
                        // top
                        selectedActivity = AppsDrawerActivity.class;
                    }
                }

                Intent i = new Intent(MainActivity.this, selectedActivity);
                startActivity(i);
        }

        return super.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onDown(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(@NonNull MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

}