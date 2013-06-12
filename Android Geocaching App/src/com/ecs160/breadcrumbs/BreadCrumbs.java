package com.ecs160.breadcrumbs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class BreadCrumbs extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	Intent intent;

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.inbox:
			intent = new Intent(this, Inbox.class);
			startActivity(intent);
			break;
		case R.id.outbox:
			intent = new Intent(this, Outbox.class);
			startActivity(intent);
			break;
		case R.id.create:
			intent = new Intent(this, Create_Note.class);
			startActivity(intent);
			break;
		case R.id.note_settings:
			intent = new Intent(this, Note_Settings.class);
			startActivity(intent);
			break;
		case R.id.phone_settings:
			intent = new Intent(this, Phone_Settings.class);
			startActivity(intent);
			break;
		}
	}
}
