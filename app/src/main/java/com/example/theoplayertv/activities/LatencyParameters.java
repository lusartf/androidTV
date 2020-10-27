package com.example.theoplayertv.activities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class LatencyParameters {

    private static Gson gson = null;
    public void LatencyParameters(){

    }

    @SerializedName("currentlatency")
    private int currentLatency = 0;
    public int getCurrentLatency(){
        return this.currentLatency;
    }
    public void setCurrentLatency(int currentLatency) {
        this.currentLatency = currentLatency;
    }


    @SerializedName("localtime")
    private long localTime = 0;
    public long getLocaltime(){
        return this.localTime;
    }
    public void setLocaltime(long localtime) {
        this.localTime = localTime;
    }

    @SerializedName("programdatetime")
    private long programdatetime = 0;
    public long getProgramDateTime(){ return this.programdatetime; }
    public void setProgramDateTime(long programdatetime) {
        this.programdatetime = programdatetime;
    }


    @SerializedName("servertime")
    private long servertime = 0;
    public long getServerTime(){
        return this.servertime;
    }
    public void setServerTime(long servertime) {
        this.servertime = servertime;
    }

    public static String ToJson(LatencyManagerConfiguration mgr){
        if(gson == null)
            gson = new GsonBuilder().create();
        return gson.toJson(mgr);
    }

    public static LatencyParameters FromJson(String json){
        if(gson == null)
            gson = new GsonBuilder().create();

        LatencyParameters config = gson.fromJson(json, LatencyParameters.class);
        return config;
    }

}
