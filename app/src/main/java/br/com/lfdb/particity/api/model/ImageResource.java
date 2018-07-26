package br.com.lfdb.particity.api.model;

import java.io.Serializable;

public class ImageResource implements Serializable {

    private Image web;
    private Image mobile;

    public Image getWeb() {
        return web;
    }

    public void setWeb(Image web) {
        this.web = web;
    }

    public Image getMobile() {
        return mobile;
    }

    public void setMobile(Image mobile) {
        this.mobile = mobile;
    }
}
