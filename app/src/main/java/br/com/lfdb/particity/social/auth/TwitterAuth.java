package br.com.lfdb.particity.social.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.List;

import br.com.lfdb.particity.R;
import br.com.lfdb.particity.social.SocialConstants;
import br.com.lfdb.particity.social.extra.TwitterDialog;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterAuth extends Activity implements TwitterDialog.TwitterDialogListener {

    private Twitter twitter;
    private ProgressDialog progressDialog;
    private RequestToken requestToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasAccessToken()) {
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            progressDialog = new ProgressDialog(this);
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            progressDialog.setMessage(getString(R.string.inicializando));
            progressDialog.show();

            new Thread() {
                @Override
                public void run() {

                    try {
                        twitter = TwitterFactory.getSingleton();
                        twitter.setOAuthConsumer(SocialConstants.TWITTER_CONSUMER_KEY, SocialConstants.TWITTER_CONSUMER_SECRET);
                        requestToken = twitter.getOAuthRequestToken();
                        runOnUiThread(() -> new TwitterDialog(TwitterAuth.this, requestToken.getAuthorizationURL(), TwitterAuth.this).show());
                    } catch (TwitterException e) {
                        Log.e("Social", e.getMessage(), e);
                        setResult(Activity.RESULT_CANCELED);
                        TwitterAuth.this.finish();
                    }
                }
            }.start();
        }
    }

    private void saveAccessToken(AccessToken accessToken) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(SocialConstants.PREF_TWITTER_AUTH_TOKEN, accessToken.getToken())
                .putString(SocialConstants.PREF_TWITTER_AUTH_TOKEN_SECRET, accessToken.getTokenSecret())
                .putString(SocialConstants.PREF_LOGGED_SOCIAL, "twitter").apply();
    }

    private boolean hasAccessToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.contains(SocialConstants.PREF_TWITTER_AUTH_TOKEN) && prefs.contains(SocialConstants.PREF_TWITTER_AUTH_TOKEN_SECRET)
                && prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "").equalsIgnoreCase("twitter");

    }

    @Override
    public void onComplete(String value) {
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(new URI(value), "UTF-8");
            for (NameValuePair param : params) {
                if (param.getName().equalsIgnoreCase("oauth_verifier")) {
                    final String oauthVerifier = param.getValue();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                saveAccessToken(twitter.getOAuthAccessToken(requestToken, oauthVerifier));
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                });
                            } catch (TwitterException e) {
                                Log.e("Social", e.getMessage(), e);
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    setResult(Activity.RESULT_CANCELED);
                                    finish();
                                });
                            }

                        }
                    }.start();
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("Social", e.getMessage(), e);
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onError(String value) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
