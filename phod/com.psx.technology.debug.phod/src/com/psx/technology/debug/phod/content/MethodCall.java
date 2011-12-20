package com.psx.technology.debug.phod.content;

import java.util.regex.Pattern;

import com.psx.technology.debug.phod.content.data.TreeNode;
import com.psx.technology.debug.phod.content.parser.PHPFunctionType;

public class MethodCall extends BasicOperation<MethodCall> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1652906306883069791L;
	protected PHPFunctionType functionType;
	protected String includeFile;
	protected Number userDefinedId;
	private Number functionTypeId;
	private String[] nameArray;
	
	public MethodCall(Number id, TreeNode parent) {
		super(id, 2, parent);
	}
	
	public MethodCall(Number id) {
		this(id,null);
	}

	public void setUserDefined(Number ud){
		userDefinedId=ud.intValue();
	}

	public boolean isUserDefined() {
		return userDefinedId.intValue() > 0 && getFunctionType().getId() < PHPFunctionType.Eval.getId();
	}

	public String getNamespace() {
		return nameArray[2];
	}

	public String getClassName() {
		return nameArray[1];
	}
	
	public String getMethodName() {
		return nameArray[0];
	}

	public String toString() {
		return getMethodName();
	}

	public boolean containsValue(Pattern text){
		if(checkVal(nameArray[0],text))  return true;
		if(checkVal(nameArray[1],text)) return true;
		if(checkVal(nameArray[2],text))  return true;
		if(checkVal(filePath,text))   return true;
		
		return false;
	}
	
	public Double getTime() {
		return timestamp[1].doubleValue()-timestamp[0].doubleValue();
	}


	public Long getMemoryCommit() {
		return memorySize[1].longValue()-memorySize[0].longValue();
	}

	public void setFunctionTypeId(Number valueOf) {
		this.functionTypeId=valueOf;
	}
	
	public PHPFunctionType getFunctionType(){
		if(this.functionType==null){
			this.functionType=PHPFunctionType.getFType(functionTypeId.intValue());
		}
		return this.functionType;
	}

	public void setIncludeFile(String object) {
		this.includeFile=object;
	}
	
	public String getIncludeFile(){
		return this.includeFile;
	}

	public void setNameArray(String[] mtd) {
		this.nameArray=mtd;
	}

	public boolean isMethod() {
		return getFunctionType().equals(PHPFunctionType.Member);
	}
	
	public boolean isStaticMethod(){
		return getFunctionType().equals(PHPFunctionType.StaticMember);
	}
}
