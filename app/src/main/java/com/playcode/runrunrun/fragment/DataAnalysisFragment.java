package com.playcode.runrunrun.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.playcode.runrunrun.App;
import com.playcode.runrunrun.R;
import com.playcode.runrunrun.model.RecordsEntity;
import com.playcode.runrunrun.utils.APIUtils;
import com.playcode.runrunrun.utils.RetrofitHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class DataAnalysisFragment extends Fragment {

    private List<RecordsEntity> mList;
    private LineChartView mLineChartView;
    private LineChartData data;
    private TextView mTextViewAvgSpeed;
    private TextView mTextViewAvgTime;
    private TextView mTextViewLongestTime;
    private TextView mTextViewLongestDistance;

    private int colorLine;
    private int color;


    public DataAnalysisFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_data_analysis, container, false);
        initView(rootView);

        return rootView;
    }

    private void initChart() {
        if (App.getServerMode() == App.SERVER_MODE.WITHOUT_SERVER) {
            mList = RecordsEntity.listAll(RecordsEntity.class);
            setupSummary();
        } else {
            SharedPreferences preferences = getActivity().getSharedPreferences("UserData", 0);
            String token = preferences.getString("token", "");
            prepareData(token);
        }
        setupChart();
    }


    private void initView(View rootView) {
        mLineChartView = (LineChartView) rootView.findViewById(R.id.chart);
        mLineChartView.setZoomType(ZoomType.VERTICAL);
        data = new LineChartData(new ArrayList<>());
        mLineChartView.setLineChartData(data);

        mTextViewAvgSpeed = (TextView) rootView.findViewById(R.id.tvAvgSpeed);
        mTextViewAvgTime = (TextView) rootView.findViewById(R.id.tvAvgTime);
        mTextViewLongestDistance = (TextView) rootView.findViewById(R.id.tvLongestDistance);
        mTextViewLongestTime = (TextView) rootView.findViewById(R.id.tvLongestTime);

        colorLine = ContextCompat.getColor(getActivity(), R.color.accent);
        color = ContextCompat.getColor(getActivity(), R.color.primary);
    }

    private void prepareData(String token) {
        RetrofitHelper.getInstance()
                .getService(APIUtils.class)
                .getUserRecords(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userRecordModel -> {
                    mList = userRecordModel.getRecords();
                    setupSummary();
                    setupChart();
                });
    }

    private void setupSummary() {
        float longestTime = 0;
        float longestDistance = 0;
        float avgSpeed;
        float avgTime;
        float totalDistance = 0;
        float totalTime = 0;

        SimpleDateFormat format = new SimpleDateFormat(getContext().getString(R.string.time_format),
                Locale.getDefault());

        if (mList == null || mList.size() == 0) {
            return;
        }

        for (int i = 0; i < mList.size(); i++) {
            RecordsEntity recordsEntity = mList.get(i);
            if (recordsEntity.getRunTime() > longestTime)
                longestTime = recordsEntity.getRunTime();
            if (recordsEntity.getDistance() > longestDistance)
                longestDistance = recordsEntity.getDistance();
            totalTime += recordsEntity.getRunTime();
            totalDistance += recordsEntity.getDistance();
        }

        avgTime = totalTime / mList.size();
        avgSpeed = (float) (totalDistance / totalTime / 3.6);

        mTextViewLongestDistance.setText(String.format(Locale.getDefault(),
                "%.2fkm", longestDistance / 1000));
        mTextViewLongestTime.setText(format.format(new Date((long) (longestTime * 1000) -
                TimeZone.getTimeZone("GMT+8:00").getRawOffset())));
        mTextViewAvgTime.setText(format.format(new Date((long) (avgTime * 1000) -
                TimeZone.getTimeZone("GMT+8:00").getRawOffset())));
        mTextViewAvgSpeed.setText(String.format(Locale.getDefault(), "%.2fkm/h", avgSpeed));
    }

    private void setupChart() {

        List<PointValue> values = new ArrayList<>();

        if (mList == null || mList.size() == 0) {
            for (int i = 0; i < 10; i++) {
                values.add(new PointValue(i + 1, 0));
            }
        } else {
            int size = mList.size() > 10 ? 10 : mList.size();
            for (int i = 0; i < size; i++) {
                PointValue pointValue = new PointValue();
                pointValue.set(i + 1, mList.get(i).getDistance() / 1000);
                pointValue.setLabel(String.format(Locale.getDefault(), "%.2f",
                        mList.get(i).getDistance() / 1000));
                values.add(pointValue);
            }
        }

        Line line = new Line(values);
        line.setColor(colorLine);
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(false);
        line.setFilled(false);
        line.setHasLabels(true);
        line.setHasLines(true);
        line.setHasPoints(true);

        List<Line> lines = new ArrayList<>();
        lines.add(line);

        data = new LineChartData(lines);

        //设置坐标轴
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisY.setName("km");

        axisX.setTextColor(color);
        axisY.setTextColor(color);
        axisX.setLineColor(color);
        axisY.setLineColor(color);

        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            axisValues.add(new AxisValue(i));
        }

        axisX.setValues(axisValues);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        mLineChartView.setLineChartData(data);
    }


    @Override
    public void onResume() {
        super.onResume();
        initChart();
    }
}
