package com.orm;

import java.util.ArrayList;

public class HomebrewOrm {
	
	public HomebrewOrm instance = null;	
	public ArrayList<String> listeTransactions;
	public ArrayList<HomebrewOrmTable> listeTables;
	
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
	
	public boolean createTable (HomebrewOrmTable table) {
		boolean flag = false;
		if(!tableExists(table)){
			listeTables.add(table);
			//todo insert into file
			flag = true;
		}
		return flag;
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
	
	private boolean tableExists(HomebrewOrmTable tableTofind){
		boolean flag = false;
		for(HomebrewOrmTable table: listeTables){
			if(table.getTableName().equals(tableTofind.getTableName())){
				flag = true;
				break;
			}
		}
		return flag;
	}
}
