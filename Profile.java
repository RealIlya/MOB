package com.example.improvement;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class Profile {

    private String name, city, house, floor, email, deviceId;
    private int suggestionsCount;
    private Bitmap img;
    private boolean flag = false;

    public Profile(String getUrl, String req, SharedPreferences sharedPrefs){
        ProfileInterface profileInterface = new Retrofit.Builder().baseUrl(getUrl).build().create(ProfileInterface.class);
        this.deviceId = sharedPrefs.getString("PREF_UNIQUE_ID", null); //context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE)
        if (this.deviceId == null) {
            this.deviceId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("PREF_UNIQUE_ID", this.deviceId);
            editor.commit();
        }
        profileInterface.getProfile(req, this.deviceId).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    JsonObject jsonObject = new JsonParser().parse(response.body().toString()).getAsJsonObject();
                    name = jsonObject.get("name").getAsString();
                    city = jsonObject.get("city").getAsString();
                    house = jsonObject.get("house").getAsString();
                    floor = jsonObject.get("floor").getAsString();
                    email = jsonObject.get("email").getAsString();
                    suggestionsCount = jsonObject.get("suggestionsCount").getAsInt();
                    String[] byteValues = jsonObject.get("img").getAsString().substring(1, jsonObject.get("img").getAsString().length() - 1).split(",");
                    byte[] bytes = new byte[byteValues.length];
                    for (int i = 0, len = bytes.length; i < len; i++) {
                        bytes[i] = Byte.parseByte(byteValues[i].trim());
                    }
                    img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    flag = true;
                } else{
                    //Mistake
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                //No Internet
            }
        });
    }

    public void saveProfile(String getUrl, String req, String name, String city, String house, String floor, String email, Bitmap img){
        ProfileInterface profileInterface = new Retrofit.Builder().baseUrl(getUrl).build().create(ProfileInterface.class);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, 100, blob);
        this.name = name;
        this.city = city;
        this.email = email;
        this.floor = floor;
        this.house = house;
        this.img = img;
        String arr = Arrays.toString(blob.toByteArray()); // It is json bytes
        profileInterface.saveProfile(req, this.deviceId, name, city, house, floor, email, arr).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Log.d("RetrofitTag", response.toString());
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                //No Internet
            }
        });
    }

    public String getName(){return name;}
    public String getCity(){return city;}
    public String getHouse(){return house;}
    public String getFloor(){return floor;}
    public Bitmap getImg(){return img;}
    public int getSuggestionsCount(){return suggestionsCount;}
    public String getEmail(){return email;}
    public boolean getFlag(){return flag;}//Getters

    public interface ProfileInterface{
        @GET("{url}")
        Call<Object> getProfile(@Path("url") String url, @Query("deviceId") String id);

        @POST("{url}")
        Call<Object> saveProfile(@Path("url") String url, @Query("deviceId") String id, @Query("name") String name, @Query("city") String city, @Query("house") String house, @Query("floor") String floor, @Query("email") String email, @Query("img") String img);//It is json
    }
}
