package com.Branham.facebookcam2;

public class Album {

	private String name;
	private String id;
	
	public Album(String name, String id){
		this.name = name;
		this.id = id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getId(){
		return id;
	}
}
