package com.example.littleprince;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    ImageButton locationBtn, settingBtn;
    TextView locationText, weatherText;

    double latitude, longitude;

    public static final String PREFERENCES_NAME = "rebuild_preference";
    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2000;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String [] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //위치정보 동의
        if (!checkedLocationServicesStatus()){
            showDialogForLocationServiceSetting();
        }
        else{
            checkRunTimePermission();
        }

        SharedPreferences prefs = getPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("isSuccess", "SUCCESS");
        editor.apply();

        locationBtn = findViewById(R.id.main_imgbtn_location);
        settingBtn = findViewById(R.id.main_imgbtn_setting);
        locationText = findViewById(R.id.main_tv_location);
        weatherText = findViewById(R.id.main_tv_weather);


        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker = new GpsTracker(MainActivity.this);

                latitude = gpsTracker.getLatitude();
                longitude = gpsTracker.getLongitude();

                String address = getCurrentAddress(latitude, longitude);
                locationText.setText(address);


            }
        });



        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

    }

    private void getWeatherData(double lat, double lon){
        String baseUrl = "api.openweathermap.org/data/2.5/weather?lat=" +
                lat + "&lon=" +
                lon + "&appid=c0c0e0b75844021f3bc5b89546cab422";

        ReceiveWeatherTask receiveWeatherTask = new ReceiveWeatherTask();
        receiveWeatherTask.execute(baseUrl);

    }

    private class ReceiveWeatherTask extends AsyncTask<String, Void, JSONObject>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... datas) {
            try{
                HttpURLConnection conn = (HttpURLConnection) new URL(datas[0]).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStream is = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader in = new BufferedReader(reader);

                    String readed;
                    while ((readed = in.readLine()) != null){
                        JSONObject jObject = new JSONObject(readed);
                        return jObject;
                    }
                }

                else {
                    return null;
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            if (result != null){
                String iconName="";
                String nowTemp="";
                String maxTemp="";
                String minTemp="";

                String humidity="";
                String speed="";
                String main="";
                String description="";

                try {
                    iconName = result.getJSONArray("weather").getJSONObject(0).getString("icon");
                    nowTemp = result.getJSONObject("main").getString("temp");
                    maxTemp = result.getJSONObject("main").getString("temp_max");
                    minTemp = result.getJSONObject("main").getString("temp_min");
                    humidity = result.getJSONObject("main").getString("humidity");
                    speed = result.getJSONObject("wind").getString("speed");
                    main = result.getJSONArray("weather").getJSONObject(0).getString("main");
                    description = result.getJSONArray("weather").getJSONObject(0).getString("description");
                } catch (JSONException e){
                    e.printStackTrace();
                }
                description = transferWeather(description);
            }
        }
    }

    private String transferWeather(String weather){
        weather = weather.toLowerCase();
        if (weather.equals("haze")){
            return "안개";
        }
        else if(weather.equals("fog")){
            return "안개";
        }
        else if(weather.equals("clouds")){
            return "구름";
        }
        else if(weather.equals("few clouds")){
            return "구름 조금";
        }
        else if(weather.equals("scattered clouds")){
            return "흐림";
        }
        else if(weather.equals("broken clouds")){
            return "구름 많음";
        }
        else if(weather.equals("overcast clouds")){
            return "구름 많음";
        }
        else if(weather.equals("clear sky")){
            return "맑음";
        }

        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length){
            boolean check_result = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED){
                    check_result = false;
                    break;
                }
            }

            if (check_result){
                ;
            }
            else{
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){
                    Toast.makeText(MainActivity.this, "위치 정보 활성화에 동의해 주세요.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "위치 정보 활성화에 동의해 주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    void checkRunTimePermission(){
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED){

        }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])){
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    public String getCurrentAddress(double latitude, double longitude){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude,longitude, 7);
        } catch (IOException e) {
            Toast.makeText( this, "GeoCoder 사용불가", Toast.LENGTH_SHORT).show();
            return "GeoCoder 사용불가";
        } catch (IllegalArgumentException illegalArgumentException){
            Toast.makeText(this, "잘못된 좌표", Toast.LENGTH_SHORT).show();
            return "잘못된 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_SHORT).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스 동의가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkedLocationServicesStatus()){
                    if (checkedLocationServicesStatus()){
                        Log.d("@@@", "onActivityResult : Gps 활성화 되었음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkedLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
