package com.xzy.rxjava2retrofitdemo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.queen.rxjavaretrofitdemo.R;
import com.xzy.rxjava2retrofitdemo.http.ApiService;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * RxJava2 + Retrofit demo.
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private TextView resultTV;
    private ProgressDialog dialog;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compositeDisposable = new CompositeDisposable();
        initView();
    }

    private void initView() {
        Button clickBtn = findViewById(R.id.btn_click_me);
        resultTV = findViewById(R.id.tv_result);
        dialog = new ProgressDialog(this);
        dialog.setMessage("加载中");
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
        compositeDisposable.add(Flowable
                .just(1)
                .doOnNext(o -> dialog.show())
                .delay(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(o -> ApiService.getInstance().getHttpResult())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(subject -> resultTV.setText(subject.toString()))
                .doOnError(throwable -> {
                    Toast.makeText(MainActivity.this, "结果异常", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, Objects.requireNonNull(throwable.getMessage()));
                })
                .doOnTerminate(() -> dialog.dismiss())
                .subscribe(o -> {
                }, throwable -> Log.e(TAG, throwable.toString())));
    }
}
