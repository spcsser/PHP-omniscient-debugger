package com.psx.technology.debug.phod.content;

import java.util.regex.Pattern;

import com.psx.technology.debug.phod.content.data.TreeNode;

public class FailureOccurence extends BasicOperation<FailureOccurence> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5271318416325145036L;
	private String name;
	private String message;

	protected FailureOccurence(Number actionId, TreeNode parent) {
		super(actionId, 1, parent);
		// TODO Auto-generated constructor stub
	}

	public int compareTo(FailureOccurence o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Double getTime() {
		return this.getTimestampOnEntry();
	}

	@Override
	public Long getMemoryCommit() {
		return this.getMemorySizeOnEntry();
	}

	@Override
	public boolean containsValue(Pattern searchItem) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setName(String mtd) {
		this.name=mtd;
	}

	public String getName() {
		return name;
	}

	public void setMessage(String message) {
		this.message=message;
	}

	public String getMessage(){
		return this.message;
	}
}
