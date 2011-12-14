package com.psx.technology.debug.phod.content;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

import com.psx.technology.debug.phod.content.data.AbstractData;
import com.psx.technology.debug.phod.content.data.TreeNode;
import com.psx.technology.debug.phod.content.data.ValueData.ValueCoreData;
import com.psx.technology.debug.phod.content.data.VariableData;

public class ProgramCalls implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1821591003476128336L;
	protected MethodCall callTree = null;
	protected HashMap<Long, BasicOperation<?>> callMap = null;
	private ProgramData programData;
	protected int maxLevel=6;

	public class SearchResult {
		protected BasicOperation<?> operation;
		protected AbstractData data;
		public SearchResult(BasicOperation<?> bo, AbstractData ad){
			this.operation=bo;
			this.data=ad;
		}
		public BasicOperation<?> getBasicOperation(){
			return operation;
		}
		public AbstractData getAbstractData(){
			return data;
		}
	}
	
	public ProgramCalls() {
	}

	public MethodCall getRootCall() {
		if (callTree == null) {
			callTree = new MethodCall(0L);
			callTree.level=0;
		}
		return callTree;
	}

	public HashMap<Long, BasicOperation<?>> getCallMap() {
		if (callMap == null) {
			createCallMap(65536);
		}
		return callMap;
	}
	
	public void createCallMap(int size){
		callMap = new HashMap<Long, BasicOperation<?>>(size);
		callMap.put(getRootCall().getActionId(), getRootCall());
	}

	public BasicOperation<?> getMethodById(Long id) {
		return getCallMap().get(id);
	}

	public MethodCall createMethod(Number id, TreeNode parent) {
		MethodCall mc = new MethodCall(id.longValue(), parent);
		getCallMap().put(id.longValue(), mc);
		return mc;
	}

	public SearchResult searchString(String text, BasicOperation<?> searchStart, IProgressMonitor monitor) {
		SearchResult result = null;
		Pattern pattern=Pattern.compile(text);
		result=searchDepthSiblingOrParent(searchStart, pattern, monitor,true);
		if(result==null){
			result = searchStringParent(searchStart, pattern, monitor);
		}

		return result;
	}

	private SearchResult searchDepthSiblingOrParent(BasicOperation<?> searchStart, final Pattern text, IProgressMonitor monitor,boolean skipSelf) {
		AbstractData data=null;
		//first root
		if(!skipSelf){
			if((data=containsDataValue(searchStart.getDataNode(),text,monitor))!=null){
				return new SearchResult(searchStart, data);
			}
			
			if(searchStart.containsValue(text)){
				return new SearchResult(searchStart, null);
			}
		}
		
		//then children
		if(searchStart.hasChildren()){
			for(TreeNode tn:searchStart.getChildrenVector()){
				SearchResult result=searchDepthSiblingOrParent(((BasicOperation<?>)tn), text, monitor,false);
				if(result!=null){
					return result;
				}
			}
		}
		return null;
	}

	private SearchResult searchStringParent(BasicOperation<?> searchStart, Pattern text, IProgressMonitor monitor) {
		BasicOperation<?> cNode = null, parent = null;
		int index = 0;
		cNode = searchStart;
		parent = (BasicOperation<?>) cNode.getParent();
		TreeNode[] children;
		while (parent != null) {
			children = parent.getChildren();
//			Arrays.sort(children);
			index = Arrays.binarySearch(children, cNode, cNode.getComparator());
			if (index >= 0) {

				for (int i = index + 1; i < children.length; i++) {
					SearchResult result = searchDepthSiblingOrParent((BasicOperation<?>) children[i], text, monitor,false);
					if (result != null) {
						return result;
					}
				}
			}
			cNode = parent;
			parent = (BasicOperation<?>) cNode.getParent();
			
			monitor.worked(1);
			if(monitor.isCanceled()){
				throw new CancellationException("Stopped.");
			}
		}
		return null;
	}

	private AbstractData containsDataValue(AbstractData searchStart, Pattern text, IProgressMonitor monitor) {
		return containsDataValue(searchStart, text, monitor, 0);
	}
	
	private AbstractData containsDataValue(AbstractData searchStart, Pattern text, IProgressMonitor monitor, int level) {
		if (searchStart == null || level>maxLevel) {
			return null;
		}
		AbstractData tmp = null;
		Vector<TreeNode> list=new Vector<TreeNode>();
		if(searchStart instanceof VariableData){
			tmp=((VariableData) searchStart).getValueDataForAction(searchStart.getActionId());
			if(tmp!=null){
				if(tmp.containsValue(text)){
					return searchStart;
				}else{
					list.add(tmp);
				}
			}
		}else{ //if(searchStart instanceof ValueCoreData){
//			list=searchStart.getChildrenVector();
//		}else if(searchStart instanceof AbstractData){
			list=searchStart.getChildrenVector();
		}

		for (int i = 0; i < list.size(); i++) {
			monitor.worked(1);
			if(monitor.isCanceled()){
				throw new CancellationException("Stopped.");
			}
			
			tmp = (AbstractData)list.get(i);
				
			if (tmp.containsValue(text)) {
				if(tmp instanceof VariableData){
					return (VariableData) tmp;
				}else if(tmp instanceof ValueCoreData){
					return (VariableData) tmp.getParent();
				}else{
					return (AbstractData) tmp;
				}
			} else {
				tmp=containsDataValue(tmp, text, monitor, level+1);
				if(tmp!=null){
					return tmp;
				}
			}
			
		}

		return null;
	}

	public Assignment createAssignment(long id, TreeNode peek) {
		Assignment as = new Assignment(id, peek);
		getCallMap().put(as.getActionId(), as);
		return as;
	}

	public void setProgramData(ProgramData programData) {
		this.programData = programData;
	}

	public ProgramData getProgramData() {
		return this.programData;
	}

	public FailureOccurence createFailure(long actionId, BasicOperation<?> cm) {
		FailureOccurence fo=new FailureOccurence(actionId, cm);
		getCallMap().put(fo.getActionId(), fo);
		return fo;
	}
}
