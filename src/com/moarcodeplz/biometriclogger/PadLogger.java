package com.moarcodeplz.biometriclogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PadLogger extends Activity implements OnTouchListener, SensorEventListener {

	private RelativeLayout rootLayout;
	private ArrayList<Button> buttons;
	private TextView topView;
	
	private final float topPortion = 0.2f;
	private int hMargin, vMargin, buttonHeight, buttonWidth, screenHeight;
	private Sequence curSequence;
	private SoundPool sp;
	private SparseIntArray soundMap;
	private ArrayList<BioEvent> loggedEvents;
	private SparseIntArray sensorMap;
	private StringBuilder sensorBuilder;
	private UploadRestClient httpClient;
	
	private final int BACKGROUND_COLOR = Color.LTGRAY;
	private final int BUTTON_UP_COLOR = Color.parseColor("#33B5E5");
	private final int BUTTON_DOWN_COLOR = Color.parseColor("#0099CC");
	private final int LOG_SPEED = SensorManager.SENSOR_DELAY_FASTEST;
	
	public static ArrayList<String> filesToOffload;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        PadLogger.filesToOffload = new ArrayList<String>();
        initializeOffloadList();
        
    	//Remove title bar
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	//Remove notification bar
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	
        setContentView(R.layout.activity_pad_logger);
        
        soundMap = new SparseIntArray();
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 100);
        soundMap.put(0, sp.load(this, R.raw.chime, 1));
        soundMap.put(1, sp.load(this, R.raw.correct, 1));
        soundMap.put(2, sp.load(this, R.raw.woohoo, 1));
        soundMap.put(3, sp.load(this, R.raw.tada, 1));
        soundMap.put(4, sp.load(this, R.raw.no, 1));
        loggedEvents = new ArrayList<BioEvent>();
        loggedEvents.add(new BioEvent("ONCREATE"));
        intializeSensorMap();
        sensorBuilder = new StringBuilder();
        httpClient = new UploadRestClient();
        
        findViewById(R.id.rootLayout).getRootView().setBackgroundColor(BACKGROUND_COLOR);
        setNextSequence();
        populateScreen();
        registerSensorListeners();
        
    }
    
    @Override
    public void onStop() {
    	
    	unregisterSensorListeners();
    	android.os.Process.killProcess(android.os.Process.myPid());
    	
    }
    
    private void initializeOffloadList() {
    
    	//TODO fill this out
    	
    }
    
    private void registerSensorListeners() {
		
		SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		for (Sensor curSensor : sm.getSensorList(Sensor.TYPE_ALL)) {
			sm.registerListener(this, curSensor, LOG_SPEED);
		}
    	
    }
    
    private void unregisterSensorListeners() {

		SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		sm.unregisterListener(this);
		
    }
    
    private void intializeSensorMap() {
		
		ArrayList<Sensor> sensors = new ArrayList<Sensor>(getAllSensors());
		Collections.sort(sensors, new SensorComparator());
		sensorMap = new SparseIntArray(sensors.size());
		for (int i=0; i<sensors.size(); i++) {
			sensorMap.put(sensors.get(i).getName().hashCode(), i);
		}
    	
    }
	
	private List<Sensor> getAllSensors() {
		
		SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		return sm.getSensorList(Sensor.TYPE_ALL);
		
	}
    
    private void setNextSequence() {
    	curSequence = Sequence.getRandomSequenceOfLength(9);
    	loggedEvents.add(new BioEvent("NEW_SEQUENCE " + curSequence.getText()));
    }
    
    @SuppressWarnings("deprecation")
	private void populateScreen() {
    	
    	Display display = getWindowManager().getDefaultDisplay();
    	hMargin = 5;
    	vMargin = 5;
    	screenHeight = display.getHeight();
    	buttonWidth = (display.getWidth() - (4*hMargin)) / 3;
    	buttonHeight = (int)(display.getHeight() - (topPortion * display.getHeight()) - (5 * vMargin)) / 4;
    	buttons = new ArrayList<Button>();
    	rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
    	
    	topView = new TextView(this);
    	topView.setText(curSequence.getSpanString());
    	topView.setGravity(Gravity.CENTER);
    	topView.setTextSize(45.0f);
    	RelativeLayout.LayoutParams topParams = new RelativeLayout.LayoutParams(display.getWidth(), (int)(topPortion * display.getHeight()));
    	topParams.leftMargin = 0;
    	topParams.topMargin = 0;
    	rootLayout.addView(topView, topParams);
    	
    	for (int i=0; i<10; i++) {
    		Button newButton = new Button(this);
    		newButton.setBackgroundColor(BUTTON_UP_COLOR);
    		newButton.setOnTouchListener(this);
    		newButton.setText("" + i);
    		buttons.add(newButton);
    		rootLayout.addView(newButton, getParamsForButton(i));
    	}
    	
    }
    
    private RelativeLayout.LayoutParams getParamsForButton(int inputButtonNumber) {
    	
    	RelativeLayout.LayoutParams toReturn = new RelativeLayout.LayoutParams(buttonWidth, buttonHeight);
    	int numMargins;
    	
    	if (inputButtonNumber >= 1 && inputButtonNumber <= 9) {
    		numMargins = (inputButtonNumber - 1) % 3;
    		toReturn.leftMargin = ((numMargins + 1) * hMargin) + (numMargins * buttonWidth);
    		numMargins = (inputButtonNumber - 1) / 3;
    		toReturn.topMargin = (int)(topPortion * screenHeight) + ((numMargins + 1) * vMargin) + (numMargins * buttonHeight);
    	} else if (inputButtonNumber == 0) {
    		toReturn.leftMargin = 2 * hMargin + buttonWidth;
    		toReturn.topMargin = (int)(topPortion * screenHeight) + (4 * vMargin) + (3*buttonHeight);
    	} else {
    		//This should never happen
    	}
    	
    	return toReturn;
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_pad_logger, menu);
        return true;
    }

	public boolean onTouch(View v, MotionEvent motion) {

		Button clickedButton = (Button) v;
		String clickedText;
		
		switch(motion.getAction()) {
			case MotionEvent.ACTION_DOWN:
				loggedEvents.add(new BioEvent("BUTTON_DOWN " + clickedButton.getText().toString()));
				clickedButton.setBackgroundColor(BUTTON_DOWN_COLOR);
				break;
			case MotionEvent.ACTION_UP:
				clickedButton.setBackgroundColor(BUTTON_UP_COLOR);
				clickedText = (String) clickedButton.getText();
				loggedEvents.add(new BioEvent("BUTTON_UP " + clickedButton.getText().toString()));
				if (curSequence.checkNext(clickedText.charAt(0))) {
					curSequence.consumeNext();
					if (curSequence.isComplete()) {
						loggedEvents.add(new BioEvent("SEQUENCE_COMPLETE"));
						createLogFile();
						if (StorageHelper.isNetworkAvailable(this)) {
							Log.d("PadLogger", "Networking is available!");
							httpClient.startOffloadIfNotRunning();
						} else {
							Log.d("PadLogger", "Networking is NOT available!");
						}
						playSuccessSound();
						setNextSequence();
					} else {
						playChime();
					}
				} else {
					loggedEvents.add(new BioEvent("SEQUENCE_FAIL"));
					curSequence.reset();
					playFailSound();
				}
				topView.setText(curSequence.getSpanString());
				break;
		}
		
		return true;
		
	}
	
	private void playFailSound() {
		
		Random r = new Random();
		sp.play(soundMap.get(4 + r.nextInt(1)), 1.0f, 1.0f, 100, 0, 1.0f);
		
	}
	
	private void playSuccessSound() {

		Random r = new Random();
		sp.play(soundMap.get(1 + r.nextInt(3)), 1.0f, 1.0f, 100, 0, 1.0f);
		
	}
	
	private void playChime() { 
		
		sp.play(soundMap.get(0), 1.0f, 1.0f, 100, 0, 1.0f);
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		sensorBuilder.append(sensorMap.get(event.sensor.hashCode()) + "," + System.currentTimeMillis() + ",");
		for (int i=0; i<6; i++) {
			if (i < event.values.length) {
				sensorBuilder.append(event.values[i]);
			}
			sensorBuilder.append(",");
		}
		//Log.d("LoggerService", "ZOMG PL0Z HALP MAI - 013");
		sensorBuilder.deleteCharAt(sensorBuilder.length() - 1).append("\n"); //Remove trailing comma
		
	}
	
	private void createLogFile() {
	
		String deviceID = Globals.getDeviceID(this);
		String logFileName = deviceID + "_" + System.currentTimeMillis();
		String logFilePath = Globals.rootDataDirectory + File.separator + logFileName;
		StringBuilder logContents = new StringBuilder();
		List<Sensor> sensors = getAllSensors();
		Sensor curSensor;
		
		logContents.append("[DEVICE_INFO]\n");
		logContents.append("DEVICE_ID:" + deviceID + "\n");
		logContents.append("DEVICE_MODEL:" + android.os.Build.MODEL);
		logContents.append("[/DEVICE_INFO]\n[SENSOR_INFO]\n");
		
		for (int i=0; i<sensors.size(); i++) {
			curSensor = sensors.get(i);
			logContents.append(sensorMap.get(curSensor.hashCode()) + "," + curSensor.getName() + "," + curSensor.getType() + "," + curSensor.getVendor() + "," + curSensor.getResolution() + "\n");
		}
		
		logContents.append("[/SENSOR_INFO]\n[SENSOR_READINGS]\n");
		logContents.append(sensorBuilder.toString());
		
		logContents.append("[/SENSOR_READINGS]\n[APP_EVENTS]\n");
		
		for (int i=0; i<loggedEvents.size(); i++) {
		
			logContents.append(loggedEvents.get(i).getEventString() + "\n");
			
		}
		
		logContents.append("[/APP_EVENTS]");
		
		StorageHelper.writeStringToFile(logFilePath, logContents.toString());
		PadLogger.filesToOffload.add(logFilePath);
		
		sensorBuilder = new StringBuilder();
		loggedEvents = new ArrayList<BioEvent>();
		
	}
	
}
