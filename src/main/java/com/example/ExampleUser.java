package com.example;

import com.orm.HomebrewOrmObject;

public class ExampleUser implements HomebrewOrmObject {

	public String firstName;
	public String lastName;
	public int telephoneNumber;
	
	public ExampleUser(String firstName, String lastName, int telephoneNumber){
		this.firstName = firstName;
		this.lastName = lastName;
		this.telephoneNumber = telephoneNumber;
	}
	
	public String toHomebrewOrmData() {
		return "firstName:"+firstName+",lastName:"+lastName+
				",telephoneNumber:"+telephoneNumber;
	}
}
