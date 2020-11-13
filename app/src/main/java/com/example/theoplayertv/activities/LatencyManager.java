package com.example.theoplayertv.activities;

import android.util.Log;
import android.webkit.ValueCallback;
/*
import com.theoplayer.android.api.THEOplayerView;
import com.theoplayer.android.api.message.MessageListener;

 */

public class LatencyManager {

    //THEOplayerView tpv;
    private LatencyParameters _latency = null;
    /*
    public LatencyManager(THEOplayerView view) {
        tpv = view;
        configuration = new LatencyManagerConfiguration();
        this.initializeJavascript();
        registerMessageListeners();
    }

    public void Sync() {
        tpv.evaluateJavaScript("THEOplayer.timeServer.sync();", null);
    }

    private void registerMessageListeners() {
        tpv.addJavaScriptMessageListener("THEOplayer.latencyManager.update", new MessageListener() {
            @Override
            public void handleMessage(String message) {
                // Log.i("latency: ", message);
                _latency = LatencyParameters.FromJson(message);

            }
        });
    }

    public LatencyManager(THEOplayerView view, LatencyManagerConfiguration config) {
        tpv = view;
        configuration = config;
        config.setFireupdate(true);
        this.initializeJavascript();
        registerMessageListeners();
    }


    public LatencyParameters getLatency() {
        //callGetLatencyinJs();
        return _latency;
    }

    private LatencyManagerConfiguration configuration;

    public void setConfiguration(LatencyManagerConfiguration cfg) {
        this.configuration = cfg;
        this.Configure();
    }

    public int getTargetLatency() {
        return this.configuration.getTargetLatency();

    }

    public void setTargetLatency(int targetLatency) {
        this.configuration.setTargetLatency(targetLatency);
        this.Configure();
    }

    public int getSeekWindow() {
        return this.configuration.getSeekWindow();
    }

    public void setSeekWindow(int seekWindow) {
        this.configuration.setSeekWindow(seekWindow);
        this.Configure();
    }

    public int getLatencyWindow() {
        return this.configuration.getLatencyWindow();
    }

    public void setLatencyWindow(int latencyWindow) {
        this.configuration.setLatencyWindow(latencyWindow);
        this.Configure();
    }

    public int getInterval() {
        return this.configuration.getInterval();
    }

    public void setInterval(int interval) {
        this.configuration.setInterval(interval);
        this.Configure();
    }

    public boolean isFireupdate() {
        return this.configuration.isFireupdate();
    }

    public void setFireupdate(boolean fireupdate) {
        this.configuration.setFireupdate(fireupdate);
        this.Configure();
    }

    public double getRateChange() {
        return this.configuration.getRateChange();
    }

    public void setRateChange(double rateChange) {
        this.configuration.setRateChange(rateChange);
        this.Configure();
    }


    public void Show() {
        callJsMethodOnLatencyManager("show();");
    }

    public void Hide() {
        callJsMethodOnLatencyManager("hide();");
    }

    private void Configure() {
        String cfg = LatencyManagerConfiguration.ToJson(this.configuration);
        callJsMethodOnLatencyManager("configure(" + cfg + ")");
    }

    public void Pause() {
        String cfg = LatencyManagerConfiguration.ToJson(this.configuration);
        callJsMethodOnLatencyManager("pause(true)");
    }

    public void Resume() {
        String cfg = LatencyManagerConfiguration.ToJson(this.configuration);
        callJsMethodOnLatencyManager("pause(false)");
    }

    private void initializeJavascript() {
        String cfg = LatencyManagerConfiguration.ToJson(this.configuration);
        String js = "THEOplayer.initializeLatencyManager(THEOplayer.players[0], " + cfg + ");";

        tpv.evaluateJavaScript(js, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                String result = s;
            }
        });
        Configure();
    }

    private void callJsMethodOnLatencyManager(String method) {
        tpv.evaluateJavaScript("THEOplayer.latencyManager." + method + ";", null);
    }

    private void callGetLatencyinJs() {
        tpv.evaluateJavaScript("THEOplayer.latencyManager.getLatency()", result -> {
            Log.i("LATENCY", result);
        });
    }
    */
}
