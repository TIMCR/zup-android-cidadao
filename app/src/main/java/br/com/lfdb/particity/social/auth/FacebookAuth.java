package br.com.lfdb.particity.social.auth;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.Settings;

import br.com.lfdb.particity.social.SocialConstants;

public class FacebookAuth extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasAccessToken()) {
            setResult(Activity.RESULT_OK);
            finish();
            return;
        }

        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        Session session = Session.getActiveSession();
        if (session == null) {
            session = new Session(this);
            Session.setActiveSession(session);
        }

        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(callback));
        } else {
            Session.openActiveSession(this, true, callback);
        }
    }

    private Session.StatusCallback callback = (session, state, exception) -> {
        if (session.isOpened()) {
            if (!session.getPermissions().contains("publish_actions")) {
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(FacebookAuth.this, "publish_actions"));
                return;
            }

            Request request = Request.newMeRequest(session, (user, response) -> {
                if (user != null) {
                    Session session1 = Session.getActiveSession();
                    saveAccessToken(session1.getAccessToken(), session1.getExpirationDate().getTime());
                    setResult(Activity.RESULT_OK);
                }

                if (response.getError() != null) {
                    Toast.makeText(FacebookAuth.this, response.getError().getErrorMessage(), Toast.LENGTH_LONG).show();
                    setResult(Activity.RESULT_CANCELED);
                }

                finish();
            });
            request.executeAsync();
        } else if (exception != null) {
            Toast.makeText(FacebookAuth.this, "Falha ao conectar-se ao Facebook: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    private boolean hasAccessToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.contains(SocialConstants.PREF_FACEBOOK_ACCESS_TOKEN) && prefs.contains(SocialConstants.PREF_FACEBOOK_EXPIRES_IN)
                && prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "").equalsIgnoreCase("facebook");

    }

    private void saveAccessToken(String accessToken, long expiresIn) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(SocialConstants.PREF_FACEBOOK_ACCESS_TOKEN, accessToken)
                .putLong(SocialConstants.PREF_FACEBOOK_EXPIRES_IN, expiresIn)
                .putString(SocialConstants.PREF_LOGGED_SOCIAL, "facebook").apply();
    }
}





