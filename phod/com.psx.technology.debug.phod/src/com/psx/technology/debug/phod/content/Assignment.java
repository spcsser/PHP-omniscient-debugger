package com.psx.technology.debug.phod.content;

import java.util.regex.Pattern;

import com.psx.technology.debug.phod.content.data.TreeNode;
import com.psx.technology.debug.phod.content.data.VariableData;

public class Assignment extends BasicOperation<Assignment> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8615479261905292205L;
	protected String varName;
	protected VariableData varData;

	public Assignment(Long actionId, TreeNode parent) {
		super(actionId, 1, parent);
	}

	public void setVariableName(String string) {
		string=string.replaceFirst("(::)([^$])", "$1\\$$2");
		this.varName=string;
	}

	public String getVariableName() {
		return varName;
	}
	
	public Double getTime() {
		return timestamp[0].doubleValue()-timestamp[0].doubleValue();
	}


	public Long getMemoryCommit() {
		return memorySize[0].longValue()-memorySize[0].longValue();
	}
	

	public void setAssignmentdata(VariableData variableData) {
		this.varData=variableData;
	}
	
	public VariableData getAssignmentData(){
		return this.varData;
	}
	
	public boolean containsValue(Pattern text){
		if(checkVal(filePath,text))   return true;
		
		return false;
	}
}
