package com.ecs160.breadcrumbs;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class Note_Settings extends ListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_list);
		
		String settings[] = {"Vibrate", "Sound", "Alert Radius"};
		setListAdapter(new ArrayAdapter<String>(this, R.layout.note_row, settings));
	}
}
