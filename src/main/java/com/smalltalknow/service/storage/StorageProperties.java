package com.smalltalknow.service.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {
    private String locationWindows = "E://server/download/";
    private String locationLinux = "/server/download/";

    public String getLocationWindows() {
        return locationWindows;
    }

    public void setLocationWindows(String locationWindows) {
        this.locationWindows = locationWindows;
    }

    public String getLocationLinux() {
        return locationLinux;
    }

    public void setLocationLinux(String locationLinux) {
        this.locationLinux = locationLinux;
    }
}
