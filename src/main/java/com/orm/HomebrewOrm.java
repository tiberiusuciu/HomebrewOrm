package com.orm;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class HomebrewOrm {

	private String databasePath;
	
	
	private Map<String, Map<String, Object>> datas;
	public ArrayList<HomebrewOrmTable> listeTables;
	public ArrayList<String> listeTransactions;
	
	
	private final String DATA_DIR_NAME = "/data";
	private final String TABLE_DIR_NAME = "/tables";
	
	private static HomebrewOrm instance = null;	
	
	private HomebrewOrm() {
		String path = loadConfiguration();
		listeTransactions = new ArrayList<String>();
		listeTables = new ArrayList<HomebrewOrmTable>();
		datas = new HashMap<String, Map<String,Object>>();
		if(path != null) {
			this.databasePath = path;
		}
		loadTable("anothertest.json");
		loadTable("another.json");
	}
	
	public static HomebrewOrm getInstance() {
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
	
	public void updateData(String tableName,
						   HashMap<String, String> collumnsToUpdate, 
						   HashMap<String, String> where) {
		String transaction = "update;"+tableName+"{";
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
	
	public void deleteValue(String tableName,
							HashMap<String, String> where) {
		String transaction = "deleteValue;"+tableName+"where{";
		for(Map.Entry<String, String> whereValue : where.entrySet()) {
			transaction+=whereValue.getKey()+":"+whereValue.getValue()+",";
		}
		transaction+="}";
		listeTransactions.add(transaction);
	}
	
	public void removeValue(String tableName,
			HashMap<String, String> where) {
		String transaction = "removeValue;"+tableName+"where{";
		for(Map.Entry<String, String> whereValue : where.entrySet()) {
			transaction+=whereValue.getKey()+":"+whereValue.getValue()+",";
		}
		transaction+="}";
		listeTransactions.add(transaction);
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
	
	private void loadData(String dataToLoad) {
		ObjectMapper mapper = new ObjectMapper();
		File dataDir = new File(this.databasePath + DATA_DIR_NAME);
		String[] dataFiles = dataDir.list();
		for(String file : dataFiles) {
			if(file.equals(dataToLoad)){
				try {
					datas.put(file, mapper.readValue(new File(dataDir + "/" + file), Map.class));
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	@SuppressWarnings("unchecked")
	private void writeTransaction() {
		ObjectMapper mapper = new ObjectMapper();
		File dataDir = new File(this.databasePath + DATA_DIR_NAME);
		for(HomebrewOrmTable homebrewOrmTable : listeTables) {
			String file = homebrewOrmTable.getTableName();
			((Map<String, Object>)datas.get(file + ".json")).put("age", "30");
			ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
			try {
				writer.writeValue(new File(dataDir + "/" + file + ".json"), datas.get(file + ".json"));
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
	
	private void loadTable(String tableToLoad) {
		ObjectMapper mapper = new ObjectMapper();
		File tableDir = new File(this.databasePath + TABLE_DIR_NAME);
		String[] tableFiles = tableDir.list();
		if(tableFiles != null) {
			for(String file : tableFiles) {
				if(file.equals(tableToLoad)) {
					try {
						Map<String,Object> table = mapper.readValue(new File(tableDir + "/" + file), Map.class);
						String tableName = (String) table.get("tableName");
						ArrayList<Map<String, Object>> values = (ArrayList<Map<String, Object>>) table.get("values");
						HomebrewOrmTable homebrewOrmTable = new HomebrewOrmTable();
						homebrewOrmTable.setTableName(tableName);
						
						for(Map<String, Object> value : values) {
							String columnName = (String) value.get("columnName");
							String type =  HomebrewOrmDataTypes.fromString((String)value.get("type"));
							HomebrewOrmTableValue homebrewOrmTableValue = new HomebrewOrmTableValue(columnName, type);
							homebrewOrmTable.addValue(homebrewOrmTableValue);
						}
						listeTables.add(homebrewOrmTable);
						loadData(tableToLoad);
					} catch (JsonParseException e) {
						e.printStackTrace();
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void writeTable(String tableName) {
		ObjectMapper mapper = new ObjectMapper();
		for(HomebrewOrmTable homebrewOrmTable : listeTables) {
			if(homebrewOrmTable.getTableName().equals(tableName)) {
				File tableDir = new File(this.databasePath + TABLE_DIR_NAME);
				try {					
					ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
					writer.writeValue(new File(tableDir + "/" + homebrewOrmTable.getTableName() + ".json"), homebrewOrmTable);
				} catch (JsonGenerationException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String loadConfiguration() {
		ObjectMapper mapper = new ObjectMapper();
		String configurationPath = null;
		try {
			Map<String,Object> configuration = mapper.readValue(new File("./config.json"), Map.class);
			if(configuration != null) {
				configurationPath = (String) configuration.get("database");
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return configurationPath;
	}
}
