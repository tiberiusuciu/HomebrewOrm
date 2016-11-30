package com.orm;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private static String path = "";
	private static HomebrewOrm instance = null;	
	
	
	private HomebrewOrm() {
		path = loadConfiguration();
		listeTransactions = new ArrayList<String>();
		listeTables = new ArrayList<HomebrewOrmTable>();
		datas = new HashMap<String, Map<String,Object>>();
		if(path != null) {
			this.databasePath = path;
		}
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
		loadTable(table.getTableName());
		if(!tableExists(table.getTableName())){
			boolean hasId = false;
			boolean hasDelete = false;
			for(HomebrewOrmTableValue hormV: table.getValues()) {
				if (hormV.getColumnName().equals("_id")) {
					hasId = true;
					break;
				}
				if (hormV.getColumnName().equals("isDeleted")) {
					hasDelete = true;
					break;
				}
			}
			if(!hasId) {
				table.addValue(new HomebrewOrmTableValue("_id", HomebrewOrmDataTypes.integerType.value));
			}
			if(!hasDelete) {
				table.addValue(new HomebrewOrmTableValue("isDeleted", HomebrewOrmDataTypes.booleanType.value));
			}
			listeTables.add(table);
			writeTable(table.getTableName());
			datas.put(table.getTableName(), new HashMap<String, Object>());
			writeData();
			flag = true;
		}
		return flag;
	}
	
	public void updateData(String tableName,
						   HashMap<String, String> collumnsToUpdate, 
						   HashMap<String, String> where) {
		String transaction = "update;"+tableName+";";
		for(Map.Entry<String, String> collumn : collumnsToUpdate.entrySet()) {
			transaction+=collumn.getKey()+":"+collumn.getValue()+",";
		}
		transaction+=";";
		for(Map.Entry<String, String> whereValue : where.entrySet()) {
			transaction+=whereValue.getKey()+":"+whereValue.getValue()+",";
		}
		transaction+="";
		listeTransactions.add(transaction);
	}
	
	public void updateTable(String tableName, List<HomebrewOrmTableValue> listeValues, String value) {
		loadTable(tableName);
		if(tableExists(tableName)){
			for (HomebrewOrmTableValue homebrewOrmTableValueParametre : listeValues) {
				boolean trouve = false;
				for (HomebrewOrmTableValue homebrewOrmListTableValue : findTable(tableName).getValues()) {
					if(homebrewOrmTableValueParametre.getColumnName().equals(homebrewOrmListTableValue.getColumnName())){
						trouve = true;
						homebrewOrmListTableValue.setType(homebrewOrmTableValueParametre.getType());
						alterTable(tableName, homebrewOrmTableValueParametre.getColumnName(), value);
					}
				}
				if(!trouve){
					findTable(tableName).addValue(homebrewOrmTableValueParametre);
					for (Entry<String, Object> entry : datas.get(tableName).entrySet()){
					    ArrayList<Object> values = (ArrayList<Object>) entry.getValue();
					    Map map = new HashMap<>();
					    map.put(homebrewOrmTableValueParametre.columnName, value);
					    values.add(map);
					}
				}
			}
		}
		writeTable(tableName);
		writeData();
	}
	
	public void alterTable(String tableName, String columnName, String value){
		loadTable(tableName);
		HashMap<String, ArrayList<Map<String, Object>>> where= where(tableName, "");
		for(Entry<String, ArrayList<Map<String, Object>>> entry : where.entrySet()){
			for(Map<String, Object> property : where.get(entry.getKey())) {
				for(HomebrewOrmTableValue collumnValue: findTable(tableName).getValues()){
					if(collumnValue.getColumnName().equals(columnName)){
						if(verifyType(collumnValue.getType(), value)){
							property.replace(columnName, value);
						}
					}
				}
			}
		}
		writeData();
	}
	
	private void deleteValueTransaction(String[] transactionInfos) {
		HashMap<String, ArrayList<Map<String, Object>>> map = where(transactionInfos[1], transactionInfos[2]);
		for(Entry<String, ArrayList<Map<String, Object>>> entry : map.entrySet()){
			for(Map<String, Object> property : map.get(entry.getKey())) {
				property.replace("isDeleted", "true");
			}
		}
	}
	
	private HashMap<String, ArrayList<Map<String, Object>>> where(String tableName, String conditions) {
		HashMap<String, ArrayList<Map<String,Object>>> result = new HashMap<String, ArrayList<Map<String,Object>>>();
		if(conditions.indexOf(',') < 0) {
			for(Entry<String, Object> iterator : datas.get(tableName).entrySet()) {
				result.put(iterator.getKey(), (ArrayList<Map<String, Object>>) iterator.getValue());
			}
		}
		else {
			String[] filtres = conditions.split(",");
			HashMap<String, Object> dataToFilter = (HashMap<String, Object>) datas.get(tableName);
			String[] key = new String[filtres.length];
			String[] value = new String[filtres.length];
			for(int i = 0; i < filtres.length; i++){
				//seperate collums and values
				String[] columnValue = filtres[i].split(":");
				key[i] = columnValue[0];
				value[i] = columnValue[1];
			}
			for(Entry<String, Object> entry : dataToFilter.entrySet()) {
				ArrayList<Map<String, String>> entryProps = (ArrayList<Map<String, String>>) entry.getValue();
				boolean trouve = true;
				for(Map<String, String> prop : entryProps) {
					for(Entry<String, String> columnValue : prop.entrySet()){
						for(int i = 0; i < filtres.length; i++) {
							if(columnValue.getKey().equals(key[i])) {
								if(!columnValue.getValue().equals(value[i])) {
									trouve = false;
									break;
								}
							}
						}
					}
				}
				if(trouve) {
					result.put(entry.getKey(), (ArrayList<Map<String, Object>>) entry.getValue());
				}
			}	
		}
		return result;
	}
	
	public void deleteValue(String tableName,
							HashMap<String, String> where) {
		String transaction = "deleteValue;"+tableName+";";
		for(Map.Entry<String, String> whereValue : where.entrySet()) {
			transaction+=whereValue.getKey()+":"+whereValue.getValue()+",";
		}
		listeTransactions.add(transaction);
	}
	
	public void removeValue(String tableName,
			HashMap<String, String> where) {
		String transaction = "removeValue;"+tableName+";";
		for(Map.Entry<String, String> whereValue : where.entrySet()) {
			transaction+=whereValue.getKey()+":"+whereValue.getValue()+",";
		}
		listeTransactions.add(transaction);
	}
	
	public void removeTable(String tableName) {
		File fileTable = new File(path+TABLE_DIR_NAME+"/"+tableName+".json");
		fileTable.delete();
		
		File fileData = new File(path+DATA_DIR_NAME+"/"+tableName+".json");
		fileData.delete();
	}
	
	public boolean commit() {
		boolean flag = false;
		if(verifyTransactions()){
			loadTablesRequired();
			executeTransactions();
			writeData();
			flag = true;
		}
		else{
			flag = false;
		}
		return flag;
	}

	private void loadTablesRequired(){
		for (HomebrewOrmTable homebrewOrmTable : listeTables) {
			loadData(homebrewOrmTable.getTableName());
		}
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
				if(!verifyUpdate(transactionInfos)){
					return false;
				}
				break;
			case "deleteValue":
				if(!verifyDeleteRemoveValue(transactionInfos)){
					return false;
				}
				break;
			case "removeValue":
				if(!verifyDeleteRemoveValue(transactionInfos)){
					return false;
				}
				break;
			default:
				break;
			}
		}
		return flag;
	}
	
	private void executeTransactions() {
		for (String transaction: listeTransactions) {
			String[] transactionInfos = transaction.split(";");
			switch (transactionInfos[0]) {
			case "insert":
				insertTransaction(transactionInfos);
				break;
			case "update":
				updateTransaction(transactionInfos);
				break;
			case "deleteValue":
				deleteValueTransaction(transactionInfos);
				break;
			case "removeValue":
				removeValueTransaction(transactionInfos);
				break;
			default:
				break;
			}
		}
	}
	
	private void updateTransaction(String[] transactionInfos){
		String[] conditions = transactionInfos[3].split(",");
		HashMap<String, ArrayList<Map<String, Object>>> map = where(transactionInfos[1], transactionInfos[3]);
		for(Entry<String, ArrayList<Map<String, Object>>> entry : map.entrySet()){
			for(Map<String, Object> property : map.get(entry.getKey())) {
				String[] updateCondition =  transactionInfos[2].split(",");
				for(int i = 0; i < updateCondition.length;i++){
					String[] updateColumnValue = updateCondition[i].split(":");
					property.replace(updateColumnValue[0], updateColumnValue[1]);
				}
			}
		}
	}
	
	private void removeValueTransaction(String[] transactionInfos){
		HashMap<String, ArrayList<Map<String, Object>>> whereReturn = where(transactionInfos[1], transactionInfos[2]);
		for (Entry<String, ArrayList<Map<String, Object>>> entry : whereReturn.entrySet()){
			datas.get(transactionInfos[1]).remove(entry.getKey());
		}
	}

	private boolean verifyInsertion(String[] transactionInfos) {
		boolean flag = true;
		loadTable(transactionInfos[2]);
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
	
	private boolean verifyUpdate(String[] transactionInfos){
		boolean flag = true;
		loadTable(transactionInfos[1]);
		if(tableExists(transactionInfos[1])){
			//find table with tableName
			HomebrewOrmTable table = findTable(transactionInfos[1]);
			//seperate all collumns
			String[] valueToUpdate = transactionInfos[2].split(",");
			for(int i = 0; i < valueToUpdate.length;i++){
				//seperate collums and values
				String[] columnValue = valueToUpdate[i].split(":");
				//verify if all column are legit
				String columnType =  findColumnType(table, columnValue[0]);
				if(columnType == null){
					flag = false;
					break;
				}
				else{
					if(!verifyType(columnType, columnValue[1])){
						flag = false;
						break;
					}
				}
			}
			if(flag!=false){
				String[] valueOfWhere = transactionInfos[3].split(",");
				for(int i = 0; i < valueOfWhere.length;i++){
					//seperate collums and values
					String[] columnValue = valueOfWhere[i].split(":");
					//verify if all column are legit
					String columnType =  findColumnType(table, columnValue[0]);
					if(columnType == null){
						flag = false;
						break;
					}
					else{
						if(!verifyType(columnType, columnValue[1])){
							flag = false;
							break;
						}
					}
				}
			}
		}
		else{
			flag = false;
		}
		return flag;
	}
	
	private String findColumnType(HomebrewOrmTable table, String columnName){
		String flag = null;
		for (HomebrewOrmTableValue value : table.getValues()) {
			if(columnName.equals(value.getColumnName())){
				flag = value.getType();
				break;
			}
		}
		
		return flag;
	}
	
	private boolean verifyDeleteRemoveValue(String[] transactionInfos){
		boolean flag = true;
		loadTable(transactionInfos[1]);
		if(tableExists(transactionInfos[1])){
			//find table with tableName
			HomebrewOrmTable table = findTable(transactionInfos[1]);
			//seperate all collumns
			String[] valueToDelete = transactionInfos[2].split(",");
			for(int i = 0; i < valueToDelete.length;i++){
				//seperate collums and values
				String[] columnValue = valueToDelete[i].split(":");
				//verify if all column are legit
				String columnType =  findColumnType(table, columnValue[0]);
				if(columnType == null){
					flag = false;
					break;
				}
				else{
					if(!verifyType(columnType, columnValue[1])){
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
	
	private void insertTransaction(String[] transactionInfos){
		Map<String, Object> tableData = datas.get(transactionInfos[2]);
		List<Map<String, String>> listeColumnValue = new ArrayList<>();
		String[] rows = transactionInfos[1].split(",");
		Boolean _idExist = false;
		for (String row : rows) {
			String[] columValue = row.split(":");
			Map<String, String> map = new HashMap<>();
			map.put(columValue[0], columValue[1]);
			listeColumnValue.add(map);
			if(columValue[0].equals("_id")){
				_idExist = true;
			}
		}
		if(!_idExist){
			Map<String, String> _id = new HashMap<>();
			_id.put("_id",findBiggestId(tableData)+1+"");
			listeColumnValue.add(_id);
		}
		
		Map<String, String> isDeleted = new HashMap<>();
		isDeleted.put("isDeleted","false");
		listeColumnValue.add(isDeleted);
		
		tableData.put(findBiggestId(tableData)+1+"", listeColumnValue);
	}
	
	private int findBiggestId(Map<String, Object> map){
		int flag = 0;
		for (Entry<String, Object> entry : map.entrySet())
		{
		   if(flag < Integer.parseInt(entry.getKey())){
			   flag = Integer.parseInt(entry.getKey());
		   }
		}
		return flag;
	}
	
	public HashMap<String, ArrayList<Map<String, Object>>> select(String tableName, HashMap<String, String> where) {
		loadTable(tableName);
		HashMap<String, ArrayList<Map<String, Object>>> map;
		if(where != null) {
			String transaction = "select;"+tableName+";";
			for(Map.Entry<String, String> whereValue : where.entrySet()) {
				transaction+=whereValue.getKey()+":"+whereValue.getValue()+",";
			}
			String conditions = transaction.split(";")[2];
			map = where(tableName, conditions);
		}
		else {
			map = where(tableName, "");
		}
		return map;
	}
	
	public void oneToOne(String mostImportantTableName, String lesserImportantTableName) {
		loadTable(mostImportantTableName);
		loadTable(lesserImportantTableName);
		if(tableExists(mostImportantTableName) && tableExists(lesserImportantTableName)){
			ArrayList<HomebrewOrmTableValue> homebrewOrmTableValues = new ArrayList<>();
			HomebrewOrmTableValue oneToOneLink = new HomebrewOrmTableValue("_"+mostImportantTableName,
													HomebrewOrmDataTypes.integerType.getValueString());
			homebrewOrmTableValues.add(oneToOneLink);
			updateTable(lesserImportantTableName, homebrewOrmTableValues, "null");
		}
	}
	
	public void oneToMany(String mostImportantTableName, String lesserImportantTableName) {
		oneToOne(mostImportantTableName, lesserImportantTableName);
	}
	
	public void manyToMany(String tableName1, String tableName2) {
		loadTable(tableName1);
		loadTable(tableName2);
		if(tableExists(tableName1) && tableExists(tableName2)){
			HomebrewOrmTable table = new HomebrewOrmTable();
			table.setTableName(tableName1+tableName2.substring(0, 1).toUpperCase()+tableName2.substring(1));
			HomebrewOrmTableValue column1 = new HomebrewOrmTableValue("_"+tableName1+"id", HomebrewOrmDataTypes.integerType.getValueString());
			HomebrewOrmTableValue column2 = new HomebrewOrmTableValue("_"+tableName2+"id", HomebrewOrmDataTypes.integerType.getValueString());
			table.addValue(column1);
			table.addValue(column2);
			createTable(table);
		}
	}
	
	private void loadData(String dataToLoad) {
		String dataFileName = dataToLoad + ".json";
		ObjectMapper mapper = new ObjectMapper();
		File dataDir = new File(this.databasePath + DATA_DIR_NAME);
		String[] dataFiles = dataDir.list();
		for(String file : dataFiles) {
			if(file.equals(dataFileName)){
				try {
					datas.put(dataToLoad, mapper.readValue(new File(dataDir + "/" + file), Map.class));
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
		if(!tableExists(tableToLoad)){
			String tableFileName = tableToLoad + ".json";
			ObjectMapper mapper = new ObjectMapper();
			File tableDir = new File(this.databasePath + TABLE_DIR_NAME);
			String[] tableFiles = tableDir.list();
			if(tableFiles != null) {
				for(String file : tableFiles) {
					if(file.equals(tableFileName)) {
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
	}
	
	private void writeTable(String tableName) {
		ObjectMapper mapper = new ObjectMapper();
		HomebrewOrmTable homebrewOrmTable = findTable(tableName);
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
	
	private void writeData() {
		ObjectMapper mapper = new ObjectMapper();
		File dataDir = new File(this.databasePath + DATA_DIR_NAME);
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		for(HomebrewOrmTable ormTable : listeTables) {
			try {
				writer.writeValue(new File(dataDir + "/" + ormTable.getTableName() + ".json"), datas.get(ormTable.getTableName()));
			} catch (IOException e) {
				e.printStackTrace();
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
	
	
	public void pretty(HashMap<String, ArrayList<Map<String, Object>>> map) {
		for(Entry<String, ArrayList<Map<String, Object>>> entry : map.entrySet()) {
			System.out.println("{" + entry.getKey() + ": [");
			for(Map<String, Object> props : entry.getValue()) {
				System.out.println("    " + props + ", ");
			}
			System.out.println("]},");
		}
	}
	/*
	public HashMap<String, ArrayList<Map<String, Object>>> sort(HashMap<String, ArrayList<Map<String, Object>>> map) {
		HashMap<String, ArrayList<Map<String, Object>>> newMap = new HashMap<String, ArrayList<Map<String, Object>>>();
		ArrayList<Integer> keys = new ArrayList<Integer>();
		for(Entry<String, ArrayList<Map<String, Object>>> entry : map.entrySet()) {
			keys.add(Integer.parseInt(entry.getKey()));
		}
		boolean flag = true;
		int tmp;
	    while (flag){
	    	flag= false;
	    	for(int i = 0; i < keys.size() - 1; i++) {
				 if(keys.get(i) < keys.get(i+1)) {
					 tmp = keys.get(i);
					 keys.set(i, keys.get(i + 1));
					 keys.set(i + 1, tmp);
					 flag = true;
				 }
			}
	    }
	    for(int i = 0; i < keys.size() - 1; i++) {
			 newMap.put(keys.get(i) + "", map.get(keys.get(i)));
		}
		return newMap;
	}
	*/
	/*public static void main(String[] args) {
		
		ExampleUser exampleUser = new ExampleUser("jd", "rondeau", 911);
		HomebrewOrmTable table = new HomebrewOrmTable();
		table.setTableName("exampleUser");
		table.addValue(new HomebrewOrmTableValue("firstName", HomebrewOrmDataTypes.stringType.value));
		table.addValue(new HomebrewOrmTableValue("lastName", HomebrewOrmDataTypes.stringType.value));
		table.addValue(new HomebrewOrmTableValue("telephoneNumber", HomebrewOrmDataTypes.integerType.value));
		HomebrewOrm.getInstance().createTable(table);
		HomebrewOrm.getInstance().insert(exampleUser, "exampleUser");
		HomebrewOrm.getInstance().commit();
		
		HomebrewOrm homebrewOrm = new HomebrewOrm();
		homebrewOrm.loadTable("exampleUser");
		HashMap<String, String> where = new HashMap<>();
		//where.put("blabla", "shiet");
		//where.put("firstName", "Tiberiu Cristian");
		//where.put("lastName", "Soares");
		//homebrewOrm.deleteValue("exampleUser", where);
		//homebrewOrm.commit();
		homebrewOrm.pretty(homebrewOrm.select("exampleUser", null));
		System.out.println(homebrewOrm.select("exampleUser", null).size());
		//homebrewOrm.writeData();
	}*/
}
