package com.zhq.taskforge.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pmhub")
public class PmhubConfig {

    private static boolean addressEnabled;

    public static boolean isAddressEnabled() {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        PmhubConfig.addressEnabled = addressEnabled;
    }
}
