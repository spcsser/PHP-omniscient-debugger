package com.psx.technology.debug.phod.content.data;

import java.util.HashMap;

public enum Modifier {
	Unknown("Unknown"), ArrayField("ArrayField"), Public("public"), Protected("protected"), Private("private"), 
			Static("static"), Public_Static("public static"), Protected_Static("protected static"), 
			Private_Static("private static"), Constant("const"), Public_Constant("public const"), 
			Protected_Constant("protected const"), Private_Constant("private const"), Local("local"),
			Assignment("Assignment"), This("This"), Return("Return"), Arguments("Arguments");

	protected String name;
	protected static HashMap<String, Modifier> mods;

	Modifier(String name) {
		this.name = name;
		Modifier.getModMap().put(name, this);
	}

	protected static HashMap<String,Modifier> getModMap(){
		if(mods==null){
			mods = new HashMap<String, Modifier>();
		}
		return mods;
	}
	
	public String getName() {
		return name;
	}

	public static Modifier getModifier(Object name) {
		return mods.get(name);
	}
}