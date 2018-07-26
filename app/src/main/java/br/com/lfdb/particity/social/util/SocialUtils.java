package br.com.lfdb.particity.social.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessTokenSource;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.google.android.gms.plus.PlusShare;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;

import br.com.lfdb.particity.social.SocialConstants;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class SocialUtils {

    public static void post(Activity context, String message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String logged = prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "");
        if (logged.equalsIgnoreCase("twitter")) {
            postToTwitter(context, message);
        } else if (logged.equalsIgnoreCase("google")) {
            postToGooglePlus(context, message);
        } else if (logged.equalsIgnoreCase("facebook")) {
            postToFacebook(context, message);
        }
    }

    private static void postToGooglePlus(Context context, String message) {
        Intent shareIntent = new PlusShare.Builder(context)
                .setType("text/plain")
                .setText(message)
                .getIntent();

        context.startActivity(shareIntent);
    }

    public static void logout(Activity context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String logged = prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "");
        if (logged.equalsIgnoreCase("twitter")) {
            logoutTwitter(context);
        } else if (logged.equalsIgnoreCase("google")) {
            logoutGooglePlus(context);
        } else if (logged.equalsIgnoreCase("facebook")) {
            logoutFacebook(context);
        }
    }

    private static void logoutFacebook(Activity context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(SocialConstants.PREF_FACEBOOK_ACCESS_TOKEN).remove(SocialConstants.PREF_FACEBOOK_EXPIRES_IN)
                .remove(SocialConstants.PREF_LOGGED_SOCIAL).apply();
    }

    private static void logoutGooglePlus(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(SocialConstants.PREF_LOGGED_SOCIAL).apply();
    }

    private static void logoutTwitter(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(SocialConstants.PREF_TWITTER_AUTH_TOKEN).remove(SocialConstants.PREF_TWITTER_AUTH_TOKEN_SECRET)
                .remove(SocialConstants.PREF_LOGGED_SOCIAL).apply();

    }

    private static void postToTwitter(final Activity context, final String message) {
        TwitterFactory twitterFactory = new TwitterFactory();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        AccessToken accessToken = new AccessToken(prefs.getString(SocialConstants.PREF_TWITTER_AUTH_TOKEN, ""),
                prefs.getString(SocialConstants.PREF_TWITTER_AUTH_TOKEN_SECRET, ""));
        final Twitter twitter = twitterFactory.getInstance();
        twitter.setOAuthConsumer(SocialConstants.TWITTER_CONSUMER_KEY, SocialConstants.TWITTER_CONSUMER_SECRET);
        twitter.setOAuthAccessToken(accessToken);
        new Thread() {
            @Override
            public void run() {
                try {
                    twitter.updateStatus(message);
                } catch (TwitterException e) {
                    Log.e("Social", e.getMessage(), e);
                    context.runOnUiThread(() -> Toast.makeText(context, "Falha ao publicar tweet", Toast.LENGTH_SHORT).show());
                }
            }
        }.start();
    }

    private static void postToFacebook(final Context context, final String mensagem) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        com.facebook.AccessToken token = com.facebook.AccessToken.createFromExistingAccessToken(prefs.getString(SocialConstants.PREF_FACEBOOK_ACCESS_TOKEN, ""),
                new Date(prefs.getLong(SocialConstants.PREF_FACEBOOK_EXPIRES_IN, 0)), null, AccessTokenSource.CLIENT_TOKEN,
                Arrays.asList("basic_info", "email", "publish_actions"));
        Session.openActiveSessionWithAccessToken(context, token, (session, state, exception) -> {
            if (session.isOpened()) {

                Bundle postParams = new Bundle();
                postParams.putString("message", mensagem);

                Request.Callback callback = response -> {
                    JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
                    String postId = null;
                    try {
                        postId = graphResponse.getString("id");
                    } catch (JSONException e) {
                        Log.e("Social Me", "JSON error " + e.getMessage());
                    }
                    FacebookRequestError error = response.getError();
                    if (error != null) {
                        Toast.makeText(context, error.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                };

                Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);

                RequestAsyncTask task = new RequestAsyncTask(request);
                task.execute();
            }
        });
    }
}
