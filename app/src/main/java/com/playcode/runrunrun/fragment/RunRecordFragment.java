package com.playcode.runrunrun.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.playcode.runrunrun.R;
import com.playcode.runrunrun.activity.ShowDetailActivity;
import com.playcode.runrunrun.adapter.RunRecordAdapter;
import com.playcode.runrunrun.model.RecordsEntity;
import com.playcode.runrunrun.model.UserRecordModel;
import com.playcode.runrunrun.utils.APIUtils;
import com.playcode.runrunrun.utils.RetrofitHelper;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class RunRecordFragment extends Fragment {

    private List<RecordsEntity> records = new ArrayList<>();
    private RunRecordAdapter adapter;

    public RunRecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_run_record, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_record);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RunRecordAdapter(records, getActivity());
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((view, runRecord) -> {
            Intent intent1 = new Intent(getContext(), ShowDetailActivity.class);
            intent1.putExtra("runRecord", runRecord);
            startActivity(intent1);
        });
    }

    /**
     * 获取用户跑步记录
     */
    private void initData() {
//        if (!AccessUtils.isNetworkConnected(getContext())) {
////            Toast.makeText(getActivity(), "网络未连接~", Toast.LENGTH_SHORT).show();
//            ToastUtils.showToast(getActivity(), "网络未连接~");
//            return;
//        }

        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle("正在读取数据...");
        dialog.show();

        SharedPreferences preferences = getContext().getSharedPreferences("UserData", 0);
        String token = preferences.getString("token", "");

        if (TextUtils.isEmpty(token)) {
            dialog.dismiss();
            return;
        }

        RetrofitHelper.getInstance()
                .getService(APIUtils.class)
                .getUserRecords(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserRecordModel>() {
                    @Override
                    public void onCompleted() {
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();
                        Toast.makeText(getActivity(), "网络错误~", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(UserRecordModel userRecordModel) {
                        Log.i("info", "" + userRecordModel.getResultCode());
                        if (userRecordModel.getResultCode() == 0) {
                            records = userRecordModel.getRecords();
                            adapter.updateMyList(records);
                        } else {
                            Toast.makeText(getActivity(), userRecordModel.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }
}
