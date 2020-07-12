package com.example.libnetwork.request;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.example.libnetwork.api.ApiResponse;
import com.example.libnetwork.cache.CacheManager;
import com.example.libnetwork.callback.JsonCallback;
import com.example.libnetwork.api.ApiService;
import com.example.libnetwork.utils.Convert;
import com.example.libnetwork.utils.UrlCreator;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/*
* 两个泛型：
*       T：Response的实体类型
*       R：Request的子类
* */
public abstract class Request<T, R extends Request> {

    protected String mUrl;
    protected HashMap<String, String> headers = new HashMap<>();
    protected HashMap<String, Object> params = new HashMap<>();

    // 设置缓存类型
    public static final int CACHE_ONLY = 1;// 只访问缓存，即便不存在，也不进行网络请求
    public static final int CACHE_FIRST = 2;// 先访问缓存，在访问网络，成功后缓存到本地
    public static final int NET_ONLY = 3;// 只访问网络接口，不做任何存储
    public static final int NET_CHCHE = 4;// 先访问网络，成功后缓存到本地
    private String cacheKey;
    private Type mType;
    private Class mClazz;
    private int mCacheStrategy;

    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_ONLY, NET_CHCHE})
    public @interface  CacheStrategy { }

    public Request(String url){
        // user/list（不需服务器域名）
        mUrl = url;
    }

    //
    public R addHeader(String key, String value){
        headers.put(key, value);
        return (R) this;
    }

    public R addParam(String key, Object value){
        if (value == null) {
            return (R) this;
        }
        // 只能是八种基本类型
        try {
            if (value.getClass() == String.class) {
                params.put(key, value);
            } else {
                Field field = value.getClass().getField("TYPE");
                Class clazz = (Class) field.get(null);
                if (clazz.isPrimitive()) {
                    params.put(key, value);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) this;
    }

    public R cacheKey(String key){
        this.cacheKey = key;
        return (R) this;
    }

    // Java1.5之后，new 抽象类和接口的默认实现为匿名内部继承类，所以不会受到泛型擦除的影响
    // 所以运行时也可以获取
    public ApiResponse<T> execute(){
        if (mCacheStrategy == CACHE_ONLY){
            return readCache();
        }

        try {
            Response response = getCall().execute();
            ApiResponse<T> result = parseResponse(response, null);
            return  result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // final参数：给了这个参数，你只能用这个参数的值，你不能修改它，对于基本类型和引用类型是一样的
    // 匿名内部类如果想直接使用入参一定要定义成final: public void test(final long abc){
    //                          FatherInterface fa = new FatherInterface(){
    //                              @Override
    //                              public void father() {
    //                              System.out.println(abc);
    //                          }};
    //                      }
    @SuppressLint("RestrictedApi")
    public void execute(final JsonCallback callback){
        if (mCacheStrategy != NET_ONLY){
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ApiResponse<T> response = readCache();
                    if (callback != null){
                        callback.onCacheSuccess(response);
                    }
                }
            });
        }
        if (mCacheStrategy != CACHE_ONLY){
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ApiResponse<T> response = new ApiResponse<>();
                    response.message = e.getMessage();
                    callback.onError(response);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    // 解析response时，需要借助callback获取实例的实际类型
                    ApiResponse<T> apiResponse = parseResponse(response, callback);
                    if (apiResponse.success){
                        callback.onError(apiResponse);
                    }else {
                        callback.onSuccess(apiResponse);
                    }
                }
            });
        }
    }

    private ApiResponse<T> readCache() {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> result = new ApiResponse<>();
        result.status = 304;
        result.message = "缓存获取成功";
        result.body = (T) cache;
        result.success = true;
        return result;
    }

    private ApiResponse<T> parseResponse(Response response, JsonCallback<T> callback) {
        String message = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Convert convert = ApiService.sConvert;
        try {
            String content = response.body().string();
            if (success){
                if (callback != null){
                    // 泛型类的真正类型
                    ParameterizedType parameterizedType = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = parameterizedType.getActualTypeArguments()[0];
                    result.body = (T) convert.convert(content, argument);
                }else if (mType != null){
                    result.body = (T) convert.convert(content, mType);
                }else if (mClazz != null){
                    result.body = (T) convert.convert(content, mClazz);
                }else {
                    Log.e("Request", "parseResponse: 无法解析!");
                }
            }else {
                message = content;
            }
        } catch (IOException e) {
            message = e.getMessage();
            success = false;
            e.printStackTrace();
        }
        result.success = success;
        result.status = status;
        result.message = message;

        // 解析返回值后，判断缓存
        if (mCacheStrategy != NET_ONLY && result.success && result.body != null && result.body instanceof Serializable){
            savaCache(result.body);
        }

        return result;
    }

    private void savaCache(T body) {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() :cacheKey;
        CacheManager.save(key, body);
    }



    private String generateCacheKey() {
        cacheKey = UrlCreator.createUrlFromParams(mUrl, params);
        return cacheKey;
    }

    public R cacheStrategy(@CacheStrategy int cacheStrategy){
        mCacheStrategy = cacheStrategy;
        return (R) this;
    }

    public R responseType(Type type){
        mType = type;
        return (R) this;
    }

    public R responseType(Class clazz){
        mClazz = clazz;
        return (R) this;
    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        okhttp3.Request request = generateRequest(builder);
        Call call = ApiService.okHttpClient.newCall(request);
        return call;
    }

    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> stringStringEntry : headers.entrySet()) {
            builder.addHeader(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
    }

    @NonNull
    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request<T, R>) super.clone();
    }
}
