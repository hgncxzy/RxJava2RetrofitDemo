package com.xzy.rxjava2retrofitdemo.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.queen.rxjavaretrofitdemo.R;
import com.xzy.rxjava2retrofitdemo.http.HttpMethods;
import com.xzy.rxjava2retrofitdemo.permission.Per;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
        Per.isGrantExternalInternet(this);
        initView();
    }

    private void initView() {
        Button clickBtn = findViewById(R.id.btn_click_me);
        resultTV = findViewById(R.id.tv_result);
        clickBtn.setOnClickListener(v -> getTestData());
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
    private void getTestData() {
        HttpMethods
                .getInstance()
                .getTestData()
                .doOnNext(o -> resultTV.setText(o.toString()))
                .doOnError(throwable -> {
                    Toast.makeText(MainActivity.this, "结果异常", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, Objects.requireNonNull(throwable.getMessage()));
                })
                .subscribe();
    }
}
