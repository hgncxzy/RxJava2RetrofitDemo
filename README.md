# Retrofit 与 Rxjava2 结合的实例

####　RxJava如何与Retrofit结合

[使用Retrofit+RxJava实现网络请求](https://www.jianshu.com/p/092452f287db)

#### 相同格式的 Http 请求数据该如何封装

请求接口： http://yapi.demo.qunar.com/mock/6321/test/get

返回参数：

```json
{
  "msg": "success",
  "code": 0,
  "data": {
    "name": "arthinking",
    "age": 24
  }
}
```

Android 端对请求数据的封装：

```java
package com.xzy.rxjava2retrofitdemo.entity;

import com.google.gson.annotations.SerializedName;

/**
 * 接口返回数据整体封装
 * <p>
 * 后台接口返回 json 格式为：
 * {
 * "msg": "success",
 * "code": 0,
 * "data": {
 * "name": "arthinking",
 * "age": 24
 * }
 * }
 */
public class HttpResult<T> {

    @SerializedName("msg")
    private String msg;
    @SerializedName("code")
    private int code;
    //用来模仿Data
    @SerializedName("data")
    private T subjects;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getSubjects() {
        return subjects;
    }

    public void setSubjects(T subjects) {
        this.subjects = subjects;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                ", subjects=" + subjects +
                '}';
    }
}

```

```java
package com.xzy.rxjava2retrofitdemo.entity;

import com.google.gson.annotations.SerializedName;

/**
 * 接口返回的数据中包含的对象封装
 */
@SuppressWarnings("unused")
public class Subject {
    @SerializedName("name")
    private String name;
    @SerializedName("age")
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

```



#### 相同格式的 Http 请求数据统一进行预处理

正常情况下，开启 Gson 转换就可以对请求数据进行处理

```java
Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
//配置转化库，采用Gson
retrofitBuilder.addConverterFactory(GsonConverterFactory.create());
```

但是对于一些通用的错误码，我们可以进行统一预处理。我们自定义一个 ResponseConvertFactory

```java
package com.xzy.rxjava2retrofitdemo.http;

import com.google.gson.Gson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 *对 http 请求结果进行统一的预处理
 */
public class ResponseConvertFactory extends Converter.Factory{

    /**
     * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    static ResponseConvertFactory create() {
        return create(new Gson());
    }

    /**
     * Create an instance using {@code gson} for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    private static ResponseConvertFactory create(Gson gson) {
        return new ResponseConvertFactory(gson);
    }

    private final Gson gson;

    private ResponseConvertFactory(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        return new GsonResponseBodyConverter<>(gson,type);
    }

}

```

在 responseBodyConverter 方法中，调用了自定义的 GsonResponseBodyConverter 类，进行统一预处理

```java
package com.xzy.rxjava2retrofitdemo.http;

import android.util.Log;

import com.google.gson.Gson;
import com.xzy.rxjava2retrofitdemo.entity.HttpResult;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Gson 转换并包含具体的预处理实现。
 */
class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final Type type;

    GsonResponseBodyConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String response = value.string();
        Log.d("Network", "response>>" + response);
        //httpResult 只解析result字段
        HttpResult httpResult = gson.fromJson(response, HttpResult.class);
        // code 为 0 的时候，是正确的返回，其余为错误的返回
        if (httpResult.getCode() != 0) {
            throw new ApiException(httpResult.getCode());
        }
        return gson.fromJson(response, type);
    }
}

```

在 ApiException 中对错误码进行集中管理即可。

#### 拦截器

可以通过拦截器拦截即将发出的请求及对响应结果做相应处理，典型的处理方式是修改 header。其实我们可能不仅要处理 header，有时也需要添加统一参数，都可以在拦截器内部完成。

[Retrofit Interceptor(拦截器) 拦截请求并做相关处理](https://www.jianshu.com/p/e27ecc092a3f)

上述文章中的通用拦截器可以公用，在这里贴出来，方便查看。

```java
package com.xzy.rxjava2retrofitdemo.interceptor;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 *
 * 通用拦截器
 */
@SuppressWarnings("unused")
public class CommonInterceptor implements Interceptor {
    private static final String TAG = "CommonInterceptor";
    private static Map<String, String> commonParams;

    public synchronized static void setCommonParam(Map<String, String> commonParams) {
        if (commonParams != null) {
            if (CommonInterceptor.commonParams != null) {
                CommonInterceptor.commonParams.clear();
            } else {
                CommonInterceptor.commonParams = new HashMap<>();
            }
            for (String paramKey : commonParams.keySet()) {
                CommonInterceptor.commonParams.put(paramKey, commonParams.get(paramKey));
            }
        }
    }

    public synchronized static void updateOrInsertCommonParam(@NonNull String paramKey,
                                                              @NonNull String paramValue) {
        if (commonParams == null) {
            commonParams = new HashMap<>();
        }
        commonParams.put(paramKey, paramValue);
    }

    /**
     * 重写该方法
     */
    @Override
    public synchronized Response intercept(Chain chain) throws IOException {
        Request request = rebuildRequest(chain.request());
        Response response = chain.proceed(request);
        // 输出返回结果
        try {
            Charset charset;
            charset = Charset.forName("UTF-8");
            ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
            Reader jsonReader = new InputStreamReader(responseBody.byteStream(), charset);
            BufferedReader reader = new BufferedReader(jsonReader);
            StringBuilder sbJson = new StringBuilder();
            String line = reader.readLine();
            do {
                sbJson.append(line);
                line = reader.readLine();
            } while (line != null);
            Log.e(TAG,"response: " + sbJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,e.getMessage(), e);
        }
//        saveCookies(response, request.url().toString());
        return response;
    }


    public static byte[] toByteArray(RequestBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        InputStream inputStream = buffer.inputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] bufferWrite = new byte[4096];
        int n;
        while (-1 != (n = inputStream.read(bufferWrite))) {
            output.write(bufferWrite, 0, n);
        }
        return output.toByteArray();
    }

    /**
     * 对Request的拦截处理
     */
    private Request rebuildRequest(Request request) {
        Request newRequest;
        if ("POST".equals(request.method())) {
            newRequest = rebuildPostRequest(request);
        } else if ("GET".equals(request.method())) {
            newRequest = rebuildGetRequest(request);
        } else {
            newRequest = request;
        }
        Log.e(TAG,"requestUrl: " + newRequest.url().toString());
        return newRequest;
    }

    /**
     * 对post请求添加统一参数
     */
    private Request rebuildPostRequest(Request request) {
//        if (commonParams == null || commonParams.size() == 0) {
//            return request;
//        }
        Map<String, String> signParams = new HashMap<>(); // 假设你的项目需要对参数进行签名
        RequestBody originalRequestBody = request.body();
        assert originalRequestBody != null;
        RequestBody newRequestBody;
        if (originalRequestBody instanceof FormBody) { // 传统表单
            FormBody.Builder builder = new FormBody.Builder();
            FormBody requestBody = (FormBody) request.body();
            int fieldSize = requestBody == null ? 0 : requestBody.size();
            for (int i = 0; i < fieldSize; i++) {
                builder.add(requestBody.name(i), requestBody.value(i));
                signParams.put(requestBody.name(i), requestBody.value(i));
            }
            if (commonParams != null && commonParams.size() > 0) {
                signParams.putAll(commonParams);
                for (String paramKey : commonParams.keySet()) {
                    builder.add(paramKey, Objects.requireNonNull(commonParams.get(paramKey)));
                }
            }
            // ToDo 此处可对参数做签名处理 signParams
            /*
             * String sign = SignUtil.sign(signParams);
             * builder.add("sign", sign);
             */
            newRequestBody = builder.build();
        } else if (originalRequestBody instanceof MultipartBody) { // 文件
            MultipartBody requestBody = (MultipartBody) request.body();
            MultipartBody.Builder multipartBodybuilder = new MultipartBody.Builder();
            if (requestBody != null) {
                for (int i = 0; i < requestBody.size(); i++) {
                    MultipartBody.Part part = requestBody.part(i);
                    multipartBodybuilder.addPart(part);

                    /*
                     上传文件时，请求方法接收的参数类型为RequestBody或MultipartBody.Part参见ApiService文件中uploadFile方法
                     RequestBody作为普通参数载体，封装了普通参数的value; MultipartBody.Part即可作为普通参数载体也可作为文件参数载体
                     当RequestBody作为参数传入时，框架内部仍然会做相关处理，进一步封装成MultipartBody.Part，因此在拦截器内部，
                     拦截的参数都是MultipartBody.Part类型
                     */

                    /*
                     1.若MultipartBody.Part作为文件参数载体传入，则构造MultipartBody.Part实例时，
                     需使用MultipartBody.Part.createFormData(String name, @Nullable String filename, RequestBody body)方法，
                     其中name参数可作为key使用(因为你可能一次上传多个文件，服务端可以此作为区分)且不能为null，
                     body参数封装了包括MimeType在内的文件信息，其实例创建方法为RequestBody.create(final @Nullable MediaType contentType, final File file)
                     MediaType获取方式如下：
                     String fileType = FileUtil.getMimeType(file.getAbsolutePath());
                     MediaType mediaType = MediaType.parse(fileType);

                     2.若MultipartBody.Part作为普通参数载体，建议使用MultipartBody.Part.createFormData(String name, String value)方法创建Part实例
                       name可作为key使用，name不能为null,通过这种方式创建的实例，其RequestBody属性的MediaType为null；当然也可以使用其他方法创建
                     */

                    /*
                      提取非文件参数时,以RequestBody的MediaType为判断依据.
                      此处提取方式简单暴力。默认part实例的RequestBody成员变量的MediaType为null时，part为非文件参数
                      前提是:
                      a.构造RequestBody实例参数时，将MediaType设置为null
                      b.构造MultipartBody.Part实例参数时,推荐使用MultipartBody.Part.createFormData(String name, String value)方法，或使用以下方法
                        b1.MultipartBody.Part.create(RequestBody body)
                        b2.MultipartBody.Part.create(@Nullable Headers headers, RequestBody body)
                        若使用方法b1或b2，则要求

                      备注：
                      您也可根据需求修改RequestBody的MediaType，但尽量保持外部传入参数的MediaType与拦截器内部添加参数的MediaType一致，方便统一处理
                     */

                    MediaType mediaType = part.body().contentType();
                    if (mediaType == null) {
                        String normalParamKey;
                        String normalParamValue;
                        try {
                            normalParamValue = getParamContent(requestBody.part(i).body());
                            Headers headers = part.headers();
                            if (!TextUtils.isEmpty(normalParamValue) && headers != null) {
                                for (String name : headers.names()) {
                                    String headerContent = headers.get(name);
                                    if (!TextUtils.isEmpty(headerContent)) {
                                        String[] normalParamKeyContainer = headerContent.split("name=\"");
                                        if (normalParamKeyContainer.length == 2) {
                                            normalParamKey = normalParamKeyContainer[1].split("\"")[0];
                                            signParams.put(normalParamKey, normalParamValue);
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (commonParams != null && commonParams.size() > 0) {
                signParams.putAll(commonParams);
                for (String paramKey : commonParams.keySet()) {
                    // 两种方式添加公共参数
                    // method 1
                    multipartBodybuilder.addFormDataPart(paramKey,
                            Objects.requireNonNull(commonParams.get(paramKey)));
                    // method 2
//                    MultipartBody.Part part = MultipartBody.Part.createFormData(paramKey, commonParams.get(paramKey));
//                    multipartBodybuilder.addPart(part);
                }
            }
            // ToDo 此处可对参数做签名处理 signParams
            /*
             * String sign = SignUtil.sign(signParams);
             * multipartBodybuilder.addFormDataPart("sign", sign);
             */
            newRequestBody = multipartBodybuilder.build();
        } else {
            try {
                JSONObject jsonObject;
                if (originalRequestBody.contentLength() == 0) {
                    jsonObject = new JSONObject();
                } else {
                    jsonObject = new JSONObject(getParamContent(originalRequestBody));
                }
                if (commonParams != null && commonParams.size() > 0) {
                    for (String commonParamKey : commonParams.keySet()) {
                        jsonObject.put(commonParamKey, commonParams.get(commonParamKey));
                    }
                }
                // ToDo 此处可对参数做签名处理
                /*
                 *
                 * String sign = SignUtil.sign(signParams);
                 * jsonObject.put("sign", sign);
                 */
                newRequestBody = RequestBody.create(originalRequestBody.contentType(), jsonObject.toString());
                Log.e(TAG,getParamContent(newRequestBody));

            } catch (Exception e) {
                newRequestBody = originalRequestBody;
                e.printStackTrace();
            }
        }
//        可根据需求添加或修改header,此处制作示意
//       return request.newBuilder()
//                .addHeader("header1", "header1")
//                .addHeader("header2", "header2")
//                .method(request.method(), newRequestBody)
//                .build();
        return request.newBuilder().method(request.method(), newRequestBody).build();
    }

    /**
     * 获取常规 post 请求参数
     */
    private String getParamContent(RequestBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readUtf8();
    }

    /**
     * 对 get 请求做统一参数处理
     */
    private Request rebuildGetRequest(Request request) {
        if (commonParams == null || commonParams.size() == 0) {
            return request;
        }
        String url = request.url().toString();
        int separatorIndex = url.lastIndexOf("?");
        StringBuilder sb = new StringBuilder(url);
        if (separatorIndex == -1) {
            sb.append("?");
        }
        for (String commonParamKey : commonParams.keySet()) {
            sb.append("&").append(commonParamKey).append("=").append(commonParams.get(commonParamKey));
        }
        Request.Builder requestBuilder = request.newBuilder();
        return requestBuilder.url(sb.toString()).build();
    }
}
```

添加拦截器

```java
       // 手动创建一个OkHttpClient并设置超时时间
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        //启用Log日志
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);
        // 添加通用拦截器
        builder.addInterceptor(new CommonInterceptor());
```

### 参考

1. 使用Retrofit+RxJava实现网络请求 -- https://www.jianshu.com/p/092452f287db
2. Retrofit Interceptor(拦截器) 拦截请求并做相关处理 -- https://www.jianshu.com/p/e27ecc092a3f