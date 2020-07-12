package com.example.libnetwork.callback;

import com.example.libnetwork.api.ApiResponse;

public abstract class JsonCallback<T> {

    public void onSuccess(ApiResponse<T> response) {

    }

    public void onError(ApiResponse<T> response) {

    }

    // cache失败不向外通知，只有onCacheSuccess才往外回调
    public void onCacheSuccess(ApiResponse<T> response) {

    }
}
