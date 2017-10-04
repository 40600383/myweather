package com.example.administrator.myweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;

import com.example.administrator.myweather.gson.Weather5;
import com.example.administrator.myweather.util.HttpUtil;
import com.example.administrator.myweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        int eightHour= 8*60*60*1000;
        long triggerAtTime= SystemClock.elapsedRealtime()+eightHour;
        Intent i= new Intent(this,AutoUpdateService.class);
        PendingIntent pendingIntent= PendingIntent.getService(this,0,i,0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }
    private void updateWeather(){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString= preferences.getString("weather",null);
        if(weatherString!=null){
            final Weather5 weather5= Utility.handleWeather5Response(weatherString);
            final String weatherId= weather5.basic.weatherId;

            String weatherUrl= "https://free-api.heweather.com/v5/weather?city="+weatherId
                    +"&key=80821b8b619043d08396349e7fb968c6";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText= response.body().string();
                    Weather5 weather5Too=Utility.handleWeather5Response(responseText);
                    if(weather5Too!=null&&"ok".equals(weather5Too.status)){
                        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(
                                AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
                }
            });
        }
    }
    private void updateBingPic(){
        String requestBingPic= "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic= response.body().string();
                SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(
                        AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }
}
