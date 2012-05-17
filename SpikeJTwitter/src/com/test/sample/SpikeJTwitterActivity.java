package com.test.sample;

import java.util.List;

import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Message;
import winterwell.jtwitter.Twitter.Status;
import winterwell.jtwitter.Twitter.User;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SpikeJTwitterActivity extends Activity {
	private static final int MAX_TWEET_SIZE = 140;
	
	private OAuthSignpostClient oauthClient;
	Twitter twitter;
	private static final String USERNAME = "rsais";
	private static final String KEY = "UoICtFYnv64oM10jBRtHtw";
	private static final String SECRET = "N76MQOBPyLYyJhVCpjAyGuUJpJBrV1Jd8QbHY0brbQ";
	
	private SharedPreferences preferences;
	public static final String TWITTER_PREFS = "twitter_prefs";
	public static final String TWITTER_PREF_AUTH_KEY = "auth_key";
	public static final String TWITTER_PREF_AUTH_SECRET = "auth_secret";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        oauthClient = new OAuthSignpostClient(KEY, SECRET, "oob");
        loadPreferences();
    }

    public void loadPreferences() {
    	preferences = getSharedPreferences(TWITTER_PREFS, MODE_PRIVATE);
    	String authKey = preferences.getString(TWITTER_PREF_AUTH_KEY, "");
    	String authSecret = preferences.getString(TWITTER_PREF_AUTH_SECRET, "");
    	
    	if(authKey.equals("") == false && authSecret.equals("") == false) {
    		((TextView)findViewById(R.id.tv_ma_auth_key_value)).setText(R.string.ma_stored);
    		((TextView)findViewById(R.id.tv_ma_auth_secret_value)).setText(R.string.ma_stored);
    		
    		oauthClient = new OAuthSignpostClient(KEY, SECRET, authKey, authSecret);
    		twitter = new Twitter(USERNAME, oauthClient);
    	}
    }
    
    public void updatePreferences(String authKey, String authSecret) {
    	Editor pEditor = preferences.edit();
    	
    	if(authKey != null && authKey.equals("") == false 
    			&& authSecret != null && authSecret.equals("") == false) {
    		pEditor.putString(TWITTER_PREF_AUTH_KEY, authKey);
    		pEditor.putString(TWITTER_PREF_AUTH_SECRET, authSecret);
    		pEditor.commit();
    		((TextView)findViewById(R.id.tv_ma_auth_key_value)).setText(R.string.ma_stored);
    		((TextView)findViewById(R.id.tv_ma_auth_secret_value)).setText(R.string.ma_stored);
    	}
    	
    }
    
    public void onClick(View v) {
    	switch (v.getId()) {
			case R.id.bt_ma_fetch_pin:
				String requestToken = oauthClient.authorizeUrl().toString();
		        fetchPin(requestToken);
				break;
			
			case R.id.bt_ma_authenticate:
				try {
					String pin = ((EditText)findViewById(R.id.et_ma_pin)).getText().toString();
					oauthClient.setAuthorizationCode(pin);
					String[] accessToken = oauthClient.getAccessToken();
					Log.d("SAMPLE", "OAUTH_KEY: " + accessToken[0]);
					Log.d("SAMPLE", "OAUTH_SECRET: " + accessToken[1]);
					
					updatePreferences(accessToken[0], accessToken[1]);
					
				} catch (Exception e) {
					Log.e("SAMPLE", "Invalid PIN!", e);
					Toast.makeText(SpikeJTwitterActivity.this, "Invalid PIN!", 
							Toast.LENGTH_SHORT).show();
				}
				break;
				
			case R.id.bt_ma_post_twitt:
				String text = ((EditText)findViewById(R.id.et_ma_twitt_text)).getText().toString();
				sendTwitt(text);
			
			case R.id.bt_ma_get_twitts:
				getTweets();
				break;
				
			case R.id.bt_ma_getMessages:
				getMessages();
				break;
				
			default: 
				break;
		}
    }
    
    public void fetchPin(String urlauth){
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlauth));
    	startActivity(myIntent);
    }
    
    public void sendTwitt(String text) {
    	if(text == null || text.length() == 0 || text.length() > MAX_TWEET_SIZE) {
    		Toast.makeText(SpikeJTwitterActivity.this, "Text must be between 1 and 140 characters!" ,
    				Toast.LENGTH_LONG).show();
    	} else {
    		try {
    			twitter.setStatus(text);
    			Toast.makeText(SpikeJTwitterActivity.this, R.string.ma_send_succeeded, 
    					Toast.LENGTH_LONG).show();
    		} catch (Exception e) {
    			Log.e("SAMPLE", "Unable to send tweet", e);
    			Toast.makeText(SpikeJTwitterActivity.this, R.string.ma_send_failed, 
    					Toast.LENGTH_LONG).show(); 
			}
    	}
    }
    
    public void getTweets() {
		try {
    		List<Status> tweets = twitter.getHomeTimeline();
    		if(tweets.isEmpty() == false) {
    			Toast.makeText(SpikeJTwitterActivity.this, "Watch Logcat for results!", 
    					Toast.LENGTH_LONG).show();
    			Log.i("SAMPLE", "Fetched " + tweets.size() + " tweet(s)!");
    			
    			for(Status s : tweets) {
    				User u = s.getUser();
    				
    				Log.d("SAMPLE", u.getName() + " @" + u.getScreenName() + " >>"
    						+ s.getText() + "<<");
    			}
    			
    		} else {
    			Log.e("SAMPLE", "No tweets were fetched");
    			Toast.makeText(SpikeJTwitterActivity.this, R.string.ma_fetch_empty, 
    					Toast.LENGTH_LONG).show(); 
    		}
	    } catch (Exception e) {
			Log.e("SAMPLE", "Unable to fetch tweets", e);
			Toast.makeText(SpikeJTwitterActivity.this, R.string.ma_fetch_failed, 
					Toast.LENGTH_LONG).show(); 
		}
    }
    
    public void getMessages() { 
    	try {
    		List<Message> messages = twitter.getDirectMessages();
    		if(messages.isEmpty() == false) {
    			Toast.makeText(SpikeJTwitterActivity.this, "Watch Logcat for results!", 
    					Toast.LENGTH_LONG).show();
    			Log.i("SAMPLE", "Fetched " + messages.size() + " message(s)!");
    			
    			for(Message m : messages) {
    				User u = m.getUser();
    				
    				Log.d("SAMPLE", u.getName() + " @" + u.getScreenName() + " >>"
    						+ m.getText() + "<<");
    			}
    			
    		} else {
    			Log.w("SAMPLE", "No messages were fetched");
    			Toast.makeText(SpikeJTwitterActivity.this, R.string.ma_fetch_messages_empty, 
    					Toast.LENGTH_LONG).show(); 
    		}
	    } catch (Exception e) {
			Log.e("SAMPLE", "Unable to fetch messages", e);
			Toast.makeText(SpikeJTwitterActivity.this, R.string.ma_fetch_messages_failed, 
					Toast.LENGTH_LONG).show(); 
		}
    }
    
}

