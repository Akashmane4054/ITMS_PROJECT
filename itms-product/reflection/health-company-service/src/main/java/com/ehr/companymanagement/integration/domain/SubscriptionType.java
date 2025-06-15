package com.ehr.companymanagement.integration.domain;

import com.ehr.core.util.Constants;

public enum SubscriptionType {

    WHITELABEL(0L, "Whitelabel"), COBRANDING(1L, "CoBranding"), NOBRANDING(2L, "NoBranding");

    Long id;
    String name;

    private SubscriptionType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static String getTypeName(Long id) {
        switch (String.valueOf(id)) {
            case "0":
                return WHITELABEL.getName();
            case "1":
                return COBRANDING.getName();
            case "2":
                return NOBRANDING.getName();
            default:
                break;
        }
        return Constants.NA;
    }

    public Long getId() {
        return id;
    }


    public String getName() {
        return name;
    }

}
