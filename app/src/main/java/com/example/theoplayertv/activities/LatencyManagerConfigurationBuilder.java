package com.example.theoplayertv.activities;

public class LatencyManagerConfigurationBuilder {

    private LatencyManagerConfiguration configuration;

    public LatencyManagerConfigurationBuilder(){
        configuration = new LatencyManagerConfiguration();
    }

    public LatencyManagerConfigurationBuilder targetLatency(int targetLatency){
        this.configuration.setTargetLatency(targetLatency);
        return this;
    }

    public LatencyManagerConfigurationBuilder seekWindow(int seekWindow){
        this.configuration.setSeekWindow(seekWindow);
        return this;
    }

    public LatencyManagerConfigurationBuilder latencyWindow(int latencyWindow){
        this.configuration.setLatencyWindow (latencyWindow);
        return this;
    }

    public LatencyManagerConfigurationBuilder interval(int interval){
        this.configuration.setInterval(interval);
        return this;
    }

    public LatencyManagerConfigurationBuilder fireUpdate(boolean fireupdate){
        this.configuration.setFireupdate(fireupdate);
        return this;
    }

    public LatencyManagerConfigurationBuilder rateChange(double rateChange){
        this.configuration.setRateChange(rateChange);
        return this;
    }

    public LatencyManagerConfigurationBuilder sync(boolean sync){
        this.configuration.setSync(sync);
        return this;
    }

    public LatencyManagerConfigurationBuilder timeServer(String timeserver){
        this.configuration.setTimeServer(timeserver);
        return this;
    }

    public LatencyManagerConfiguration build(){
        return this.configuration;
    }
}
