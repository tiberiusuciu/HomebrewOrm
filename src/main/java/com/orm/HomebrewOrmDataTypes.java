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

	public static String fromString(String text) {
		if (text != null) {
			if (text.equalsIgnoreCase(stringType.value)) {
				return HomebrewOrmDataTypes.stringType.value;
			} else if (text.equalsIgnoreCase(integerType.value)) {
				return HomebrewOrmDataTypes.integerType.value;
			} else if (text.equalsIgnoreCase(doubleType.value)) {
				return HomebrewOrmDataTypes.doubleType.value;
			} else if (text.equalsIgnoreCase(longType.value)) {
				return HomebrewOrmDataTypes.longType.value;
			} else if (text.equalsIgnoreCase(shortType.value)) {
				return HomebrewOrmDataTypes.shortType.value;
			}else if (text.equalsIgnoreCase(booleanType.value)) {
				return HomebrewOrmDataTypes.booleanType.value;
			}else if (text.equalsIgnoreCase(charType.value)) {
				return HomebrewOrmDataTypes.charType.value;
			}else if (text.equalsIgnoreCase(floatType.value)) {
				return HomebrewOrmDataTypes.floatType.value;
			}
		}
		return null;
	}
	
	
	
}
