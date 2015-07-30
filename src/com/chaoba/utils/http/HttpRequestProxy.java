package com.chaoba.utils.http;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

import com.chaoba.utils.Logger;
import com.google.gson.Gson;

public abstract class HttpRequestProxy {
	protected static final Gson gson = new Gson();
	private AsyncRequestCallback mCallback;

	public void compositeParams(String... params) {
		if (params != null) {
			for (String para : params) {
				Logger.d("compositeParams:" + para);
			}
		}
	}

	public abstract Header[] compositeHead();

	public abstract void request(Header[] header);

	public final void startRequest(AsyncRequestCallback callback,
			String... params) {
		if (params != null) {
			for (String param : params) {
				if (param != null) {
					Logger.d(param);
				}
			}
		}
		compositeParams(params);
		request(compositeHead());
		mCallback = callback;
	}

	/**
	 * stop get request
	 */
	public void stop() {
		mCallback = null;
	}

	public abstract <T extends ResponseWrapper> T convertJsonToObject(
			String json);

	public static class ResponseWrapper {
		public String message;
		public int resCode;
		public int requestCode;

		public boolean succeed() {
			return resCode == HttpStatus.SC_OK;
		}

	}

	/**
	 * The task that send http request in background
	 * 
	 * @author Liyanshun
	 * 
	 */
	public class RequestAsyncTask extends AsyncTask<Object, Integer, Integer> {
		private static final String TAG = "RequestAsyncTask";
		private HttpClient client;
		private ResponseWrapper wrapper;
		private HttpRequestProxy proxy;

		public RequestAsyncTask(HttpClient client, HttpRequestProxy proxy) {
			this.client = client;
			this.proxy = proxy;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			Logger.d("on post");
			if (mCallback != null && result != HttpStatus.SC_UNAUTHORIZED
					&& wrapper != null) {
				Logger.d("on callback");
				mCallback.onCallback(wrapper);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected Integer doInBackground(Object... params) {
			HttpUriRequest request = (HttpUriRequest) params[0];
			Log.d(TAG, request.getURI().toString());

			return sendRequest(request);

		}

		private int sendRequest(HttpUriRequest request) {
			int resCode;
			String json = "";
			try {
				Logger.i(request.toString());

				HttpResponse response = client.execute(request);
				resCode = response.getStatusLine().getStatusCode();
				json = EntityUtils.toString(response.getEntity());
				Log.d(TAG, "result:" + resCode);
				if (resCode == HttpStatus.SC_OK
						|| resCode == HttpStatus.SC_CREATED) {

				} else {
					Log.d(TAG, json);
					json = "{\"message\":\"Netwrok error!\"}";
				}

			} catch (Exception e) {
				resCode = HttpStatus.SC_REQUEST_TIMEOUT;
				json = "{\"message\":\"Netwrok error!\"}";
				e.printStackTrace();
			}
			if (proxy != null) {
				wrapper = proxy.convertJsonToObject(json);
				wrapper.resCode = resCode;
			}
			return resCode;
		}

	}
}
