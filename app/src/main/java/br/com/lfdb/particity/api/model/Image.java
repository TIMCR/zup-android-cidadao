package br.com.lfdb.particity.api.model;

import java.io.Serializable;

public class Image implements Serializable {

    private String active;
    private String disabled;

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getDisabled() {
        return disabled;
    }

    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }
}
