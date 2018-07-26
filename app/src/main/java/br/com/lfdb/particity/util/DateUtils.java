package br.com.lfdb.particity.util;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String getIsoString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.US).format(date);
    }

    public static Date parseRFC3339Date(String datestring) throws Exception {
        Date d;

        if (datestring.endsWith("Z")) {
            try {
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                d = s.parse(datestring);
            } catch (java.text.ParseException pe) {
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
                s.setLenient(true);
                d = s.parse(datestring);
            }
            return d;
        }

        String firstpart = datestring.substring(0, datestring.lastIndexOf('-'));
        String secondpart = datestring.substring(datestring.lastIndexOf('-'));

        secondpart = secondpart.substring(0, secondpart.indexOf(':')) + secondpart.substring(secondpart.indexOf(':') + 1);
        datestring = firstpart + secondpart;
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        try {
            d = s.parse(datestring);
        } catch (java.text.ParseException pe) {
            s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.US);
            s.setLenient(true);
            d = s.parse(datestring);
        }
        return d;
    }

    public static String getIntervaloTempo(Date data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        Calendar hoje = Calendar.getInstance();

        int contador = 0;
        while (calendar.get(Calendar.DAY_OF_YEAR) < hoje.get(Calendar.DAY_OF_YEAR)) {
            contador++;
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (contador == 0) return "hoje";
        if (contador == 1) return "ontem";

        return "há " + contador + " dias atrás";
    }

    public static String getDefault(Date data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        if (day.length() < 2) { day = "0" + day; }
        if (month.length() < 2) { month = "0" + month; }
        return day + "/" + month + "/" + year;
    }

    public static String getString(Long tempoEmSegundos) {
        long var = tempoEmSegundos != null ? tempoEmSegundos : 0;
        if (var < 60) return String.format("%d Segundos", var);

        var /= 60;
        if (var < 60) return String.format("%d Minutos", var);

        var /= 60;
        if (var < 60) return String.format("%d Horas", var);

        var /= 24;
        return String.format("%d Dias", var);
    }

    public static String formatDate(Context context, Date date) {
        return DateFormat.getDateFormat(context).format(date) + " - " + DateFormat.getTimeFormat(context).format(date);
    }
}
