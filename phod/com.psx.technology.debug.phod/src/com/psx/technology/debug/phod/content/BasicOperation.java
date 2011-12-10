package com.psx.technology.debug.phod.content;

import java.security.InvalidParameterException;
import java.util.regex.Pattern;

import com.psx.technology.debug.phod.content.data.AbstractData;
import com.psx.technology.debug.phod.content.data.AtomType;
import com.psx.technology.debug.phod.content.data.TreeNode;

public abstract class BasicOperation<T> extends TreeNode  implements Comparable<BasicOperation<T>>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3910375973920698253L;

	protected Number level;

	protected Number[] timestamp;
	protected Number[] memorySize;

	protected Integer stateCount;
	protected String filePath;
	protected Number lineNumber;
	protected AbstractData dataNode;

	protected BasicOperation(Number actionId, int statecount, TreeNode parent) {
		super(parent,actionId);
		this.stateCount = statecount;
		this.timestamp = new Double[stateCount];
		this.memorySize = new Long[stateCount];
		this.dataNode = new AbstractData(null,actionId) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 537466012285427618L;

			@Override
			public AtomType getType() {
				return AtomType.Undefined;
			}

			@Override
			public boolean containsValue(Pattern searchItem) {
				return false;
			}

		};
	}

	protected void checkSaneState(Number state) {
		if (state == null || state.intValue() >= stateCount || stateCount < 0) {
			throw new InvalidParameterException("The state must be between 0 and " + stateCount + ".");
		}
	}

	public void setLevel(Number level) {
		this.level = level.intValue();
	}

	public void setTimestamp(Number tmp, Integer state) {
		checkSaneState(state);
		timestamp[state.intValue()] = tmp.doubleValue();
	}

	public void setMemorySize(Number tmp, Integer state) {
		checkSaneState(state);
		memorySize[state.intValue()] = tmp.longValue();
	}

	public void setFilepath(String string) {
		filePath = string;
	}

	public void setLineNumber(Number tmp) {
		lineNumber = tmp.intValue();
	}

	public String getFilePath() {
		return filePath;
	}

	public Integer getLineNumber() {
		return lineNumber.intValue();
	}

	public Integer getLevel() {
		return level.intValue();
	}

	public abstract Double getTime();

	public abstract Long getMemoryCommit();
	
	public Double getTimestampOnEntry() {
		return timestamp[0].doubleValue();
	}

	public Long getMemorySizeOnEntry() {
		return memorySize[0].longValue();
	}
	
	public Double getTimestampOnExit() {
		return timestamp[stateCount-1].doubleValue();
	}

	public Long getMemorySizeOnExit() {
		return memorySize[stateCount-1].longValue();
	}

	public int getStateCount() {
		return stateCount;
	}

	public AbstractData getDataNode() {
		return this.dataNode;
	}

	public BasicOperation<?> getLastChild() {
		return (BasicOperation<?>) getChildren()[getChildren().length - 1];
	}

	public int getChildCount() {
		if(getChildren()==null)
			return 0;
		else 
			return getChildren().length;
	}

	@Override
	public int compareTo(BasicOperation<T> o) {
		Long myId=this.getActionId().longValue(),hisId=o.getActionId();
		return hisId.compareTo(myId);
	}
	
}
