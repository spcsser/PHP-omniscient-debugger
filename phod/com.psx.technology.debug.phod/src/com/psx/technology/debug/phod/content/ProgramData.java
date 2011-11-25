package com.psx.technology.debug.phod.content;

import java.io.Serializable;
import java.util.HashMap;

import com.psx.technology.debug.phod.content.data.AbstractData;
import com.psx.technology.debug.phod.content.data.AtomType;
import com.psx.technology.debug.phod.content.data.Modifier;
import com.psx.technology.debug.phod.content.data.ValueData;
import com.psx.technology.debug.phod.content.data.VariableData;
import com.psx.technology.debug.phod.content.data.ValueData.ValueCoreData;

public class ProgramData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3662558859184043391L;
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


	public VariableData getVariable(AbstractData parent, Long scope, String scopeType, String name, Long actionId, Modifier mod) {
		VariableData vd=null;
		if(mod.compareTo(Modifier.Public_Static)>=0 && mod.compareTo(Modifier.Private_Static)<=0){
			scope=0L;
		}
		name=name.replaceFirst("(self::\\$)|(\\$this->)", "");
		String identif=scopeType+":"/*+scope*/+":"+name;
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
