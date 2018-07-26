package br.com.lfdb.particity.api.model;

import java.io.Serializable;

public class InventoryCategory implements Serializable {

    private final Marker marker;
    private final Marker pin;
    private final Icon icon;

    public InventoryCategory(Marker marker, Marker pin, Icon icon) {
        this.marker = marker;
        this.pin = pin;
        this.icon = icon;
    }

    public Marker getMarker() {
        return marker;
    }

    public Marker getPin() {
        return pin;
    }

    public Icon getIcon() {
        return icon;
    }
}
