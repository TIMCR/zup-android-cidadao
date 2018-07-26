package br.com.lfdb.particity.domain;

import org.joda.time.DateTime;

public enum Periodo {
    ULTIMOS_6_MESES, ULTIMOS_3_MESES, ULTIMO_MES, ULTIMA_SEMANA;

    public String getDateString() {
        DateTime dateTime = DateTime.now();

        switch (this) {
            case ULTIMA_SEMANA:
                dateTime = dateTime.minusWeeks(1);
                break;
            case ULTIMO_MES:
                dateTime = dateTime.minusMonths(1);
                break;
            case ULTIMOS_3_MESES:
                dateTime = dateTime.minusMonths(3);
                break;
            case ULTIMOS_6_MESES:
                dateTime = dateTime.minusMonths(6);
                break;
        }

        return dateTime.toString();
    }
}
