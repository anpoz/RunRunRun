package com.playcode.runrunrun.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.playcode.runrunrun.R;
import com.playcode.runrunrun.model.RecordsEntity;
import com.playcode.runrunrun.utils.BOSUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ShowDetailActivity extends AppCompatActivity implements AMap.OnMapLoadedListener, LocationSource {

    private RecordsEntity runRecord;
    private PolylineOptions polylineOptions;
    private List<LatLng> points = new ArrayList<>();

    private TextView distance, calorie, speed, time, date;

    private Subscription mSubscription;

    private MapView mapView;
    private AMap aMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail);
        Intent intent = getIntent();
        runRecord = (RecordsEntity) intent.getSerializableExtra("runRecord");
        initView();
        setData();
        initMapView(savedInstanceState);
//        downloadPoints();
    }

    private void setData() {

        DecimalFormat df = new DecimalFormat("#0.00");
        String distanceStr = df.format(runRecord.getDistance() / 1000);

        float runTime = runRecord.getRunTime() * 1000 - 8 * 1000 * 3600;
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.time_format), Locale.getDefault());
        String timeStr = sdf.format(runTime);

        df = new DecimalFormat("#0.0");
        String calorieStr = df.format(runRecord.getCalorie());

        sdf = new SimpleDateFormat(getString(R.string.min_format),Locale.getDefault());
        float avgtime = (float) ((1.0 / ((runRecord.getDistance() / 1000) /
                runRecord.getRunTime())) * 1000 - 8 * 3600 * 1000);
        String speedStr = sdf.format(avgtime);

        sdf = new SimpleDateFormat(getString(R.string.date_hour_format),Locale.getDefault());
        String dateStr = sdf.format(runRecord.getDate());

        distance.setText(distanceStr);
        time.setText(timeStr);
        calorie.setText(calorieStr);
        speed.setText(speedStr);
        date.setText(dateStr);
    }

    private void initView() {
        distance = (TextView) findViewById(R.id.distance_sd);
        time = (TextView) findViewById(R.id.time_sd);
        speed = (TextView) findViewById(R.id.speed_sd);
        calorie = (TextView) findViewById(R.id.calorie_sd);
        date = (TextView) findViewById(R.id.date);
    }


    private void initMapView(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.map_item);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            UiSettings us = aMap.getUiSettings();
            us.setZoomControlsEnabled(false);
            setUpMap();
            aMap.setOnMapLoadedListener(this);
            polylineOptions = new PolylineOptions();
            polylineOptions.width(30);
            polylineOptions.color(ContextCompat.getColor(this, R.color.yellow));
        }
    }

    private void setUpMap() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeWidth(0);
        myLocationStyle.radiusFillColor(Color.alpha(Color.WHITE));
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMapType(AMap.MAP_TYPE_NIGHT);
        //设置定位监听 activate deactivate
        aMap.setLocationSource(this);
        //设置默认定位按钮是否显示
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        //设置为定位模式
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }


    /**
     * 下载点集合
     */
    private void downloadPoints() {

        mSubscription = BOSUtils.getInstance()
                .downloadPoints(runRecord.getPointsKey())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LatLng>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<LatLng> latLngs) {
                        points = latLngs;
                        aMap.addPolyline(polylineOptions.addAll(points));
                        double maxLat = points.get(0).latitude, minLat = points.get(0).latitude,
                                maxLong = points.get(0).longitude, minLong = points.get(0).longitude;
                        for (LatLng point : latLngs) {
                            if (point.latitude > maxLat) {
                                maxLat = point.latitude;
                            }
                            if (point.latitude < minLat) {
                                minLat = point.latitude;
                            }
                            if (point.longitude > maxLong) {
                                maxLong = point.longitude;
                            }
                            if (point.longitude < minLat) {
                                minLong = point.longitude;
                            }
                        }
                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng((maxLat + minLat) / 2, (maxLong + minLong) / 2)));
                    }
                });

    }

    @Override
    public void onMapLoaded() {
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        if (points != null) {
            aMap.addPolyline(polylineOptions);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        downloadPoints();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (aMap != null) {
            aMap.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
        points = null;
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }



    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }


    @Override
    public void deactivate() {

    }
}
