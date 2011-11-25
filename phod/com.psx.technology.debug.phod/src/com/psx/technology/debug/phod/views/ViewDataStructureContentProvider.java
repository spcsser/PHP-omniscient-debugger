package com.psx.technology.debug.phod.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.psx.technology.debug.phod.content.BasicOperation;
import com.psx.technology.debug.phod.content.data.AbstractData;
import com.psx.technology.debug.phod.content.data.AtomType;
import com.psx.technology.debug.phod.content.data.TreeNode;
import com.psx.technology.debug.phod.content.data.VariableData;
import com.psx.technology.debug.phod.content.data.ValueData.ValueCoreData;

class ViewDataStructureContentProvider implements ITreeContentProvider {

	private Long actionId;

	public Long getActionId(){
		return actionId;
	}
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		//System.out.println(viewer.toString() + oldInput + newInput);
	}

	@Override
	public Object getParent(Object element) {
		return ((TreeNode) element).getParent();
	}

	@Override
	public boolean hasChildren(Object parentElement) {
		if(parentElement instanceof VariableData){
			VariableData vd=(VariableData)parentElement;
			if(vd.hasChildren()){
				return true;
			}else{
				AbstractData vcd=null;
				vcd=vd.getValueDataForAction(actionId>vd.getActionId()?actionId:vd.getActionId());
				if(vcd==null) return false;
				if(vcd.getType().equals(AtomType.Object) || vcd.getType().equals(AtomType.Array)) return true;
				return vcd.hasChildren();
			}
		} else if(parentElement instanceof ValueCoreData){
			return ((AbstractData)parentElement).hasChildren();
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {

		if (inputElement instanceof BasicOperation<?>) {
			BasicOperation<?> bo=(BasicOperation<?>) inputElement;
			this.actionId=bo.getDataNode().getActionId();
			return bo.getDataNode().getChildren();
		}

		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof VariableData){
			VariableData vd=(VariableData)parentElement;
			if(vd.hasChildren()){
				return vd.getChildren();
			}else{
				AbstractData vcd=null;
				vcd=vd.getValueDataForAction(actionId>vd.getActionId()?actionId:vd.getActionId());
				
				if(vcd.getType().equals(AtomType.Object) || vcd.getType().equals(AtomType.Array)){
					return new Object[]{vcd};
				}else{
					return vcd.getChildren();
				}
			}
		}else if(parentElement instanceof ValueCoreData){
			return ((AbstractData)parentElement).getChildren();
		}
		return null;
	}
}