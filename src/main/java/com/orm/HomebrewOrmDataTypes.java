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
			if (text.equalsIgnoreCase(stringType.getValueString())) {
				return HomebrewOrmDataTypes.stringType;
			} else if (text.equalsIgnoreCase(integerType.getValueString())) {
				return HomebrewOrmDataTypes.integerType;
			} else if (text.equalsIgnoreCase(doubleType.getValueString())) {
				return HomebrewOrmDataTypes.doubleType;
			} else if (text.equalsIgnoreCase(longType.getValueString())) {
				return HomebrewOrmDataTypes.longType;
			} else if (text.equalsIgnoreCase(shortType.getValueString())) {
				return HomebrewOrmDataTypes.shortType;
			}else if (text.equalsIgnoreCase(booleanType.getValueString())) {
				return HomebrewOrmDataTypes.booleanType;
			}else if (text.equalsIgnoreCase(charType.getValueString())) {
				return HomebrewOrmDataTypes.charType;
			}else if (text.equalsIgnoreCase(floatType.getValueString())) {
				return HomebrewOrmDataTypes.floatType;
			}
		}
		return null;
	}
	
	
	
}
