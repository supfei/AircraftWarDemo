package com.example.aircraftwardemo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aircraftwardemo.R;
import com.example.aircraftwardemo.data.ScoreRecord;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<ScoreRecord> scoreList;

    public void setData(List<ScoreRecord> scoreList) {
        this.scoreList = scoreList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (scoreList == null || position >= scoreList.size()) {
            return;
        }

        ScoreRecord record = scoreList.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvName.setText(record.getName());
        holder.tvScore.setText(String.valueOf(record.getScore()));
        holder.tvDate.setText(record.getDate());
    }

    @Override
    public int getItemCount() {
        return scoreList == null ? 0 : scoreList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvScore, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvName = itemView.findViewById(R.id.tv_name);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
