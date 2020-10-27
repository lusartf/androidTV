package com.example.theoplayertv.activities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class LatencyManagerConfiguration {

    private static Gson gson = null;
    public void LatencyManagerConfiguration(){

    }
    @SerializedName("targetlatency")
    private int targetLatency = 3000;
    public int getTargetLatency(){
        return this.targetLatency;
    }
    public void setTargetLatency(int targetLatency) {
        this.targetLatency = targetLatency;
    }

    @SerializedName("seekwindow")
    private int seekWindow = 3000;
    public int getSeekWindow(){
        return this.seekWindow;
    }
    public void setSeekWindow(int seekWindow) {
        this.seekWindow = seekWindow;
    }

    @SerializedName("latencywindow")
    private int latencyWindow = 250;
    public int getLatencyWindow() {
        return latencyWindow;
    }
    public void setLatencyWindow(int latencyWindow) {
        this.latencyWindow = latencyWindow;
    }

    @SerializedName("interval")
    private int interval = 40;
    public int getInterval() {
        return interval;
    }
    public void setInterval(int interval) {
        this.interval = interval;
    }

    @SerializedName("fireupdate")
    private boolean fireupdate = false;
    public boolean isFireupdate() {
        return fireupdate;
    }

    public void setFireupdate(boolean fireupdate) {
        this.fireupdate = fireupdate;
    }

    @SerializedName("ratechange")
    private double rateChange = 0.8;
    public double getRateChange() {
        return rateChange;
    }

    public void setRateChange(double rateChange) {
        this.rateChange = rateChange;
    }

    @SerializedName("sync")
    private boolean sync = true;
    public boolean getSync() {
        return sync;
    }

    public void setSync(boolean doSync) {
        this.sync = doSync;
    }


    @SerializedName("disableonpause")
    private boolean disableOnPause = true;
    public boolean getdisableOnPause() {
        return this.disableOnPause;
    }
    public void setdisableOnPause(boolean val) {
        this.disableOnPause = val;
    }

    @SerializedName("enableoverlay")
    private boolean enableoverlay = true;
    public boolean getEnableOverlay() {
        return this.enableoverlay;
    }
    public void setEnableOverlay(boolean val) {
        this.sync = val;
    }


    @SerializedName("timeserver")
    private String timeServer = "https://time.theoplayer.com";
    public String getTimeServer(){
        return this.timeServer;
    }
    public void setTimeServer(String value) {
        this.timeServer = value;
    }


    @SerializedName("useencodertime")
    private boolean useencodertime = true;
    public boolean getUseEncoderTime() {
        return this.useencodertime;
    }
    public void setUseEncoderTime(boolean val) {
        this.useencodertime = val;
    }

    @SerializedName("encoderlatency")
    private int encoderlatency = 40;
    public int getEncoderLatency() {
        return encoderlatency;
    }
    public void setEncoderLatency(int value) {
        this.encoderlatency = value;
    }

    public static String ToJson(LatencyManagerConfiguration mgr){
        if(gson == null)
            gson = new GsonBuilder().create();
        return gson.toJson(mgr);
    }

    public static LatencyManagerConfiguration FromJson(String json){
        if(gson == null)
            gson = new GsonBuilder().create();

        LatencyManagerConfiguration config = gson.fromJson(json, LatencyManagerConfiguration.class);
        return config;
    }

}
