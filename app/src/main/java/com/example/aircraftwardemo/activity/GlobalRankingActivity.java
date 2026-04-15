package com.example.aircraftwardemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.example.aircraftwardemo.R;
import com.example.aircraftwardemo.adapter.RankingAdapter;
import com.example.aircraftwardemo.data.ScoreRecord;
import com.example.aircraftwardemo.network.*;

import java.util.List;

public class GlobalRankingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RankingAdapter adapter;
    private ScoreRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_ranking);

        // 初始化UI
        recyclerView = findViewById(R.id.ranking_recycler_view);
        progressBar = findViewById(R.id.ranking_progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RankingAdapter();
        recyclerView.setAdapter(adapter);

        // 获取Repository实例
        repository = ScoreRepository.getInstance(this);

        // 加载排行榜数据
        loadGlobalRanking();

        adapter.setOnItemDeleteListener(this::onDeleteItem);
    }

    private void loadGlobalRanking() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // 先显示本地数据
        List<ScoreRecord> localScores = repository.getLocalScores();
        if (!localScores.isEmpty()) {
            adapter.setData(localScores);
            adapter.notifyDataSetChanged();
        }

        // 然后尝试从服务器获取最新数据
        repository.getGlobalScores(new ScoreNetworkManager.RankingCallback() {
            @Override
            public void onSuccess(List<ScoreRecord> scores) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (scores == null || scores.isEmpty()) {
                        showEmptyView("暂无排行榜数据");
                    } else {
                        adapter.setData(scores);
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    // 如果本地有数据，显示本地数据
                    List<ScoreRecord> localScores = repository.getLocalScores();
                    if (!localScores.isEmpty()) {
                        adapter.setData(localScores);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(GlobalRankingActivity.this,
                                "网络错误，显示本地数据", Toast.LENGTH_SHORT).show();
                    } else {
                        showEmptyView("加载失败: " + error);
                    }
                });
            }
        });
    }

    private void showEmptyView(String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    public void onRefreshClick(View view) {
        loadGlobalRanking();
    }

    public void onBackClick(View view) {
        finish();
    }

    private void onDeleteItem(int position, ScoreRecord record) {
        new AlertDialog.Builder(this)
                .setTitle("确定要删除“" + record.getName() + "”的分数记录吗？")
                //.setMessage("确定要删除 " + record.getName() + " 的分数记录吗？")
                .setPositiveButton("是", (dialog, which) -> {
                    // 1. 从本地数据库删除
                    repository.deleteScore(record);

                    // 2. 刷新界面
                    List<ScoreRecord> newList = repository.getLocalScores();
                    adapter.setData(newList);

                    // 3. 空视图处理
                    if (newList.isEmpty()) {
                        showEmptyView("暂无排行榜数据");
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }

                    Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("否", null)  // 点击“否”什么都不做，对话框关闭
                .show();
    }
}