package com.moarcodeplz.biometriclogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class StorageHelper {
	
	public static boolean appendStringToFile(String fileUri, String toWrite) {
		
		try {
			FileWriter f = new FileWriter(fileUri, true);
			f.write(toWrite);
			f.flush();
			f.close();
			return true;
		} catch (Exception ex) {
			Log.e("StorageHelper", "StorageHelper appendStringToFile failed.");
			return false;
		}
		
	}

	public static boolean isNetworkAvailable(Context inputContext) {

		ConnectivityManager connManager = (ConnectivityManager) inputContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getActiveNetworkInfo();

		if (mWifi != null && mWifi.isConnectedOrConnecting()) {
			return true;
		} else {
			return false;
		}
		
	}
	
	public static boolean doesFileExist(String inputUri) {
		
		return new File(inputUri).exists();
		
	}

	public static String getStringFromFile(String fileUri) {
		
		//TODO close stream properly
		
		try {
			FileInputStream stream = new FileInputStream(new File(fileUri));
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			stream.close();
			Log.e("StorageHelper", "StorageHelper getStringFromFile succeeded.");
			return Charset.defaultCharset().decode(bb).toString();
		} catch (Exception ex) {
			Log.e("StorageHelper", "StorageHelper writeDefaultPreferenceFile failed.");
			return null;
		} 
	
	}
	
	public static boolean writeStringToFile(String fileUri, String toWrite) {
		
		try {
			File curFile = new File(fileUri);
			if (!curFile.exists()) {
				curFile.getParentFile().mkdirs();
				curFile.createNewFile();
			}
			OutputStreamWriter ows = new OutputStreamWriter(new FileOutputStream(curFile));
			ows.write(toWrite);
			ows.flush();
			ows.close();
			boolean writeSuccess = true;
			Log.d("StorageHelper", "StorageHelper writeStringToFile succeeded.");
			return writeSuccess;
		} catch (Exception ex) {
			Log.d("StorageHelper", "StorageHelper writeStringToFile failed.");
			return false;
		}
	}

}
