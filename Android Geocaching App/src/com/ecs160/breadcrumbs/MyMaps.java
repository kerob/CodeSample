package com.ecs160.breadcrumbs;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MyMaps extends MapActivity {
	private MapView mapView;
	private GeoPoint point;
	private MapController mc;
	private Point screenPts; //use this global variable to convert to string and store id database
	private LocationManager locationManager;
	private String provider;
	public static double longitude;
	public static double latitude;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mymaps);
        ///*
	    mapView = (MapView) findViewById(R.id.mapView);
	    mapView.setBuiltInZoomControls(true);
	    
	    //List<Overlay> mapOverlays = mapView.getOverlays();
	    //Drawable drawable = this.getResources().getDrawable(R.drawable.icon);
	    //HelloItemizedOverlay itemizedoverlay = new HelloItemizedOverlay(drawable);
	    
	    //fixed location, need to make dynamic so map startup is at where you are
	    //point = new GeoPoint(19240000,-99120000); //mexico city
	    //point = new GeoPoint(38575169, -121478218); //sacramento 
	    point = new GeoPoint(42000000, -100000000); // US

	    //OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
	    
	    mc = mapView.getController();
        mc.animateTo(point);
        mc.setZoom(4); 
        mapView.invalidate();
//*/

        MapOverlay mapOverlay = new MapOverlay();
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);        
 
        mapView.invalidate();
/*
        //DEFAULT LOCATION, use gps, it must be on
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the location provider -> use
		// default
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);

        EditText address = (EditText) findViewById(R.id.address_text);

		// Initialize the location fields
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			int lat = (int) (location.getLatitude());
			int lng = (int) (location.getLongitude());
			address.setText(String.valueOf(lat) + String.valueOf(lng));
		} else {
			address.setText("GPS not on");			
		}
//*/	
	}
	
    class MapOverlay extends com.google.android.maps.Overlay
    {
        @Override
        public boolean draw(Canvas canvas, MapView mapView, 
        boolean shadow, long when) 
        {
            super.draw(canvas, mapView, shadow);                   
 
            //---translate the GeoPoint to screen pixels---
            screenPts = new Point();
            //Point screenPts = new Point();
            mapView.getProjection().toPixels(point, screenPts);
 
            //---add the marker---
            Bitmap bmp = BitmapFactory.decodeResource(
                getResources(), R.drawable.mappin);            
            canvas.drawBitmap(bmp, screenPts.x, screenPts.y-50, null);         
            return true;
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) 
        {   
            EditText address = (EditText) findViewById(R.id.address_text);
            
        	GeoPoint p = mapView.getProjection().fromPixels(
                    (int) event.getX(),
                    (int) event.getY());

        	//---when user lifts his finger---
            if (event.getAction() == 1) {                
              //  /*
                Geocoder geoCoder = new Geocoder(
                        getBaseContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geoCoder.getFromLocation(
                            p.getLatitudeE6()  / 1E6, 
                            p.getLongitudeE6() / 1E6, 1);
     
                        String add = "";
                        if (addresses.size() > 0) 
                        {
                            for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); 
                                 i++)
                               add += addresses.get(0).getAddressLine(i) + "\n";
                        }
     
                        Toast.makeText(getBaseContext(), add, Toast.LENGTH_SHORT).show();
                        
                        address.setText(add); //set text to pick point on map
                    }
                    catch (IOException e) {                
                        e.printStackTrace();
                    }   
                    return true;
                }
                else  {              
                    //return false;
            //        */
            ///*
//                    address.setText("Not found address");
                    address.setText(p.getLatitudeE6() + " " + p.getLongitudeE6());

                	Toast.makeText(getBaseContext(), 
                        p.getLatitudeE6() / 1E6 + "," + 
                        p.getLongitudeE6() /1E6 , 
                        Toast.LENGTH_SHORT).show();
                    
                    point = new GeoPoint(p.getLatitudeE6(), p.getLongitudeE6());
            }                            
            return false;
            //*/
        }        
    }
    
    public void onClick(View v)
    {
    	if(v.equals(findViewById(R.id.finish_button)))
    	{
    		latitude = point.getLatitudeE6();
    		longitude =point.getLongitudeE6();

    		Toast toast = Toast.makeText(getBaseContext(), "Finished", Toast.LENGTH_SHORT);
    		toast.show();
    		this.finish();
    		//return object here
    	}
    	else if(v.equals(findViewById(R.id.set)))
    	{
            EditText address = (EditText) findViewById(R.id.address_text);
    		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());    
            try {
                List<Address> addresses = geoCoder.getFromLocationName(
                		address.getText().toString(), 5);
                		//"empire state building", 5);
                String add = "";
                if (addresses.size() > 0) {
                    point = new GeoPoint(
                            (int) (addresses.get(0).getLatitude() * 1E6), 
                            (int) (addresses.get(0).getLongitude() * 1E6));
                    mc.animateTo(point);    
                    mapView.invalidate();
                }    
            } catch (IOException e) {
                e.printStackTrace();
            }
    		Toast toast = Toast.makeText(getBaseContext(), address.getText().toString(), Toast.LENGTH_SHORT);
    		toast.show();
    	}
    }
    
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
}