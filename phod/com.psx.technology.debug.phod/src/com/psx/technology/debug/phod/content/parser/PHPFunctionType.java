package com.psx.technology.debug.phod.content.parser;

public enum PHPFunctionType {
	Unknown(0), Normal(1), StaticMember(2), Member(3), New(4), Eval(16), Include(17), Include_Once(18), Require(19), Require_Once(
			20);

	protected int id;

	PHPFunctionType(int id) {
		this.id = id;
	}
	
	public int getId(){
		return id;
	}

	public static PHPFunctionType getFType(int id) {
		for (PHPFunctionType f : values()) {
			if (f.id == id) {
				return f;
			}
		}
		return null;
	}
}