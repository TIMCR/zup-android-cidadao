package br.com.lfdb.particity.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Marker implements Serializable {

    private MarkerResource retina;
    @SerializedName("default")
    private MarkerResource common;

    public MarkerResource getRetina() {
        return retina;
    }

    public void setRetina(MarkerResource retina) {
        this.retina = retina;
    }

    public MarkerResource getCommon() {
        return common;
    }

    public void setCommon(MarkerResource common) {
        this.common = common;
    }
}
