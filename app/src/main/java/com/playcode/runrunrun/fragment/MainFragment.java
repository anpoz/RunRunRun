package com.playcode.runrunrun.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.playcode.runrunrun.App;
import com.playcode.runrunrun.R;
import com.playcode.runrunrun.model.RecordsEntity;
import com.playcode.runrunrun.utils.APIUtils;
import com.playcode.runrunrun.utils.RetrofitHelper;

import java.util.List;
import java.util.Locale;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private TextView distance;
    private TextView count;
    private TextView time;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        initView(rootView);
//        initData();
        return rootView;
    }

    private void initData() {
        if (App.getServerMode() == App.SERVER_MODE.WITHOUT_SERVER) {
            List<RecordsEntity> recordsEntity = RecordsEntity.listAll(RecordsEntity.class);
            if (recordsEntity != null && !recordsEntity.isEmpty())
                setupData(recordsEntity);
        } else {
            SharedPreferences setting = getActivity().getSharedPreferences("UserData", 0);
            String token = setting.getString("token", "0");
            if (token.equals(""))
                return;

            RetrofitHelper.getInstance()
                    .getService(APIUtils.class)
                    .getUserRecords(token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(userRecordModel -> {
//                    if (userRecordModel.getResultCode() != 0) {
//                        Toast.makeText(getActivity(), userRecordModel.getMessage(), Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                        if (userRecordModel.getRecords() != null && userRecordModel.getRecords().size() != 0) {
                            setupData(userRecordModel.getRecords());
                        }
                    });
        }
    }

    private void setupData(List<RecordsEntity> records) {
        int size = records.size();
        float timeCount = 0;
        float distanceCount = 0;
        for (int i = 0; i < size; i++) {
            RecordsEntity recordsEntity = records.get(i);
            timeCount += recordsEntity.getRunTime();
            distanceCount += recordsEntity.getDistance();
        }
        distance.setText(String.format(Locale.getDefault(), "%.2f", distanceCount / 1000));
        time.setText(String.format(Locale.getDefault(), "%.2f", timeCount / 3600));
        count.setText(String.valueOf(size));
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void initView(View view) {
        distance = (TextView) view.findViewById(R.id.tvDistance);
        count = (TextView) view.findViewById(R.id.tvCount);
        time = (TextView) view.findViewById(R.id.tvTime);
    }

}
