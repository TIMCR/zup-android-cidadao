package br.com.lfdb.particity.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Icon implements Serializable {

    private ImageResource retina;
    @SerializedName("default")
    private ImageResource common;

    public ImageResource getRetina() {
        return retina;
    }

    public void setRetina(ImageResource retina) {
        this.retina = retina;
    }

    public ImageResource getCommon() {
        return common;
    }

    public void setCommon(ImageResource common) {
        this.common = common;
    }
}
