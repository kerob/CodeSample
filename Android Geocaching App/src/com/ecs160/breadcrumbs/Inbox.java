package com.ecs160.breadcrumbs;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class Inbox extends ListActivity {

	public static final int DELETE_ID = Menu.FIRST;
	public static String ITEM;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_list);
		setList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, DELETE_ID, 0, R.string.delete);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			deleteNote();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void deleteNote() {
	}

	private void setList() {
		String[] recipients = SimpleDB.getItemNamesForDomain("Notes_Receive");
		this.setListAdapter(new ArrayAdapter<String>(this, R.layout.note_row,
				recipients));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Object o = this.getListAdapter().getItem(position);
		ITEM = o.toString();
		Intent intent = new Intent(this, NoteStatus.class);
		this.startActivity(intent);
	}
}
