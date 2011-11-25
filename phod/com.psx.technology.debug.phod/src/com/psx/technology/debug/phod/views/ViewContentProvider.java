package com.psx.technology.debug.phod.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.TreeItem;

import com.psx.technology.debug.phod.content.BasicOperation;
import com.psx.technology.debug.phod.content.ProgramCalls;
import com.psx.technology.debug.phod.content.data.TreeNode;

class ViewContentProvider implements ITreeContentProvider {
	
	public ViewContentProvider() {
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		if(parent instanceof ProgramCalls){
			
			return ((ProgramCalls)parent).getRootCall().getChildren();
		}else{
			return new Object[0];
		}
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		BasicOperation<?> mc = (BasicOperation<?>)parentElement;
		return mc.getChildren();
	}

	@Override
	public Object getParent(Object element) {
		if(element instanceof TreeItem){
			TreeItem ti=(TreeItem)element;
			TreeNode tn=(TreeNode)ti.getData();
			return tn.getParent();
		}else if(element instanceof BasicOperation<?>){
			return ((BasicOperation<?>) element).getParent();
		}else{
			return null;
		}
	}

	@Override
	public boolean hasChildren(Object element) {
		return ((BasicOperation<?>) element).getChildCount() != 0;
	}
}