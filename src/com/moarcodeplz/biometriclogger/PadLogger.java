package com.moarcodeplz.biometriclogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
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

public class PadLogger extends Activity implements OnTouchListener {

	RelativeLayout rootLayout;
	ArrayList<Button> buttons;
	TextView topView;
	
	private final float topPortion = 0.2f;
	private int hMargin, vMargin, buttonHeight, buttonWidth, screenHeight;
	private int sequenceNumber = 0;
	Sequence curSequence;
	private SoundPool sp;
	private SparseIntArray soundMap;
	
	private final int BACKGROUND_COLOR = Color.LTGRAY;
	private final int BUTTON_UP_COLOR = Color.parseColor("#33B5E5");
	private final int BUTTON_DOWN_COLOR = Color.parseColor("#0099CC");
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
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
        soundMap.put(5, sp.load(this, R.raw.scratch, 1));
        
        findViewById(R.id.rootLayout).getRootView().setBackgroundColor(BACKGROUND_COLOR);
        setNextSequence();
        populateScreen();
        
    }
    
    private void setNextSequence() {
    	curSequence = new Sequence(Globals.SEQUENCES[sequenceNumber]);
    	sequenceNumber = (sequenceNumber + 1) % Globals.SEQUENCES.length;
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
				clickedButton.setBackgroundColor(BUTTON_DOWN_COLOR);
				break;
			case MotionEvent.ACTION_UP:
				clickedButton.setBackgroundColor(BUTTON_UP_COLOR);
				clickedText = (String) clickedButton.getText();
				if (curSequence.checkNext(clickedText.charAt(0))) {
					curSequence.consumeNext();
					if (curSequence.isComplete()) {
						//TODO go to other sequence or something
						playSuccessSound();
						setNextSequence();
					} else {
						playChime();
					}
				} else {
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
		sp.play(soundMap.get(4 + r.nextInt(2)), 1.0f, 1.0f, 100, 0, 1.0f);
		
	}
	
	private void playSuccessSound() {

		Random r = new Random();
		sp.play(soundMap.get(1 + r.nextInt(3)), 1.0f, 1.0f, 100, 0, 1.0f);
		
	}
	
	private void playChime() { 
		
		sp.play(soundMap.get(0), 1.0f, 1.0f, 100, 0, 1.0f);
		
	}
	
}
