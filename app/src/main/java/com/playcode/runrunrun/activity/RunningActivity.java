package com.playcode.runrunrun.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.playcode.runrunrun.R;
import com.playcode.runrunrun.model.MessageModel;
import com.playcode.runrunrun.model.RecordsEntity;
import com.playcode.runrunrun.utils.APIUtils;
import com.playcode.runrunrun.utils.AccessUtils;
import com.playcode.runrunrun.utils.BOSUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class RunningActivity extends AppCompatActivity implements AMapLocationListener, LocationSource,
        AMap.OnMapLoadedListener, View.OnClickListener {

    private static final int MIN_ALLOW_DISTANCE = 1;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationClientOption;
    private AMap aMap;
    //处理定位更新的接口
    private OnLocationChangedListener mListener;
    private static final String INFO = "info";
    MapView mapView;
    TextView showTime, showDistance;
    LatLng currentLatLng;
    float distance;
    Button start, stop;
    //标识跑步状态
    volatile boolean isStart = false;
    boolean isFirstStart = true;
    //轨迹点集合
    private List<LatLng> points = new ArrayList<>();
    private PolylineOptions polylineOptions;
    //计时器变量
    long firstStartTime, currentTime, startTime = 0, sumTime = 0;

    private RecordsEntity runRecord = new RecordsEntity();
    private String token;
    private float weight;

    public static final String BASE_URL = "http://codeczx.duapp.com/FitServer/";


    private Subscription mTimer;
    private Retrofit mRetrofit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_running);

        SharedPreferences preferences = getSharedPreferences("UserData", 0);
        token = preferences.getString("token", "");
        weight = preferences.getFloat("weight", 60);

        init(savedInstanceState);

        if (!AccessUtils.isNetworkConnected(this)) {
            Toast.makeText(this, "网络未连接~", Toast.LENGTH_SHORT).show();
            RunningActivity.this.finish();
        }

        if (!AccessUtils.isGPSEnabled(this)) {
            new AlertDialog
                    .Builder(this)
                    .setTitle("必须开启GPS才能使功能正常运行，是否开启？")
                    .setPositiveButton("不开启", (dialog, which) -> {
                        RunningActivity.this.finish();
                    })
                    .setNegativeButton("前往开启", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 0);
                    })
                    .create()
                    .show();
        }

        locationClient = new AMapLocationClient(getApplicationContext());
        locationClient.setLocationListener(this);

        locationClientOption = new AMapLocationClientOption();

        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationClientOption.setNeedAddress(true);

        //设置定位间隔,单位毫秒,默认为2000ms
        locationClientOption.setInterval(2000);
        locationClient.setLocationOption(locationClientOption);
        //启动定位，调用onLocationChange
        locationClient.startLocation();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm:ss.S")
                                .create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

    }


    private void init(Bundle savedInstanceState) {

        mapView = (MapView) findViewById(R.id.map);
        showTime = (TextView) findViewById(R.id.show_time);
        showDistance = (TextView) findViewById(R.id.show_distance);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

        start.setOnClickListener(this);
        stop.setOnClickListener(this);

        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
            aMap.setOnMapLoadedListener(this);
            polylineOptions = new PolylineOptions();
            polylineOptions.width(30);
            polylineOptions.color(ContextCompat.getColor(this, R.color.primary_dark));
//            polylineOptions.useGradient(true);
//            List<Integer>colors = new ArrayList<>();
//            colors.add(0xff00ff00);
//            colors.add(0xffff0000);
//            colors.add(0xff0000ff );
//            polylineOptions.colorValues(colors);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stop:
                isStart = false;
                if (mTimer != null && !mTimer.isUnsubscribed())
                    mTimer.unsubscribe();
                //记录本次暂停开始时的毫秒数
                startTime = System.currentTimeMillis();
                start.setText(getResources().getString(R.string.running_start_locate));
                startDialog();
                break;
            case R.id.start:
                isStart = !isStart;
                if (isStart) {
                    startTimer();
                    if (isFirstStart) {
                        //记录初次开始毫秒数
                        firstStartTime = System.currentTimeMillis();
                        isFirstStart = false;
                        runRecord.setDate(new Timestamp(firstStartTime));
                    } else {
                        //每次暂停加起来的毫秒数
                        sumTime = sumTime + System.currentTimeMillis() - startTime;
                    }
                    start.setText(getResources().getString(R.string.running_pause_locate));
                } else {
                    if (mTimer != null && !mTimer.isUnsubscribed())
                        mTimer.unsubscribe();
                    //记录本次暂停开始时的毫秒数
                    startTime = System.currentTimeMillis();
                    start.setText(getResources().getString(R.string.running_start_locate));
                }
                break;
        }
    }

    private void uploadData() {
        if (points.size() == 0)
            RunningActivity.this.finish();

        ProgressDialog dialog = new ProgressDialog(RunningActivity.this);
        dialog.setTitle("正在上传数据...");
        dialog.show();

        Gson gson = new Gson();
        String pointsStr = gson.toJson(points);
        String pointsKey = UUID.randomUUID().toString();
        runRecord.setPointsKey(pointsKey);

        GeocodeSearch geocodeSearch = new GeocodeSearch(RunningActivity.this);
        LatLonPoint point = new LatLonPoint(points.get(0).latitude, points.get(0).longitude);

        BOSUtils.getInstance()
                .uploadPoints(pointsStr, pointsKey)
                .subscribeOn(Schedulers.io())
                .flatMap(s -> Observable.just(point))
                .flatMap(latLonPoint -> Observable.create((Observable.OnSubscribe<String>) subscriber -> {
                    try {
                        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(point, 1000, GeocodeSearch.AMAP);
                        RegeocodeAddress regeocodeAddress = geocodeSearch.getFromLocation(regeocodeQuery);
                        if (null != regeocodeAddress) {
                            subscriber.onNext(regeocodeAddress.getCity() + regeocodeAddress.getDistrict());
                        }
                    } catch (AMapException e) {
                        subscriber.onError(e);
                    }
                    subscriber.onCompleted();
                }))
                .flatMap(this::uploadRecord)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MessageModel>() {
                    @Override
                    public void onCompleted() {
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();
                        Toast.makeText(RunningActivity.this, "上传记录时出错了," + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(MessageModel messageModel) {
                        Intent intent1 = new Intent(RunningActivity.this, ShowDetailActivity.class);
                        intent1.putExtra("runRecord", runRecord);
                        startActivity(intent1);
                        if (locationClient != null) {
                            locationClient.stopLocation();
                            locationClient.onDestroy();
                            locationClient = null;
                            locationClientOption = null;
                        }
                        RunningActivity.this.finish();
                    }
                });


    }

    private Observable<MessageModel> uploadRecord(String address) {
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
        String date = sdf.format(runRecord.getDate());
        float runTime = (currentTime + 8 * 60 * 60 * 1000) / 1000;
        //速度 米/秒
        float speed = distance / runTime;
        //速度 分钟/400米
        speed = 400 / speed / 60;
        //跑步热量（kcal）＝体重（kg）×运动时间（hour）×指数K  指数K＝30÷速度（分钟/400米）
        float calorie = weight * (runTime / 3600) * (30 / speed);

        runRecord.setRunTime(runTime);
        runRecord.setCalorie(calorie);
        runRecord.setDistance(distance);

        return mRetrofit.create(APIUtils.class)
                .add("add", token, date,
                        String.valueOf(distance), String.valueOf(calorie)
                        , String.valueOf(runTime), runRecord.getPointsKey(), address);
    }

    /**
     * 是否结束本次运动的dialog
     */
    private void startDialog() {

        if (distance < MIN_ALLOW_DISTANCE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("本次运动距离太短将不会保存，是否结束运动");
            builder.setNegativeButton("继续运动", (dialog, which) -> {
                isStart = true;
                sumTime = sumTime + System.currentTimeMillis() - startTime;
                start.setText(getResources().getString(R.string.running_pause_locate));
                startTimer();
                Toast.makeText(RunningActivity.this, "继续运动", Toast.LENGTH_SHORT).show();
            });
            builder.setPositiveButton("结束运动", (dialog, which) -> {
                if (locationClient != null) {
                    locationClient.stopLocation();
                    locationClient.onDestroy();
                    locationClient = null;
                    locationClientOption = null;
                }
                finish();
            });
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("保存本次运动");
            builder.setNegativeButton("取消", null);
            builder.setPositiveButton("确定", (dialog, which) -> {

                uploadData();

            });
            builder.show();
        }
    }


    /**
     * 启动计时器的方法
     */
    public void startTimer() {

        mTimer = Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                    currentTime = System.currentTimeMillis() - firstStartTime - 8 * 60 * 60 * 1000 - sumTime;
                    showTime.setText(simpleDateFormat.format(currentTime));
                });

    }

    /**
     * 设置AMap的属性
     */
    private void setUpMap() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeWidth(0);
        myLocationStyle.radiusFillColor(Color.alpha(Color.WHITE));
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        //设置定位监听
        aMap.setLocationSource(this);
        //设置默认定位按钮是否显示
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        //设置为定位模式
        aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
    }

    /**
     * 地图加载完成后调用此方法
     */
    @Override
    public void onMapLoaded() {
        aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel()));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        if (points != null) {
            aMap.addPolyline(polylineOptions);
        }
    }

    /**
     * 定位回调监听，当定位完成后调用此方法
     *
     * @param aMapLocation 定位数据
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
//        Log.i("info","aMaplocation"+i++);
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //显示系统小蓝点
                mListener.onLocationChanged(aMapLocation);
                currentLatLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                //精度小于预定值且开始运动
                if (aMapLocation.getAccuracy() < 15 && isStart) {
                    aMap.addPolyline(polylineOptions.add(currentLatLng));
                    points.add(currentLatLng);
                    if (points.size() >= 2) {
                        distance += AMapUtils.calculateLineDistance(points.get(points.size() - 1), points.get(points.size() - 2));
                        String disStr = String.format(Locale.getDefault(),"%.2f", distance / 1000);
                        showDistance.setText(disStr);
                    }
                }
            } else {
                Log.i("info", aMapLocation.getErrorInfo());
            }
        }
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
        mapView.onDestroy();
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
            locationClient = null;
            locationClientOption = null;
        }

        if (mTimer != null && !mTimer.isUnsubscribed())
            mTimer.unsubscribe();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        Log.i(INFO, "activate");
        mListener = onLocationChangedListener;
        if (locationClient != null) {
            locationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        Log.i(INFO, "deactivate");
        mListener = null;
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        locationClient = null;
    }

}
