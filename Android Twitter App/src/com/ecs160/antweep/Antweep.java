package com.ecs160.antweep;

import winterwell.jtwitter.OAuthSignpostClient;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Antweep extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
    
	public static final String TOKEN_PREFS = "tokens";
	public static final String JTWITTER_OAUTH_KEY = "b2xBKr9od22TIPmTwi66Q";
	public static final String JTWITTER_OAUTH_SECRET = "ZTzWqW1z6eZRPskdlYz0hKP7pUf3Nu99n9FPDkn5w4";
        //Globals. you can do better
	public static String ACCESS_TOKEN="";
	public static String ACCESS_KEY="";
	public static String UserName;
	OAuthSignpostClient oauthClient;
	Button authorize;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        oauthClient = new OAuthSignpostClient(JTWITTER_OAUTH_KEY, 
       		JTWITTER_OAUTH_SECRET,"oob");
        
        final String authUrl = oauthClient.authorizeUrl().toString();
        Intent i= new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
        startActivity(i);
            
        authorize = (Button)findViewById( R.id.start_btn );
        authorize.setOnClickListener(this);
       
    }
    /* Handle when the back button is pressed. After the Twitter 'Accept' webpage
     * has issued you a pin, you need to enter it, and the pressing the back buttton allows you
     * to go back to your Application start page, as opposed to ending the ctivity.
    */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) ) {
        	finish(); //finish the webpage activity
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    public void onClick(View v)
    {
    	if(v.equals(findViewById(R.id.start_btn)))
    	{		//after clicking on the 'confirm pin' button, disable it
    		        authorize.setEnabled(false);

			EditText pin = (EditText) findViewById(R.id.pin_entry);
			String verifier = pin.getText().toString().trim();
			pin.setText("");
			
			EditText name = (EditText) findViewById(R.id.name_entry);
			UserName = name.getText().toString().trim();
			

			oauthClient.setAuthorizationCode(verifier);
			String[] pair = oauthClient.getAccessToken();
					
			ACCESS_TOKEN=pair[0];
			ACCESS_KEY=pair[1];
			//How to store and retrieve the tokens (and any other (key,value) pair) using SharedPreferences
			//Editor tokenFile = getSharedPreferences(TOKEN_PREFS,0).edit();
			//tokenFile.putString(pair[0], pair[1]);
			//tokenFile.commit();
			//SharedPreferences settings = getSharedPreferences(TOKEN_PREFS,0);
			Intent i = new Intent(this, FriendList.class);
			startActivity(i);
			
    	}
    }
       
}
