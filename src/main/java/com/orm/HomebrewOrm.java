package com.orm;

public class HomebrewOrm {
	
	public HomebrewOrm instance = null;
	
	private HomebrewOrm() {
		
	}
	
	public HomebrewOrm getInstance() {
		if(instance == null) {
			instance = new HomebrewOrm();
		}
		return instance;
	}
	
}
