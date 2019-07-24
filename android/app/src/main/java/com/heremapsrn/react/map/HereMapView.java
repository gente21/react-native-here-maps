package com.heremapsrn.react.map;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.here.android.mpa.common.ApplicationContext;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapOverlay;
import com.here.android.mpa.mapping.MapView;
import com.here.android.mpa.common.Image;

//test
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutingError;
import javax.annotation.Nullable;

import com.heremapsrn.R;

//import com.heremapsrn.react.utils.RouteListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HereMapView extends MapView {

    private static final String TAG = HereMapView.class.getSimpleName();

    private static final String MAP_TYPE_NORMAL = "normal";
    private static final String MAP_TYPE_SATELLITE = "satellite";

    private Button button;

    private Map map;

    private MapRoute mapRoute;

    private GeoCoordinate mapCenter;

    private GeoCoordinate mapOrigin;

    private GeoCoordinate mapDestination;

    private String mapType = "normal";

    private boolean mapIsReady = false;

    private double zoomLevel = 10;

    ArrayList<MapMarker> markers;

    // Force scroll to stop when moving the map start ******************************
    private ViewParent mViewParent;

    public void setViewParent(@Nullable final ViewParent viewParent) { //any ViewGroup
            mViewParent = viewParent;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (null == mViewParent) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    mViewParent.requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (null == mViewParent) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    mViewParent.requestDisallowInterceptTouchEvent(false);
                }
                break;
            default:
                break;
        }

        return super.onInterceptTouchEvent(event);
    }
    // Force scroll to stop when moving the map end ********************************

    private class RouteListener implements CoreRouter.Listener {

        // Method defined in Listener
        public void onProgress(int percentage) {
          // Display a message indicating calculation progress
        }
      
        // Method defined in Listener
        public void onCalculateRouteFinished(List<RouteResult> routeResult, RoutingError error) {
          // If the route was calculated successfully
          if (error == RoutingError.NONE) {
            // Render the route on the map
            mapRoute = new MapRoute(routeResult.get(0).getRoute());
            map.addMapObject(mapRoute);
          }
          else {
            Log.i(TAG, ">>UNABLE TO CALCULATE ROUTE");
          }
        }
    }

    public HereMapView(final Context context) {

        super(context);
        Log.i(TAG, ">>Initializing COMAN MEJILLONES");

        markers = new ArrayList<MapMarker>();

        // Set up disk cache path for the map service for this application
        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                context.getExternalFilesDir(null) + File.separator + ".here-maps",
                "com.heremapsrn");

        MapEngine.getInstance().init(new ApplicationContext(context), new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {

                    map = new Map();
                    setMap(map);

                    map.setMapScheme(Map.Scheme.NORMAL_DAY);

                    mapIsReady = true;

                    if (mapCenter != null) map.setCenter(mapCenter, Map.Animation.NONE);

                    /*
                    // Add the marker
                    if (markers != null) {
                        for (MapMarker marker : markers) {
                            map.addMapObject(marker);
                        }
                    }

                    Log.d(TAG, String.format("mapType: %s", mapType));
                    setMapType(mapType);

                    setZoomLevel(zoomLevel);

                    

                    // Create a gesture listener on marker object
                    getMapGesture().addOnGestureListener(
                            new MapGesture.OnGestureListener.OnGestureListenerAdapter() {
                                @Override
                                public boolean onMapObjectsSelected(List<ViewObject> objects) {
                                    for (ViewObject viewObj : objects) {
                                        if (viewObj.getBaseType() == ViewObject.Type.USER_OBJECT) {
                                            if (((MapObject) viewObj).getType() == MapObject.Type.MARKER) {
                                                // At this point we have the originally added
                                                // map marker, so we can do something with it
                                                // (like change the visibility, or more
                                                // marker-specific actions)
                                                MapMarker mapMarker = (MapMarker) viewObj;
                                                TextView helloTextView = new TextView(context);
                                                helloTextView.setText(mapMarker.getTitle());
                                                MapOverlay mapOverlay = new MapOverlay(helloTextView, mapMarker.getCoordinate());
                                                mapOverlay.setAnchorPoint(new PointF(80.0f, 150.0f));
                                                // map.addMapOverlay(mapOverlay);

                                                button = new Button(context);
                                                button.setText(mapMarker.getTitle());
                                                // create overlay and add it to the map
                                                map.addMapOverlay(
                                                        new MapOverlay(button, mapMarker.getCoordinate()));
                                            }
                                        }
                                    }
                                    // return false to allow the map to handle this callback also
                                    return false;
                                }
                            }, 1, true );
                    */

                    // Route calculation
                    CoreRouter router = new CoreRouter();
                    // Create the RoutePlan and add two waypoints
                    RoutePlan routePlan = new RoutePlan();
                    routePlan.addWaypoint(new RouteWaypoint(mapOrigin));
                    routePlan.addWaypoint(new RouteWaypoint(mapDestination));
                    // Create the RouteOptions and set its transport mode & routing type
                    RouteOptions routeOptions = new RouteOptions();
                    routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
                    routeOptions.setRouteType(RouteOptions.Type.FASTEST);
                    routePlan.setRouteOptions(routeOptions);
                    router.calculateRoute(routePlan, new RouteListener());

                    map.setCenter(routePlan.getWaypoint(0).getNavigablePosition(),
                    Map.Animation.BOW);


                    Log.i(TAG, "INIT FINISH !!!!");

                } else {
                    Log.e(TAG, String.format("Error initializing map: %s", error.getDetails()));
                }


            }
        });


    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause...");
        MapEngine.getInstance().onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume...");
        MapEngine.getInstance().onResume();
    }

    public void setCenter(String center) {
        String[] values = center.split(",");

        if (values.length == 2) {
            double latitude = Double.parseDouble(values[0]);
            double longitude = Double.parseDouble(values[1]);

            mapCenter = new GeoCoordinate(latitude, longitude);
            if (mapIsReady) map.setCenter(mapCenter, Map.Animation.NONE);
        } else {
            Log.w(TAG, String.format("Invalid center: %s", center));
        }
    }

    public void setOrigin(String origin) {
        String[] values = origin.split(",");

        if (values.length == 2) {
            double latitude = Double.parseDouble(values[0]);
            double longitude = Double.parseDouble(values[1]);

            mapOrigin = new GeoCoordinate(latitude, longitude);
            // Log.i(TAG, ">>Got origin: "+origin);
            //if (mapIsReady) map.setOrigin(mapOrigin, Map.Animation.NONE);
        } else {
            Log.w(TAG, String.format("Invalid origin: %s", origin));
        }
    }

    public void setDestination(String destination) {
        String[] values = destination.split(",");

        if (values.length == 2) {
            double latitude = Double.parseDouble(values[0]);
            double longitude = Double.parseDouble(values[1]);

            mapDestination = new GeoCoordinate(latitude, longitude);
            // Log.i(TAG, ">>Got destination: "+destination);
            //if (mapIsReady) map.setDestination(mapDestination, Map.Animation.NONE);
        } else {
            Log.w(TAG, String.format("Invalid destination: %s", destination));
        }
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
        if (!mapIsReady) return;

        if (mapType.equals(MAP_TYPE_NORMAL)) {
            map.setMapScheme(Map.Scheme.NORMAL_DAY);
        } else if (MAP_TYPE_SATELLITE.equals(mapType)) {
            map.setMapScheme(Map.Scheme.SATELLITE_DAY);
        }
    }

    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel = zoomLevel;
        if (!mapIsReady) return;

        map.setZoomLevel(zoomLevel);
    }

    public void setMarker(String markerPosition) {

        String[] values = markerPosition.split(",");

        if (values.length == 2) {
            double latitude = Double.parseDouble(values[0]);
            double longitude = Double.parseDouble(values[1]);

            // Create a custom marker image
            Image myImage = new Image();

            try {
                myImage.setImageResource(R.drawable.marker);
            } catch (IOException e) {
                Log.e(TAG, String.format("Error initializing image marker: %s", e.getMessage()));
            }
            // Create the MapMarker
            MapMarker marker =
                    new MapMarker(new GeoCoordinate(latitude, longitude), myImage);

            markers.add(marker);

            if (mapIsReady) map.addMapObject(marker);
        } else {
            Log.w(TAG, String.format("Invalid marker position: %s", markerPosition));
        }
    }

    public void setMarkersList(ReadableArray markersPosition) {

        for(int i=0; i< markersPosition.size(); i++) {

            ReadableMap readableMap = markersPosition.getMap(i);

            // String[] values = readableMap.getString("location").split(",");

                double latitude = readableMap.getDouble("latitude");
                double longitude = readableMap.getDouble("longitude");

                String title = readableMap.getString("title");
                String description = readableMap.getString("description");

                // Create a custom marker image
                Image myImage = new Image();

                try {
                    myImage.setImageResource(R.drawable.marker);
                } catch (IOException e) {
                    Log.e(TAG, String.format("Error initializing image marker: %s", e.getMessage()));
                }

                //Create the MamMarker
                MapMarker marker = new MapMarker(new GeoCoordinate(latitude, longitude), myImage);
                marker.setAnchorPoint(new PointF(myImage.getWidth() / 2f, myImage.getHeight()));

                marker.setTitle(title);
                marker.setDescription(description);

                // Add the MapMarker in the list
                markers.add(marker);

                if (mapIsReady) map.addMapObject(marker);

        }

    }

}
