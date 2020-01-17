package com.test.cache.entity;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class Test {
	
	@QuerySqlField
	String name;
	
	@QuerySqlField
	String num;

	public Test(String name, String num){
		this.name = name;
		this.num = num;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

	
	
}
