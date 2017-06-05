package com.smartgateway.app.data.model;


import android.support.annotation.Nullable;

import java.util.List;

public class Credentials {

    private List<Provider> providers;
    private String message;

    public String getMessage() {
        return message;
    }

    @Nullable
    public Provider getProvider(String providerName) {
        if (providers == null || providers.size() == 0) {
            return null;
        }

        for (Provider provider : providers) {
            if (provider != null && providerName.equals(provider.getProvider())) {
                return provider;
            }
        }
        return null;
    }

    public int getProviderCount() {
        if (providers == null) {
            return 0;
        }
        return providers.size();
    }
}
