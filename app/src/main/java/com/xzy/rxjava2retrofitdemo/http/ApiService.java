package com.xzy.rxjava2retrofitdemo.http;

import android.annotation.SuppressLint;

import com.xzy.rxjava2retrofitdemo.entity.HttpResult;
import com.xzy.rxjava2retrofitdemo.entity.Subject;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


/**
 * Created by liukun on 16/3/9.
 */
public class ApiService {
    private static final String TAG = "ApiService";
    private static final String BASE_URL = "http://yapi.demo.qunar.com/mock/6321/";
    private static final int DEFAULT_TIMEOUT = 5;
    private IApiService service;

    /**
     * 构造方法私有
     */
    private ApiService() {
        // 手动创建一个OkHttpClient并设置超时时间
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        Retrofit retrofit = new Retrofit
                .Builder()
                .client(builder.build())
                // 对http请求结果进行统一的预处理 GosnResponseBodyConvert
                .addConverterFactory(ResponseConvertFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        service = retrofit.create(IApiService.class);
    }

    /**
     * 在访问 ApiService 时创建单例
     */
    private static class SingletonHolder {
        private static final ApiService INSTANCE = new ApiService();
    }

    /**
     * 获取单例
     *
     * @return ApiService
     */
    public static ApiService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 用于获取测试接口的数据
     */
    @SuppressLint("CheckResult")
    public Flowable<Subject> getHttpResult() {
        return service
                .getHttpResult()
                .map(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 用来统一处理 Http 的 resultCode,并将 HttpResult 的 Data 部分剥离出来返回给subscriber
     *
     * @param <T> Subscriber真正需要的数据类型，也就是Data部分的数据类型
     */
    private class HttpResultFunc<T> implements Function<HttpResult<T>, T> {
        @Override
        public T apply(HttpResult<T> tHttpResult) {
            if (tHttpResult == null) {
                throw new ApiException(100);
            }
            return tHttpResult.getSubjects();
        }
    }

}