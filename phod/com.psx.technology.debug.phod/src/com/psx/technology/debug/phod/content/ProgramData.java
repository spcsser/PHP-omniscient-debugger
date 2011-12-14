package com.psx.technology.debug.phod.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.psx.technology.debug.phod.content.data.AbstractData;
import com.psx.technology.debug.phod.content.data.AtomType;
import com.psx.technology.debug.phod.content.data.Modifier;
import com.psx.technology.debug.phod.content.data.ValueData;
import com.psx.technology.debug.phod.content.data.ValueData.ValueCoreData;
import com.psx.technology.debug.phod.content.data.VariableData;

public class ProgramData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3662558859184043391L;
	public static final String SCOPETYPE_CLASS = "C";
	public static final String SCOPETYPE_LOCAL = "L";
	public static final String SCOPETYPE_GLOBAL = "G";
	public static final String SCOPETYPE_OBJECT = "O";
	public static final String SCOPETYPE_FAILURE = "F";
	HashMap<String, VariableData> variableTable;
	HashMap<Long, ValueData> valueTable;
	ProgramCalls pc;
	
	public ProgramData(ProgramCalls pc, Integer varTableCapacity, Integer valTableCapacity){
		//key is ast + var name
		variableTable=new HashMap<String, VariableData>(varTableCapacity);
		//key is id
		valueTable=new HashMap<Long, ValueData>(valTableCapacity);
		this.pc=pc;
		pc.setProgramData(this);
	}
	
	public void clearData(){
		variableTable.clear();
		variableTable=null;
		valueTable.clear();
		valueTable=null;
	}
	
	public ProgramCalls getProgramCalls(){
		return pc;
	}


	public ValueCoreData getValueData(AbstractData parent, Long id, Long actionId) {
		ValueData vd=valueTable.get(id);
		if(vd==null){
			vd=new ValueData(parent,id, AtomType.Undefined, actionId, null, "CalledUnknownId");
			valueTable.put(id, vd);
		}
		return vd.getLastValue();
	}
	
	public ValueCoreData getValueData(Long id, Long actionId) {
		ValueData vd=valueTable.get(id);
		return vd.getValueDataForAction(actionId);
	}

	public ValueCoreData addValueData(VariableData parent, Long id, com.psx.technology.debug.phod.content.data.AtomType type,
			Long actionId, String name, String value) {
		ValueData vd=null;
		ValueCoreData vcd=null;
		vd=valueTable.get(id);
		if(vd==null){
			vd=new ValueData(parent,id, type, actionId, name, value);
			valueTable.put(id, vd);
			vcd=vd.getLastValue();
		}else{
			vcd=vd.addTimeEntry(parent,id, type, actionId, name, value);
		}
		return vcd;
	}


	public VariableData getVariable(AbstractData parent, Long scope, Long classId, String scopeType, String name, Long actionId, Modifier mod) {
		VariableData vd=null;
		if(mod.compareTo(Modifier.Static)>=0 && mod.compareTo(Modifier.Private_Static)<=0){
			scopeType=SCOPETYPE_CLASS;
			scope=classId;
		}else if(mod.compareTo(Modifier.Local)==0){
			scopeType=SCOPETYPE_LOCAL;
			if(parent instanceof VariableData && ((VariableData)parent).getModifier().equals(Modifier.Arguments)){
				scope=actionId;
			}
		}
		Pattern pattern=Pattern.compile("(?:(?:self::\\$?)|(?:[a-zA-Z0-9\\_-]*::))(.*)");
		Matcher matcher=pattern.matcher(name);
		if(matcher.matches()){
			name=matcher.replaceAll("$1");
			scopeType=SCOPETYPE_CLASS;
			scope=classId;
			if(mod.equals(Modifier.Unknown)||mod.equals(Modifier.Local)){
				mod=Modifier.Static;
			}
		}
		pattern=Pattern.compile("(?:(?:\\$?(?:[^\\->]*->)+)|(?:\\{[^\\\\}]\\}:))(.*)");
		matcher=pattern.matcher(name);
		if(matcher.matches()){
			name=matcher.replaceAll("$1");
			scopeType=SCOPETYPE_OBJECT;
		}
		pattern=Pattern.compile("\\$(.*)");
		matcher=pattern.matcher(name);
		if(matcher.matches()){
			name=matcher.replaceAll("$1");
			if(scopeType==""){
				scopeType=SCOPETYPE_LOCAL;
				scope=actionId;
			}
		}
		
		String identif=scopeType+":"+scope+":"+name;
		vd=variableTable.get(identif);
		if(vd==null){
//			vd=new VariableData(parent, scope, scopeType, name, actionId, mod);
			vd=new VariableData(parent, name, actionId, mod);
			variableTable.put(identif, vd);
		}else{
			if(vd.getModifier()==null||vd.getModifier().equals(Modifier.Unknown) && !mod.equals(Modifier.Unknown)){
				vd.setModifier(mod);
			}
			parent.add(vd);
		}
		return vd;
	}

	public void getDataStatistics() {
		System.out.println("VarTable: "+variableTable.size()+", ValTable: "+valueTable.size());
	}


//	public VariableData getVariableData(VariableData vd) {
//		variableTable.get(vd.getScopeType()+":"+vd.getName());
//		return null;		
//	}
	
}
