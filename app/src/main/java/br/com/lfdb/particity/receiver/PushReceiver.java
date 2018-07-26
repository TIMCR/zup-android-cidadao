package br.com.lfdb.particity.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import br.com.lfdb.particity.R;
import br.com.lfdb.particity.SplashActivity;
import br.com.lfdb.particity.service.LoginService;

public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (String.valueOf(new LoginService().getUserId(context)).equals(intent.getExtras().getString("user_id"))) {
            Intent i = new Intent(context, SplashActivity.class);
            i.putExtra("report_id", Long.valueOf(intent.getStringExtra("object_id")));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, Long.valueOf(System.currentTimeMillis()).intValue(), i, 0);

            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_stat_zup)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(intent.getStringExtra("message"))
                    .setTicker(intent.getStringExtra("message"))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, notification);
        }
    }
}
