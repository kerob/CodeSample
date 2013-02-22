package com.ecs160.antweep;

import java.util.ArrayList;
import java.util.List;

import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.User;
import winterwell.jtwitter.TwitterException;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.database.Cursor;


public class FriendList extends ListActivity {
	
	private static final String USER_NAME=Antweep.UserName;
	public static OAuthSignpostClient FoauthClient;
	public static Twitter twitter;
	public static String friend;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.test);
		registerForContextMenu(getListView());

		//create an oauthclient with everything that's needed for authorization
		FoauthClient = new OAuthSignpostClient(Antweep.JTWITTER_OAUTH_KEY, 
        		Antweep.JTWITTER_OAUTH_SECRET, Antweep.ACCESS_TOKEN, Antweep.ACCESS_KEY );
		//Now you can instantiate a Twitter object that can actually work		
		twitter = new Twitter(USER_NAME, FoauthClient);
		
		EditText stat = (EditText) findViewById(R.id.stat_entry);
		String gettweet = twitter.getStatus().toString().trim();
		//
		//SharedPreferences settings = getSharedPreferences(Antweep.TOKEN_PREFS, MODE_WORLD_READABLE);
       
        
		//Creates a List of Friends and converts them to strings to user in the ListAdapter
		List<User> arr= twitter.getFriends();
		Object[] str=arr.toArray();
		String[] friends = new String[str.length];
		for(int i=0;i<str.length;i++) {
			friends[i] = str[i].toString();
		}
		// Make Array Adapter to use strings in listview
		this.setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, friends));
		
		// Get Latest Tweet
		stat.setText(gettweet);

		
		//SharedPreferences settings = getSharedPreferences(Antweep.TOKEN_PREFS,0);

		
	}

    public void onClick(View v)
    {
    	//Checks for new tweet
    	if(v.equals(findViewById(R.id.tweet)))
    	{		
    		EditText stat = (EditText) findViewById(R.id.stat_entry);
			String post = stat.getText().toString().trim();
			   try{
	                twitter.setStatus(post);          //Set New Status and Notify User
	                Toast.makeText(FriendList.this, "New Tweet Successfully Posted!", Toast.LENGTH_SHORT).show();
	            } catch (TwitterException e) {	//Notify of Error
	            	Toast.makeText(FriendList.this, "Error: Tweet Not Posted", Toast.LENGTH_SHORT).show();
	            }
			
			
    	}
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// Get the Clicked Friend
		Object o = this.getListAdapter().getItem(position);
		String selected = o.toString();
		// Alert User of Which Friend
		Toast.makeText(this, "Checking Friend: " + selected, Toast.LENGTH_LONG)
				.show();
		friend = selected;
		//Start Activity to show Friend's tweets
		Intent i2 = new Intent(this, Friend_tweets.class);
		startActivityForResult(i2,0);
	}
	
}
