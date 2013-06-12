package com.ecs160.antweep;

import java.util.List;

import winterwell.jtwitter.Twitter.User;
import winterwell.jtwitter.Twitter.Status;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.app.ListActivity;


public class Friend_tweets extends ListActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.friend_tweets);
		
		registerForContextMenu(getListView());
		// Prepare Friend's Tweets similar to how we get the friends in FriendList
		List<Status> Ftweets = FriendList.twitter.getUserTimeline(FriendList.friend);
		Object[] str= Ftweets.toArray();
		String[] ft = new String[str.length];
		for(int i=0;i<str.length;i++) {
			ft[i] = str[i].toString();
		}
		this.setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, ft));
	}
}
