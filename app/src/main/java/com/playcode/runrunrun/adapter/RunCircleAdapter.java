package com.playcode.runrunrun.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.playcode.runrunrun.R;
import com.playcode.runrunrun.model.RecordsEntity;
import com.playcode.runrunrun.utils.RxBus;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by anpoz on 2016/3/31.
 */
public class RunCircleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private List<RecordsEntity> mList;
    private Context mContext;

    public RunCircleAdapter(List<RecordsEntity> list, Context context) {
        mList = list;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_item_runcircle, parent, false);
        view.setOnClickListener(this);
        return new RunCircleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RunCircleViewHolder _holder = (RunCircleViewHolder) holder;
        _holder.itemView.setTag(mList.get(position));

        _holder.mTextViewName.setText(mList.get(position).getName());
        _holder.mTextViewAddress.setText(mList.get(position).getAddress());

        long time = (long) (mList.get(position).getRunTime() * 1000) -
                TimeZone.getTimeZone("GMT+8:00").getRawOffset();
        SimpleDateFormat format1 = new SimpleDateFormat(mContext.getString(R.string.time_format), Locale.getDefault());
        _holder.mTextViewTime.setText(format1.format(time));

        float distance = mList.get(position).getDistance() / 1000;
        _holder.mTextViewDistance.setText(String.format(Locale.getDefault(), "%.2fkm", distance));
        _holder.mTextViewCalorie.setText(String.format(Locale.getDefault(), "%.2fKcal", mList.get(position).getCalorie()));

        SimpleDateFormat format2 = new SimpleDateFormat(mContext.getString(R.string.hour_min_format), Locale.getDefault());
        _holder.mTextViewDate.setText(format2.format(mList.get(position).getDate()));
    }

    public void updateDataSet(List<RecordsEntity> list) {
        if (mList == null) {
            mList = list;
        } else {
            mList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void resetData(List<RecordsEntity> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onClick(View v) {
        RxBus.getInstance()
                .post(v.getTag());
    }

    private static class RunCircleViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewName;
        TextView mTextViewAddress;
        TextView mTextViewDistance;
        TextView mTextViewTime;
        TextView mTextViewCalorie;
        TextView mTextViewDate;

        RunCircleViewHolder(View itemView) {
            super(itemView);
            mTextViewName = (TextView) itemView.findViewById(R.id.tvCardName);
            mTextViewAddress = (TextView) itemView.findViewById(R.id.tvCardAddress);
            mTextViewDistance = (TextView) itemView.findViewById(R.id.tvCardDistance);
            mTextViewTime = (TextView) itemView.findViewById(R.id.tvCardTime);
            mTextViewCalorie = (TextView) itemView.findViewById(R.id.tvCardCalorie);
            mTextViewDate = (TextView) itemView.findViewById(R.id.tvCardDate);
        }
    }
}
