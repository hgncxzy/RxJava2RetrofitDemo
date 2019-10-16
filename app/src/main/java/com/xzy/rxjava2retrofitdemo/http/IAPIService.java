package com.xzy.rxjava2retrofitdemo.http;

import com.xzy.rxjava2retrofitdemo.entity.HttpResult;
import com.xzy.rxjava2retrofitdemo.entity.Subject;

import io.reactivex.Flowable;
import retrofit2.http.GET;


/**
 * 请求接口。
 */
public interface IAPIService {

    @GET("test/get")
    Flowable<HttpResult<Object>> getTestData();

    @GET("test/get")
    Flowable<Object> getTestData1();
}
