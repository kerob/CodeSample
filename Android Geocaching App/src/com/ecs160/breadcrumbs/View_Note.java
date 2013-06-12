package com.ecs160.breadcrumbs;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class View_Note extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_note);

		HashMap<String, String> hm = SimpleDB.getAttributesForItem("Notes", Outbox.ITEM);
		EditText contact = (EditText) findViewById(R.id.contact);
		contact.setText(hm.get("Number").toString());
		
		EditText location = (EditText) findViewById(R.id.location);
		location.setText(hm.get("Location").toString()); 

		EditText message = (EditText) findViewById(R.id.message);
		message.setText(hm.get("Message").toString());
	}
}