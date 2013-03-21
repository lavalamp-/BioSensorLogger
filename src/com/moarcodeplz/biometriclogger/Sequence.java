package com.moarcodeplz.biometriclogger;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

public class Sequence {

	private String text;
	private int index;
	
	public Sequence(String inputText) {
		
		text = inputText;
		index = 0;
		
	}
	
	public boolean checkNext(char inputChar) {
		
		if (isComplete()) {
			return true;
		} else if (text.charAt(index) == inputChar){
			return true;
		} else {
			return false;
		}
		
	}
	
	public void consumeNext() {
		
		index++;
		
	}
	
	public boolean isComplete() {
		
		return index >= text.length();
	}
	
	
	public void reset() {
		
		index = 0;
		
	}
	
	public String getText() {
		
		return text;
		
	}
	
	public SpannableString getSpanString() {
		
		SpannableString toReturn = new SpannableString(text);
		
		if (index > 0) {
			toReturn.setSpan(new ForegroundColorSpan(Color.GREEN), 0, Math.min(index, toReturn.length()), 0);
		}
		
		if (index < toReturn.length()) {
			toReturn.setSpan(new ForegroundColorSpan(Color.YELLOW), index, index + 1, 0);
			toReturn.setSpan(new BackgroundColorSpan(Color.BLACK), index, index + 1, 0);
		}
		
		if (index < toReturn.length() - 1) {
			toReturn.setSpan(new ForegroundColorSpan(Color.RED), index + 1, toReturn.length(), 0);
		}
		
		return toReturn;
		
	}
	
}
