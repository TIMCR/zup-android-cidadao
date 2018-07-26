package br.com.lfdb.particity.base;

import br.com.lfdb.particity.api.model.ImageResource;
import br.com.lfdb.particity.api.model.ReportCategory;

public class Defaults {

    private static String active = null;
    private static String inaactive = null;
    private static String marker = null;

    public static void setup(ReportCategory category, boolean isRetina) {
        String[] splitted;
        ImageResource image = isRetina ? category.getIcon().getRetina() : category.getIcon().getCommon();
        splitted = image.getMobile().getActive().split("/");
        active = splitted[splitted.length - 1];
        splitted = image.getMobile().getDisabled().split("/");
        inaactive = splitted[splitted.length - 1];

        splitted = isRetina ? category.getMarker().getRetina().getMobile().split("/") : category.getMarker().getCommon().getMobile().split("/");
        marker = splitted[splitted.length - 1];
    }

    public static String getDefaultInactiveIcon() {
        return inaactive;
    }

    public static String getDefaultActiveIcon() {
        return active;
    }

    public static String getDefaultMarker() {
        return marker;
    }
}
