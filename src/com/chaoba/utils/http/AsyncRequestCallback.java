package com.chaoba.utils.http;

import com.chaoba.utils.http.HttpRequestProxy.ResponseWrapper;


public interface AsyncRequestCallback<T extends  ResponseWrapper> {

	void onCallback( T t);
}
