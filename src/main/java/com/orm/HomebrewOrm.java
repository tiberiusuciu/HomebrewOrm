package com.orm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomebrewOrm {
	
	public HomebrewOrm instance = null;	
	public ArrayList<String> listeTransactions;
	public ArrayList<HomebrewOrmTable> listeTables;
	
	private HomebrewOrm() {
		listeTransactions = new ArrayList<String>();
		listeTables = new ArrayList<HomebrewOrmTable>();
	}
	
	public HomebrewOrm getInstance() {
		if(instance == null) {
			instance = new HomebrewOrm();
		}
		return instance;
	}
	
	public void insert(HomebrewOrmObject object, String tableName) {
		listeTransactions.add("insert;"+object.toHomebrewOrmData()+";into "+ tableName);
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
	
	public void updateData(HashMap<String, String> collumnsToUpdate, 
						   HashMap<String, String> where) {
		String transaction = "update;{";
		for(Map.Entry<String, String> collumn : collumnsToUpdate.entrySet()) {
			transaction+=collumn.getKey()+":"+collumn.getValue()+",";
		}
		transaction+="};where{";
		for(Map.Entry<String, String> whereValue : where.entrySet()) {
			transaction+=whereValue.getKey()+":"+whereValue.getValue()+",";
		}
		transaction+="}";
		listeTransactions.add(transaction);
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
