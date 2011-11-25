package com.psx.technology.debug.phod.content.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.regex.Pattern;

public abstract class TreeNode implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8451514467849886061L;
	protected Vector<TreeNode> children;
	protected TreeNode parent;
	protected Number actionId;
	private static Comparator<TreeNode> comparator;
	protected boolean ordered=false;
	
	public TreeNode(TreeNode parent,Number actionId) {
		setParent(parent);
		children=new Vector<TreeNode>(10,5);
		this.actionId=actionId;
	}
	
	public Comparator<TreeNode> getComparator() {
		if (comparator == null) {
			comparator = new Comparator<TreeNode>() {
				public int compare(TreeNode o1, TreeNode o2) {
					return o1.getActionId().compareTo(o2.getActionId());
				}
			};
		}
		return comparator;
	}
	
	public Vector<TreeNode> getChildrenVector(){
		if(!ordered){
			Collections.sort(children, getComparator());
		}
		return children;
	}
	
	public TreeNode[] getChildren(){
		TreeNode[] ta=new TreeNode[children.size()];
		
		return getChildrenVector().toArray(ta);
	}
	
	public TreeNode getParent(){
		return parent;
	}
	
	public void setParent(TreeNode parent){
		this.parent=parent;
		if(parent!=null){
			parent.add(this);
		}
	}
	
	public void add(TreeNode node){
		children.add(node);
	}
	
	public void removeFromParent() {
		if (getParent() == null || getParent().getChildren() == null) {
			return;
		} else {
			getParent().children.remove(this);
			this.parent=null;
		}
	}
	
	public abstract boolean containsValue(Pattern searchItem);
	
	protected boolean checkVal(Object object, Pattern searchItem){
		return object != null && searchItem.matcher(object.toString()).find();
	}
	
	public boolean isRoot() {
		return parent==null;
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public TreeNode getRoot() {
		TreeNode root=this;
		while(root.getParent()!=null){
			root=(AbstractData)root.getParent();
		}
		return root;
	}
	
	public Long getActionId(){
		return actionId.longValue();
	}
}
