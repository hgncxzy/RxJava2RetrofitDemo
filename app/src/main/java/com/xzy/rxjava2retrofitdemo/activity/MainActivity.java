package com.xzy.rxjava2retrofitdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.queen.rxjavaretrofitdemo.R;
import com.xzy.rxjava2retrofitdemo.http.ApiService;
import java.util.Objects;

/**
 * RxJava2 + Retrofit demo.
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private TextView resultTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        Button clickBtn = findViewById(R.id.btn_click_me);
        resultTV = findViewById(R.id.tv_result);
        clickBtn.setOnClickListener(v -> getHttpResult());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * 进行网络请求
     */
    private void getHttpResult() {
        ApiService
                .getInstance()
                .getHttpResult()
                .doOnNext(subject -> resultTV.setText(subject.toString()))
                .doOnError(throwable -> {
                    Toast.makeText(MainActivity.this, "结果异常", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, Objects.requireNonNull(throwable.getMessage()));
                })
                .subscribe();
    }
}
