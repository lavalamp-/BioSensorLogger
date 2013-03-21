package com.moarcodeplz.biometriclogger;

import java.io.File;
import java.util.UUID;

import android.content.Context;
import android.os.Environment;
import android.telephony.TelephonyManager;

public class Globals {
	
	public static final String rootDataDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "data" + File.separator + "BioSensorLogger" + File.separator;
	public static final String nameFile = rootDataDirectory + File.separator + "name";
	public static final String offloadUrl = "http://www.moarcodeplz.com/uploadData.php";
	
	public static String getDeviceID(Context inputContext) {
		
		TelephonyManager tm = (TelephonyManager) inputContext.getSystemService(Context.TELEPHONY_SERVICE);

	    final String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(inputContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

	    return new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode()).toString();
		
	}
	
}
