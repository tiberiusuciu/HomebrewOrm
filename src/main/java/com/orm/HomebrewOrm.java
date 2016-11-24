package com.orm;

import java.util.ArrayList;

public class HomebrewOrm {
	
	public HomebrewOrm instance = null;	
	public ArrayList<String> listeTransactions;
	
	
	private HomebrewOrm() {
		
	}
	
	public HomebrewOrm getInstance() {
		if(instance == null) {
			instance = new HomebrewOrm();
		}
		return instance;
	}
	
	public void insert() {
		
	}
	
	public void createTable() {
		
	}
	
	public void updateData() {
		
	}
	
	public void updateTable() {
		
	}
	
	public void deleteValue() {
		
	}
	
	public void removeValue() {
		
	}
	
	public void removeTable() {
		
	}
	
	public void commit() {
		
	}
	
	public void select() {
		
	}
	
	public void oneToOne() {
		
	}
	
	public void oneToMany() {
		
	}
	
	public void manyToMany() {
		
	}
	
	private void loadData() {
		
	}
	
	private void writeData() {
		
	}
	
}
