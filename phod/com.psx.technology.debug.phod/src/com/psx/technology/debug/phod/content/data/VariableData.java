package com.psx.technology.debug.phod.content.data;

import java.util.Collections;
import java.util.Vector;
import java.util.regex.Pattern;

import com.psx.technology.debug.phod.content.data.ValueData.ValueCoreData;

public class VariableData extends AbstractData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7324149414496185878L;
	// protected Long scope;
	// protected String scopeType;
	protected String name;
	// protected Vector<Object[]> timeEntrys;
	protected Modifier modifier;
	protected Vector<ValueCoreData> timeEntrys;
	protected Long lastId;

	// public VariableData(AbstractData parent, Long scope, String scopeType,
	// String name, Long actionId, Modifier mod){
	public VariableData(AbstractData parent, String name, Long actionId, Modifier mod) {
		super(parent, actionId);
		// this.scope=scope;
		// this.scopeType=scopeType;
		this.modifier = mod;
		this.name = name;
		this.timeEntrys = new Vector<ValueData.ValueCoreData>();
	}

	public ValueCoreData getValueDataForAction(Long action) {
		ValueCoreData lastVcd = null;
		if(!ordered){
			Collections.sort(timeEntrys, getComparator());
		}
		for (ValueCoreData vcd : timeEntrys) {
			int compare = vcd.getActionId().compareTo(action);
			if (compare == 0) {
				// getActionId matches action
				return vcd;
			} else if (compare > 0 && lastVcd != null) {
				// getActionId is after action
				return lastVcd;
			}
			lastVcd = vcd;
		}
		return lastVcd;
	}

	public void addTimeEntry(Long action, ValueCoreData value) {
		timeEntrys.add(value);
		lastId = action;
	}

	public TreeNode getLastValue() {
		return timeEntrys.lastElement();
	}

	@Override
	public AtomType getType() {
		return AtomType.Undefined;
	}

	public Modifier getModifier() {
		return this.modifier;
	}

	public String getName() {
		return name;
	}

	public void setModifier(Modifier mod) {
		modifier = mod;
	}

	public AbstractData getAssignmentValueCoreData(ValueCoreData v) {
		ValueCoreData prevVcd = v, vcd = null;
		for (int i = timeEntrys.size() - 1; i > 0; i--) {
			vcd = timeEntrys.get(i);
			if (vcd.equalsIgnoreActionId(v)) {
				prevVcd = vcd;
			} else if (prevVcd != null) {
				return prevVcd;
			}
		}
		return prevVcd;
	}

	// public String getScopeType() {
	// return scopeType;
	// }

	@Override
	public boolean containsValue(Pattern searchItem) {
		if (_containsValue(searchItem))
			return true;

		if (checkVal(modifier, searchItem))
			return true;
		if (checkVal(name, searchItem))
			return true;
		// if(checkVal(scopeType,searchItem))return true;
		// if(checkVal(scope,searchItem))return true;

		return false;
	}

	public Long findAssignmentTime(Long actionId) {
		ValueCoreData entry=null,lastEntry=null;
		for(int i=timeEntrys.size()-1;i>=0;i--){
			entry=timeEntrys.get(i);
			if(entry.getActionId()<actionId && lastEntry!=null){
				if(!lastEntry.equalsIgnoreActionId(entry)){
					return lastEntry.getActionId();
				}
			}
			lastEntry=entry;
		}
		return lastEntry==null ? null : lastEntry.getActionId();
	}
}
