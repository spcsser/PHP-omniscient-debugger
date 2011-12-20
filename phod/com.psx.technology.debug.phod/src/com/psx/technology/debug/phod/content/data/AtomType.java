package com.psx.technology.debug.phod.content.data;

public enum AtomType{
	Undefined(0),Void(1),Null(2),Boolean(3),Integer(4),Double(5),String(6),Array(7),ArrayField(8),Object(9),Resource(10),Mixed(11),Class(12);

	protected Integer id;
	private AtomType(Integer id){
		this.id=id;
	}
	
	public static AtomType getType(Integer id){
		for(AtomType at:values()){
			if(at.id.equals(id)){
				return at;
			}
		}
		return Undefined;
	}
	
	public Integer getId(){
		return id;
	}
}