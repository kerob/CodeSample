package com.ecs160.breadcrumbs;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Outbox extends ListActivity {
	public static String ITEM;
	public static final int INSERT_ID = Menu.FIRST;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_list);
		setList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.menu_insert);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case INSERT_ID:
			Intent intent = new Intent(this, Create_Note.class);
			this.startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setList() {
		String[] recipients = SimpleDB.getItemNamesForDomain("Notes_Send");
		this.setListAdapter(new ArrayAdapter<String>(this, R.layout.note_row,
				recipients));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Object o = this.getListAdapter().getItem(position);
		ITEM = o.toString();
		Intent intent = new Intent(this, View_Note.class);
		this.startActivity(intent);
	}
}
