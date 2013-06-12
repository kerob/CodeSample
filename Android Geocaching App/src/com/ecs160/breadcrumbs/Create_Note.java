package com.ecs160.breadcrumbs;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.net.URL;

//import com.google.gdata.client.*;
//import com.google.gdata.client.photos.*;
//import com.google.gdata.data.*;
//import com.google.gdata.data.media.*;
//import com.google.gdata.data.photos.*;

public class Create_Note extends Activity {
	private static final int PICK_CONTACT = 3;
	private static final int SELECT_IMAGE = 4;
	public String CONTACT_NAME = "";
	public String CONTACT_NUMBER = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_note);

		/* Use the LocationManager class to obtain GPS locations */
		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener mlocListener = new MyLocationListener();
		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
				mlocListener);
		//ImageView yourimgView=(ImageView)findViewById(R.id.picture);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.send) {
			createNote();
		}
		if (v.getId() == R.id.get_contact) {
			Intent intent = new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
		}
		if (v.getId() == R.id.get_location) {
			Intent i = new Intent(this, MyMaps.class);
			startActivity(i);
			EditText location = (EditText) findViewById(R.id.location);
			location.setText(MyMaps.longitude + " " + MyMaps.latitude);
		}
		if (v.getId() == R.id.get_pic){
			  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			  intent.setType("image/*");
			  startActivityForResult(intent, SELECT_IMAGE);		}
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					CONTACT_NAME = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
				}
				ContentResolver cr = getContentResolver();
				Cursor pCur = cr
						.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
								new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
								ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
										+ " = ?",
								new String[] { CONTACT_NAME }, null);
				while (pCur.moveToNext()) {
					CONTACT_NUMBER = pCur
							.getString(pCur
									.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
					EditText receiver = (EditText) findViewById(R.id.contact);
					receiver.setText(CONTACT_NUMBER);
				}
				pCur.close();
			}
		case (SELECT_IMAGE):
		    if (resultCode == Activity.RESULT_OK) {
		      Uri selectedImage = data.getData();
		      String[] filePathColumn = {MediaStore.Images.Media.DATA};
		      
		      Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	             cursor.moveToFirst();

	             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	             String filePath = cursor.getString(columnIndex); // file path of selected image
	             cursor.close();
	 
	             Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
	           
	             ImageView yourimgView=(ImageView)findViewById(R.id.picture);    
	             yourimgView.setImageBitmap(yourSelectedImage);
		    } 
		}
		
	}

	private void createNote() {
		EditText location = (EditText) findViewById(R.id.location);
		EditText message = (EditText) findViewById(R.id.message);
		
		String dblocation = location.getText().toString().trim();
		String dbmessage = message.getText().toString().trim();

		SimpleDB.createItem("Notes_Send", CONTACT_NAME);
		SimpleDB.createAttributeForItem("Notes_Send", CONTACT_NAME, "Number",
				CONTACT_NUMBER);
		SimpleDB.createAttributeForItem("Notes_Send", CONTACT_NAME, "Message",
				dbmessage);
		SimpleDB.createAttributeForItem("Notes_Send", CONTACT_NAME, "Location",
				dblocation);

		// text message
		SmsManager myManager = SmsManager.getDefault();
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(
				"SMS_SENT"), 0);
		PendingIntent pi2 = PendingIntent.getBroadcast(this, 0, new Intent(
				"SMS_DELIVERED"), 0);
		// myManager.sendTextMessage(dbreceiver, null, dbmessage, pi, pi2);

	}

	/* Class My Location Listener */
	public class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			loc.getLatitude();
			loc.getLongitude();
			String Text = "My current location is: " + "Latitude = "
					+ loc.getLatitude() + "Longitude = " + loc.getLongitude();
			Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT)
					.show();
			EditText location = (EditText) findViewById(R.id.location);
			location.setText(loc.getLatitude() + " " + loc.getLongitude() + "");
		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Disabled",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Enabled",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
}
