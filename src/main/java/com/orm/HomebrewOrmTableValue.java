package com.orm;

public class HomebrewOrmTableValue {

	public String columnName;
	public String type;
	
	public HomebrewOrmTableValue(String columnName, String type) {
		super();
		this.columnName = columnName;
		this.type = type;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "HomebrewOrmTableValue [columnName=" + columnName + ", type=" + type + "]";
	}
}
