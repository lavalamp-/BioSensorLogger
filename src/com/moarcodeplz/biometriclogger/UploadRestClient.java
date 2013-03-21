package com.moarcodeplz.biometriclogger;

import java.io.File;
import java.io.FileNotFoundException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class UploadRestClient {
	
	private final AsyncHttpClient client = new AsyncHttpClient();
	private boolean isOffloadRunning = false;
	
	private JsonHttpResponseHandler jsonHandler = new JsonHttpResponseHandler() {
		
		@Override 
		public void onSuccess(JSONObject response) {
			
			handleResponse(response);
			
		}
		
		@Override
		public void onFailure(Throwable e, JSONArray errorResponse) {
			
			Log.e("UploadRestClient", "onFailure thrown in Json handler for respone: " + e.getMessage());
			
		}
		
		@Override
		public void onFailure(Throwable e, JSONObject errorResponse) {
			
			Log.e("UploadRestClient", "onFailure thrown in Json handler for respone: " + e.getMessage());
			
		}
		
	};
	
	private AsyncHttpResponseHandler textHandler = new AsyncHttpResponseHandler() {
		
		@Override
		public void onSuccess(String response) {
			
			Log.d("UploadRestClient", "Successful http post!");
			handleResponse(response);
			
		}
		
		@Override
		public void onFailure(Throwable e, String response) {
			
			Log.e("UploadRestClient", "Failed http post!");
			handleResponse(response);
			
		}
		
	};
	
	private void handleResponse(JSONObject response) {
		
		try {
			String serverResponse = response.getString("RESPONSE");
			String fileName = response.getString("FILE_NAME");
			
			if (serverResponse.equalsIgnoreCase("true")) {
				
				File wasUploaded = new File(fileName);
				boolean wasDeleted = wasUploaded.delete();
				
				if (!wasDeleted) {
					Log.e("UploadRestClient", "Unable to delete file " + fileName);
					isOffloadRunning = false;
				} else {
					PadLogger.filesToOffload.remove(0);
					offloadNextFile();
				}
				
			} else {
				isOffloadRunning = false;
				Log.e("UploadRestClient", "The server response was fail: " + response.getString("cause"));
			}
			
		} catch (Exception ex) {
			//TODO proper error handling
			isOffloadRunning = false;
			Log.d("UploadRestClient", "Exception thrown while parsing JSONObject response");
		}
		
	}
	
	private void handleResponse(String response) {
		
		System.out.println(response);
		
	}
	
	private RequestParams getBasicParams() {
		
		RequestParams toReturn = new RequestParams();
		
		return toReturn;
		
	}
	
	public void startOffloadIfNotRunning() {
	
		if (!isOffloadRunning) {
			offloadNextFile();
		}
		
	}
	
	private void offloadNextFile() {
		
		if (PadLogger.filesToOffload.size() > 0) {
			
			try {
				RequestParams toSend = getBasicParams();
				toSend.put("FILE_NAME", PadLogger.filesToOffload.get(0));
				toSend.put("FILE", new File(PadLogger.filesToOffload.get(0)));
				isOffloadRunning = true;
				client.post(Globals.offloadUrl, toSend, jsonHandler);
			} catch (FileNotFoundException e) {
				Log.e("UploadRestClient", "File not found exception thrown: " + e.getMessage());
			}
				
		} else {
			isOffloadRunning = false;
		}
		
	}
	
}
