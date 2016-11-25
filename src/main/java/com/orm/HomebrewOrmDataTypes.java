package com.orm;

public enum HomebrewOrmDataTypes {
	stringType("string"), integerType("integer"), doubleType("double"), longType(
			"long"), shortType("short"), booleanType("boolean"), charType("char"), floatType("float");
	String value;

	private HomebrewOrmDataTypes(String value) {
		this.value = value;
	}

	public String getValueString() {
		return value;
	}

	public static HomebrewOrmDataTypes fromString(String text) {
		if (text != null) {
			if (HomebrewOrmDataTypes.stringType.equals(text)) {
				return HomebrewOrmDataTypes.stringType;
			} else if (HomebrewOrmDataTypes.integerType.equals(text)) {
				return HomebrewOrmDataTypes.integerType;
			} else if (HomebrewOrmDataTypes.doubleType.equals(text)) {
				return HomebrewOrmDataTypes.doubleType;
			} else if (HomebrewOrmDataTypes.longType.equals(text)) {
				return HomebrewOrmDataTypes.longType;
			} else if (HomebrewOrmDataTypes.shortType.equals(text)) {
				return HomebrewOrmDataTypes.shortType;
			}else if (HomebrewOrmDataTypes.booleanType.equals(text)) {
				return HomebrewOrmDataTypes.booleanType;
			}else if (HomebrewOrmDataTypes.charType.equals(text)) {
				return HomebrewOrmDataTypes.charType;
			}else if (HomebrewOrmDataTypes.floatType.equals(text)) {
				return HomebrewOrmDataTypes.floatType;
			}
		}
		return null;
	}
	
	
	
}
