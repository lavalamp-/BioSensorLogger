package com.moarcodeplz.biometriclogger;

public class BioEvent {

	private String eventName;
	private int eventValue;
	private long eventTime;
	
	public BioEvent(String inputEventName) {
	
		this(inputEventName, -1);
		
	}
	
	public BioEvent(String inputEventName, int inputEventValue) {
	
		eventName = inputEventName;
		eventValue = inputEventValue;
		eventTime = System.currentTimeMillis();
		
	}
	
	public String getEventString() {
	
		return eventName + "," + eventTime + "," + eventValue;
		
	}
	
}
