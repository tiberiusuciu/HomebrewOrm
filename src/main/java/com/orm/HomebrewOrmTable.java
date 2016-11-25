package com.orm;

import java.util.ArrayList;
import java.util.List;

public class HomebrewOrmTable {
	private String tableName;
	private List<HomebrewOrmTableValue> values;
	
	public HomebrewOrmTable() {
		tableName = "";
		values = new ArrayList<HomebrewOrmTableValue>();
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<HomebrewOrmTableValue> getValues() {
		return values;
	}

	public void setValues(List<HomebrewOrmTableValue> values) {
		this.values = values;
	}
	
	public void addValue(HomebrewOrmTableValue value){
		values.add(value);
	}

	@Override
	public String toString() {
		return "HomebrewOrmTable [tableName=" + tableName + ", values=" + values + "]";
	}
}
