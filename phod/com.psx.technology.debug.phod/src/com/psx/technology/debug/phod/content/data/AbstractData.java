package com.psx.technology.debug.phod.content.data;

import java.util.regex.Pattern;


public abstract class AbstractData extends TreeNode implements IData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6177737737499472462L;

	public AbstractData(AbstractData parent, Number actionId) {
		super(parent,actionId);
	}
	
	protected boolean _containsValue(Pattern text){
		if(checkVal(actionId,text)) return true;
		
		return false;
	}
}
