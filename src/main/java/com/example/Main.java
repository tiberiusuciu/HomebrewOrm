package com.example;

import java.util.ArrayList;
import java.util.HashMap;

import com.orm.HomebrewOrm;
import com.orm.HomebrewOrmDataTypes;
import com.orm.HomebrewOrmTable;
import com.orm.HomebrewOrmTableValue;

public class Main {
	public static void main(String[] args) {
		ExampleUser exampleUser = new ExampleUser("jd", "rondeau", 911);
		HomebrewOrmTable table = new HomebrewOrmTable();
		table.setTableName("exampleUser");
		table.addValue(new HomebrewOrmTableValue("firstName", HomebrewOrmDataTypes.stringType.getValueString()));
		table.addValue(new HomebrewOrmTableValue("lastName", HomebrewOrmDataTypes.stringType.getValueString()));
		table.addValue(new HomebrewOrmTableValue("telephoneNumber", HomebrewOrmDataTypes.integerType.getValueString()));
		HomebrewOrm.getInstance().createTable(table);
		//System.out.println(HomebrewOrm.getInstance().listeTables);
		HomebrewOrm.getInstance().insert(exampleUser, "exampleUser");
		HashMap<String, String> columnToUpdate = new HashMap<>();
		columnToUpdate.put("firstName", "jean-Daniel");
		HashMap<String, String> where = new HashMap<>();
		where.put("_id", "4");
		HomebrewOrm.getInstance().updateData("exampleUser", columnToUpdate, where);
		//HomebrewOrm.getInstance().deleteValue("exampleUser", where);
		//HomebrewOrm.getInstance().removeValue("exampleUser", where);
		System.out.println(HomebrewOrm.getInstance().commit());
		HomebrewOrm.getInstance().removeTable("anothertest");
		HomebrewOrm.getInstance().alterTable("exampleUser", "telephoneNumber", "911");
		ArrayList<HomebrewOrmTableValue> listeValues = new ArrayList<>();
		listeValues.add(new HomebrewOrmTableValue("firstName", HomebrewOrmDataTypes.integerType.getValueString()));
		
		HomebrewOrm.getInstance().updateTable("exampleUser", listeValues, "1000");
	}
}
