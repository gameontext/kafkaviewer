package org.ozzy.demo;

public class GameOnEvent {
	private long offset;
	private String topic;
	private String key;
	private String value;
	
	public GameOnEvent(){
		
	}
	
	public GameOnEvent(long offset, String topic, String key, String value){
		System.out.println("Building game on event");
		this.offset = offset;
		this.topic = topic;
		this.key=key;
		this.value=value;
	}
	public long getOffset(){
		return offset;
	}
	public String getTopic(){
		return topic;
	}
	public String getKey(){
		return key;
	}
	public String getValue(){
		return value;
	}
}
