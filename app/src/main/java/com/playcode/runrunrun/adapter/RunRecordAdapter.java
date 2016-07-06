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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by czx on 2016/3/30.
 */
public class RunRecordAdapter extends RecyclerView.Adapter<RunRecordAdapter.MyViewHolder> implements
        View.OnClickListener {

    private List<RecordsEntity> mList;
    private Context mContext;

    public RunRecordAdapter(List<RecordsEntity> list, Context context) {
        mList = list;
        mContext = context;
    }

    public void updateMyList(List<RecordsEntity> records) {
        mList = records;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        DecimalFormat df = new DecimalFormat("#0.00");
        String distance = df.format(mList.get(position).getDistance() / 1000);
        SimpleDateFormat sdf = new SimpleDateFormat(mContext.getString(R.string.time_format), Locale.getDefault());
        float time = mList.get(position).getRunTime() * 1000 - 8 * 1000 * 3600;
        String runTime = sdf.format(time);
        df = new DecimalFormat("#0.0");
        String calorie = df.format(mList.get(position).getCalorie());

        holder.distance.setText(distance);
        holder.runTime.setText(runTime);
        holder.calorie.setText(calorie);
        sdf = new SimpleDateFormat(mContext.getString(R.string.month_day_format), Locale.getDefault());
        holder.date.setText(sdf.format(mList.get(position).getDate()));
        holder.itemView.setTag(mList.get(position));
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

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView distance, runTime, calorie, date;

        MyViewHolder(View itemView) {
            super(itemView);
            distance = (TextView) itemView.findViewById(R.id.tv_distance);
            runTime = (TextView) itemView.findViewById(R.id.tv_runtime);
            calorie = (TextView) itemView.findViewById(R.id.tv_calorie);
            date = (TextView) itemView.findViewById(R.id.tv_date);
        }


    }
}
