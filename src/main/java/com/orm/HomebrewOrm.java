package com.orm;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.ExampleUser;
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
		listeTransactions.add("insert;"+object.toHomebrewOrmData()+";"+ tableName);
	}
	
	public boolean createTable (HomebrewOrmTable table) {
		boolean flag = false;
		if(!tableExists(table.getTableName())){
			listeTables.add(table);
			writeTable(table.getTableName());
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
		transaction+="};{";
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
		String transaction = "deleteValue;"+tableName+";{";
		for(Map.Entry<String, String> whereValue : where.entrySet()) {
			transaction+=whereValue.getKey()+":"+whereValue.getValue()+",";
		}
		transaction+="}";
		listeTransactions.add(transaction);
	}
	
	public void removeValue(String tableName,
			HashMap<String, String> where) {
		String transaction = "removeValue;"+tableName+";{";
		for(Map.Entry<String, String> whereValue : where.entrySet()) {
			transaction+=whereValue.getKey()+":"+whereValue.getValue()+",";
		}
		transaction+="}";
		listeTransactions.add(transaction);
	}
	
	public void removeTable() {
		
	}
	
	public void commit() {
		System.out.println(verifyTransactions());
	}

	private boolean verifyTransactions() {
		boolean flag = true;
		for (String transaction: listeTransactions) {
			String[] transactionInfos = transaction.split(";");
			switch (transactionInfos[0]) {
			case "insert":
				if(!verifyInsertion(transactionInfos)){
					return false;
				}
				break;
			case "update":
				break;
			case "deleteValue":
				break;
			case "removeValue":
				break;
			default:
				break;
			}
		}
		return flag;
	}

	private boolean verifyInsertion(String[] transactionInfos) {
		boolean flag = true;
		if(tableExists(transactionInfos[2])){
			//find table with tableName
			HomebrewOrmTable table = findTable(transactionInfos[2]);
			//seperate all collumns
			String[] valueToInsert = transactionInfos[1].split(",");
			for(int i = 0; i < valueToInsert.length;i++){
				//seperate collums and values
				String[] columnValue = valueToInsert[i].split(":");
				//verify if all tables are legit
				if(!columnValue[0].equals(table.getValues().get(i).getColumnName())){
					flag = false;
					break;
				}
				else{
					if(!verifyType(table.getValues().get(i).getType(), columnValue[1])){
						flag = false;
						break;
					}
				}
			}
		}
		else{
			flag = false;
		}
		return flag;
	}

	private boolean verifyType(String tableType, String value){
		boolean flag = true;
		if(tableType.equals(HomebrewOrmDataTypes.booleanType.value)){
			if(!Boolean.parseBoolean(value)){
				flag = false;
			}
		}
		else if(tableType.equals(HomebrewOrmDataTypes.charType.value)){
			if(value.length() != 1){
				flag = false;
			}
		}
		else if(tableType.equals(HomebrewOrmDataTypes.integerType.value)){
			try { 
		        Integer.parseInt(value); 
		    } catch(NumberFormatException e) { 
		        return false; 
		    } catch(NullPointerException e) {
		        return false;
		    }
		}
		else if(tableType.equals(HomebrewOrmDataTypes.doubleType.value)){
			try { 
		        Double.parseDouble(value); 
		    } catch(NumberFormatException e) { 
		        return false; 
		    } catch(NullPointerException e) {
		        return false;
		    }
		}
		else if(tableType.equals(HomebrewOrmDataTypes.floatType.value)){
			try { 
		        Float.parseFloat(value); 
		    } catch(NumberFormatException e) { 
		        return false; 
		    } catch(NullPointerException e) {
		        return false;
		    }
		}
		else if(tableType.equals(HomebrewOrmDataTypes.longType.value)){
			try { 
		        Long.parseLong(value); 
		    } catch(NumberFormatException e) { 
		        return false; 
		    } catch(NullPointerException e) {
		        return false;
		    }
		}
		else if(tableType.equals(HomebrewOrmDataTypes.shortType.value)){
			try { 
		        Short.parseShort(value); 
		    } catch(NumberFormatException e) { 
		        return false; 
		    } catch(NullPointerException e) {
		        return false;
		    }
		}
		return flag;
	}
	
	private HomebrewOrmTable findTable(String tableName) {
		HomebrewOrmTable table = null;
		for(HomebrewOrmTable tables: listeTables){
			if(tables.getTableName().equals(tableName)){
				table = tables;
				break;
			}
		}
		return table;
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
	
	private boolean tableExists(String tableTofind){
		boolean flag = false;
		for(HomebrewOrmTable table: listeTables){
			if(table.getTableName().equals(tableTofind)){
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
	
	public static void main(String[] args) {
		ExampleUser exampleUser = new ExampleUser("jd", "rondeau", 911);
		HomebrewOrmTable table = new HomebrewOrmTable();
		table.setTableName("exampleUser");
		table.addValue(new HomebrewOrmTableValue("firstName", HomebrewOrmDataTypes.stringType.value));
		table.addValue(new HomebrewOrmTableValue("lastName", HomebrewOrmDataTypes.stringType.value));
		table.addValue(new HomebrewOrmTableValue("telephoneNumber", HomebrewOrmDataTypes.integerType.value));
		HomebrewOrm.getInstance().createTable(table);
		HomebrewOrm.getInstance().insert(exampleUser, "exampleUser");
		HomebrewOrm.getInstance().commit();
	}
}
