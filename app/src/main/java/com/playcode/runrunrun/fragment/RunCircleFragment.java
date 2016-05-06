package com.playcode.runrunrun.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.playcode.runrunrun.R;
import com.playcode.runrunrun.activity.ShowDetailActivity;
import com.playcode.runrunrun.adapter.RunCircleAdapter;
import com.playcode.runrunrun.model.RecordsEntity;
import com.playcode.runrunrun.model.RunCircleResultModel;
import com.playcode.runrunrun.utils.APIUtils;
import com.playcode.runrunrun.utils.AccessUtils;
import com.playcode.runrunrun.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class RunCircleFragment extends Fragment implements SwipyRefreshLayout.OnRefreshListener {
    private SwipyRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RunCircleAdapter mRunCircleAdapter;

    private List<RecordsEntity> list;
    private int lastItemId = 0;

    private Retrofit retrofit;


    public RunCircleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_run_circle, container, false);

        initView(rootView);
//        initRunCircle();

        return rootView;
    }

    private void initView(View rootView) {
        mSwipeRefreshLayout = (SwipyRefreshLayout) rootView.findViewById(R.id.srl_runCircle);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_runCircle);

        //初始化retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("http://codeczx.duapp.com/FitServer/")
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm:ss.S")
                                .create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRunCircleAdapter = new RunCircleAdapter(new ArrayList<>(), getContext());
        mRunCircleAdapter.setOnItemClickListener((v, recordsEntity) -> {
            Intent intent1 = new Intent(getActivity(), ShowDetailActivity.class);
            intent1.putExtra("runRecord", recordsEntity);
            startActivity(intent1);
        });
        mRecyclerView.setAdapter(mRunCircleAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        initRunCircle();
    }

    private void initRunCircle() {
        if (!AccessUtils.isNetworkConnected(getContext())) {
//            Toast.makeText(getActivity(), "网络未连接~", Toast.LENGTH_SHORT).show();
            ToastUtils.showToast(getActivity(), "网络未连接~");
            return;
        }
        SharedPreferences preferences = getActivity().getSharedPreferences("UserData", 0);
        String token = preferences.getString("token", "");
        if (TextUtils.isEmpty(token)) {
            Toast.makeText(getActivity(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        Observable.timer(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });

        refreshData(token);
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        if (direction == SwipyRefreshLayoutDirection.TOP) {//下拉刷新
            Log.d("onfresh", "下拉");
            initRunCircle();
        } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {//上拉加载更多
            Log.d("onfresh", "上拉");
            SharedPreferences preferences = getActivity().getSharedPreferences("UserData", 0);
            String token = preferences.getString("token", "");
            if (TextUtils.isEmpty(token)) {
                Toast.makeText(getActivity(), "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            loadMore(token);
        }
    }

    private void refreshData(String token) {

        retrofit.create(APIUtils.class)
                .getMaxId()
                .subscribeOn(Schedulers.io())
                .flatMap(maxIdModel -> retrofit.create(APIUtils.class)
                        .getRecordsById(maxIdModel.getId(), token))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RunCircleResultModel>() {
                    @Override
                    public void onCompleted() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Snackbar.make(mRecyclerView, "网络错误，刷新失败...", Snackbar.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(RunCircleResultModel runCircleResultModel) {
                        if (runCircleResultModel.getResultCode() == 0) {
                            list = runCircleResultModel.getRecords();
                            mRunCircleAdapter.resetData(list);
                            Log.d("initRunCircle", "刷新运动圈数据" + list.size() + "条");
                            mSwipeRefreshLayout.setRefreshing(false);
                            lastItemId = runCircleResultModel.getRecords()
                                    .get(runCircleResultModel.getRecords().size() - 1)
                                    .getId();
                            Log.d("initRunCircle", "lastItemId:" + lastItemId);
                        } else {
                            Snackbar.make(mRecyclerView, "刷新失败，" + runCircleResultModel.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadMore(String token) {
        retrofit.create(APIUtils.class)
                .getRecordsById(lastItemId - 1, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RunCircleResultModel>() {
                    @Override
                    public void onCompleted() {
                        Log.d("initRunCircle", "加载运动圈数据onComplete");
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Snackbar.make(mRecyclerView, "网络错误，加载失败...", Snackbar.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(RunCircleResultModel runCircleResultModel) {
                        if (runCircleResultModel.getResultCode() == 0) {
                            if (runCircleResultModel.getRecords().size() > 0) {
                                mRunCircleAdapter.updateDataSet(runCircleResultModel.getRecords());
                                lastItemId = runCircleResultModel.getRecords()
                                        .get(runCircleResultModel.getRecords().size() - 1)
                                        .getId();
                                Log.d("initRunCircle", "lastItemId:" + lastItemId);
                            } else {
                                Snackbar.make(mRecyclerView, "没有更多了", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            Snackbar.make(mRecyclerView, "加载失败，" + runCircleResultModel.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
