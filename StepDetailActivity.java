package com.thebluecode.trxautophone;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.thebluecode.trxautophone.adapters.StepAdapter;
import com.thebluecode.trxautophone.models.Task;

public class StepDetailActivity extends AppCompatActivity {
    private StepAdapter stepAdapter;
    private Task task;
    private TextView tvTaskName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_detail);

        // Khởi tạo views
        tvTaskName = findViewById(R.id.tv_task_name);
        RecyclerView recyclerView = findViewById(R.id.step_list);
        
        // Khởi tạo adapter TRƯỚC khi sử dụng
        stepAdapter = new StepAdapter(step);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(stepAdapter);

        // Lấy task từ intent
        if (getIntent().hasExtra("task")) {
            task = getIntent().getParcelableExtra("task");
            if (task != null) {
                tvTaskName.setText(task.getName());
                stepAdapter.setSteps(task.getSteps());
            }
        }
    }
}