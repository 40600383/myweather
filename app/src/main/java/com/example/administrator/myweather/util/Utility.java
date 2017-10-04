package com.example.administrator.myweather.util;

import android.text.TextUtils;

import com.example.administrator.myweather.db.City;
import com.example.administrator.myweather.db.County;
import com.example.administrator.myweather.db.Province;
import com.example.administrator.myweather.gson.Weather5;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/9/14 0014.
 */

public class Utility {
    //解析服务器返回的省级数据
    public static boolean handleProvinceRespone(String respone){
        if(!TextUtils.isEmpty(respone)){
            try {
                JSONArray allProvinces = new JSONArray(respone);
                for(int i= 0;i<allProvinces.length();i++){
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCityRespone(String respone,int provinceId){
        if(!TextUtils.isEmpty(respone)){
            try {
                JSONArray allCities = new JSONArray(respone);
                for(int i= 0;i<allCities.length();i++){
                    JSONObject cityObject=allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCountyRespone(String respone,int cityId){
        if(!TextUtils.isEmpty(respone)){
            try {
                JSONArray allCounties= new JSONArray(respone);
                for(int i= 0;i<allCounties.length();i++){
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    County county=  new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather5 handleWeather5Response(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather5.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
