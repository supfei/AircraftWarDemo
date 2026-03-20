package com.example.aircraftwardemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
}