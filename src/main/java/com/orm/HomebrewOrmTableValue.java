package com.orm;

public class HomebrewOrmTableValue {

	public String columnName;
	public HomebrewOrmDataTypes type;
	
	public HomebrewOrmTableValue(String columnName, HomebrewOrmDataTypes type) {
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

	public HomebrewOrmDataTypes getType() {
		return type;
	}

	public void setType(HomebrewOrmDataTypes type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "HomebrewOrmTableValue [columnName=" + columnName + ", type=" + type + "]";
	}
}
