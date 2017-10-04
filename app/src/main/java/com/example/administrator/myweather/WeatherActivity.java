package com.example.administrator.myweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.myweather.gson.Forecast;
import com.example.administrator.myweather.gson.Weather5;
import com.example.administrator.myweather.service.AutoUpdateService;
import com.example.administrator.myweather.util.HttpUtil;
import com.example.administrator.myweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    public DrawerLayout drawerLayout;
    private Button navigateButton;

    private ImageView bingPicImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        weatherLayout= (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime= (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        navigateButton= (Button) findViewById(R.id.navigate_button);

        bingPicImg= (ImageView) findViewById(R.id.bing_pic_img);

        navigateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);

        swipeRefresh= (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather",null);

        String bingPicUrl=preferences.getString("bing_pic_url",null);
        Log.d("TAG---", "onCreate: bingPicUrl"+bingPicUrl);
        if(bingPicUrl!=null)
            Glide.with(this).load(bingPicUrl).into(bingPicImg);
        else
            loadBingPic();
        if(weatherString!=null){
            Weather5 weather5= Utility.handleWeather5Response(weatherString);
            //mWeatherId=weather5.basic.weatherId;
            showWeatherInfo(weather5);
        }else {
           mWeatherId= getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String weatherString = preferences.getString("weather",null);
                if(weatherString!=null) {
                    Weather5 weather5= Utility.handleWeather5Response(weatherString);
                    mWeatherId =weather5.basic.weatherId;
                }
                requestWeather(mWeatherId);

            }
        });
    }
    public void requestWeather(final String weatherId){
        String weatherUrl= "https://free-api.heweather.com/v5/weather?city="+weatherId
                +"&key=80821b8b619043d08396349e7fb968c6";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败！",
                                Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText= response.body().string();
                final Weather5 weather5= Utility.handleWeather5Response(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather5!=null&&"ok".equals(weather5.status)){
                            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather5);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                    Toast.LENGTH_LONG).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }
    private void loadBingPic(){
        Log.d("TAG---", "loadBingPic: ");
        //String requestBingPic = "http://guolin.tech/api/bing_pic";
//        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
                //final String bingPicUrl= response.body().string();
                final String bingPicUrl="http://cn.bing.com/az/hprichbg/rb/Mooncake_ZH-CN10274798301_1920x1080.jpg";
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(
                        WeatherActivity.this).edit();
                editor.putString("bing_pic_url",bingPicUrl);
                editor.apply();
                Log.d("TAG---", "onResponse: ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TAG---", "run: ");
                        Glide.with(WeatherActivity.this).load(bingPicUrl).into(bingPicImg);
                    }
                });
            }
       // });
    //}
    private void showWeatherInfo(Weather5 weather5){
        String cityName= weather5.basic.cityName;
        String updateTime= weather5.basic.update.updateTime.split(" ")[1];
        String degree= weather5.now.temperature+"℃";
        String weatherInfo= weather5.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather5.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout,false);
            TextView dateText= (TextView) view.findViewById(R.id.date_text);
            TextView infoText= (TextView) view.findViewById(R.id.info_text);
            TextView maxText= (TextView) view.findViewById(R.id.max_text);
            TextView minText= (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather5.aqi!=null){
            aqiText.setText(weather5.aqi.city.aqi);
            pm25Text.setText(weather5.aqi.city.pm25);
        }
        String comfort ="舒适度："+weather5.suggestion.comfort.info;
        String carWash="洗车指数："+weather5.suggestion.carWash.info;
        String sport="运动建议："+weather5.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent= new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
//                (WeatherActivity.this).edit();
//        editor.putString("bing_pic_url",null);
//        editor.apply();
        Log.d("TAG---", "onDestroy: ");
    }
}
