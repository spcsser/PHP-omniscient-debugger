package com.psx.technology.debug.phod.content.parser;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.psx.technology.debug.phod.content.Assignment;
import com.psx.technology.debug.phod.content.BasicOperation;
import com.psx.technology.debug.phod.content.MethodCall;
import com.psx.technology.debug.phod.content.ProgramCalls;
import com.psx.technology.debug.phod.content.ProgramData;
import com.psx.technology.debug.phod.content.data.AbstractData;
import com.psx.technology.debug.phod.content.data.AtomType;
import com.psx.technology.debug.phod.content.data.Modifier;
import com.psx.technology.debug.phod.content.data.TreeNode;
import com.psx.technology.debug.phod.content.data.VariableData;
import com.psx.technology.debug.phod.content.data.ValueData.ValueCoreData;

public class XDebugParser implements Parser {

	public static final Integer STATE_ENTER = 0;
	public static final Integer STATE_EXIT = 1;
	public static final Integer STATE_ASSIGNMENT = 2;
	public static final Integer STATE_EXCEPTION = 6;

	public static final Long FUNCTION_INTERNAL = 0L;
	public static final Long FUNCTION_USER_DEFINED = 1L;
	public static final String NAMESPACE_SEPARATOR = "\\";
	public static final String OBJECT_OPERATOR = "->";
	public static final String OBJECT_OPERATOR_STATIC = "::";
	
	private JSONParser parser;
	private ProgramCalls prd;
	private ProgramData pd;

	public static XDebugParser createParser() {
		return new XDebugParser();
	}
	
	protected void init(int size){
		parser=new JSONParser();
		prd = new ProgramCalls();
		prd.createCallMap(size);
	}
	
	protected void initData(Long fileLength){
		Integer varTableCap=65536, valTableCap=65536;
		
		pd = new ProgramData(prd, varTableCap, valTableCap);
	}
	
	public ProgramCalls parseCallFile(String fileName, IProgressMonitor monitor) {
		monitor.subTask("Opening file");
		File file = new File(fileName);
		FileReader fis = null;
		try {
			fis = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		if (fis != null) {
			// long bufferSize = 8192; // Default value in BufferedReader
			BufferedReader reader = new BufferedReader(fis);
			// CSVReader reader = new CSVReader(fis, '\t',
			// CSVParser.NULL_CHARACTER, CSVParser.NULL_CHARACTER, 4);
			Integer avgLineLength=131;
			Integer stackSize=((Long)(file.length()/avgLineLength)).intValue();
			Stack<MethodCall> callStack = new Stack<MethodCall>();
			callStack.ensureCapacity(stackSize);
			init(stackSize);
			
			String nextLine;
			Integer actionType;

			JSONObject jsob;
			
			callStack.push(prd.getRootCall());

			monitor.worked(1);

			monitor.subTask("Reading data sets");

			BasicOperation<?> cm = null;
			Number id;
			
			try {
				while ((nextLine = reader.readLine()) != null) {
					if (nextLine.length() < 42) {// need at least 42 signs to be correct
						continue;
					}
					try {
						jsob = (JSONObject)parser.parse(nextLine);
					} catch (ParseException e) {
						System.err.println(nextLine);
						e.printStackTrace();
						continue;
					}
					
					actionType = ((Number) jsob.get("atp")).intValue();
					id = (Number)jsob.get("aid");
					cm = null;
					if(!callStack.isEmpty()){
						cm=callStack.peek();
					}
					if (STATE_ENTER.equals(actionType)) {
						cm = prd.createMethod(id.longValue(), cm);
						callStack.push((MethodCall) cm);
						handleMethodLineData((MethodCall) cm, jsob, actionType);
					} else if (STATE_EXIT.equals(actionType)) {
						cm = prd.getMethodById(id.longValue());
						if (cm != null) {
							callStack.pop();
							handleMethodLineData((MethodCall) cm, jsob, actionType);
						}
					} else if (STATE_ASSIGNMENT.equals(actionType)) {
						cm = prd.createAssignment(id.longValue(), cm);
						handleAssignmentLineData((Assignment) cm, jsob, pd);
					} else if(STATE_EXCEPTION.equals(actionType)){
						cm = prd.createMethod(id.longValue(), cm);
						MethodCall mc=handleExceptionLineData((MethodCall) cm, jsob, actionType);
						while(callStack.peek().getLevel()>mc.getLevel()){
							callStack.pop();
						}
					} else {
						throw new IllegalArgumentException("State is note expected with value " + jsob.get("typ"));
					}

					if (monitor.isCanceled())
						break;
					monitor.worked(1);
				}
				/*
				for (BasicOperation<?> bo : callStack) {
					bo.removeFromParent();
					prd.getCallMap().remove(bo.getActionId());
				}
				*/
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				close(reader);
				close(fis);
				System.out.println("Call file: "+file.length());
				if (callStack != null) {
					callStack.removeAllElements();
				}
			}
			return prd;
		}
		return null;
	}

	protected MethodCall handleExceptionLineData(MethodCall mc, JSONObject jsob, Integer actionType) {
		// 0 Level, 1 ID, 2 Enter(0)/Exit(1), 3 timestamp, 4 memory commit
		mc.setLevel((Number) jsob.get("lvl"));

		mc.setTimestamp((Number) jsob.get("tme"), 0);
		mc.setMemorySize((Number) jsob.get("mem"), 0);
		String[] mtd = splitMethodName((String) jsob.get("nme"));
		mc.setNameArray(mtd);
		mc.setFunctionTypeId(0);
		mc.setUserDefined(0);
		return mc;
	}

	protected void handleAssignmentLineData(Assignment mc, HashMap<?,?> jsob, ProgramData pd) {
		mc.setLevel((Number) jsob.get("lvl"));

		mc.setTimestamp((Number) jsob.get("tme"), 0);
		mc.setMemorySize((Number) jsob.get("mem"), 0);

		mc.setVariableName((String) jsob.get("nme"));
		mc.setFilepath((String) jsob.get("fle"));

		mc.setLineNumber((Number) jsob.get("lne"));
	}

	protected void close(Closeable obj) {
		if (obj != null) {
			try {
				obj.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected MethodCall handleMethodLineData(MethodCall mc, HashMap<?,?> jsob, Integer actionType) {
		// 0 Level, 1 ID, 2 Enter(0)/Exit(1), 3 timestamp, 4 memory commit
		mc.setLevel((Number) jsob.get("lvl"));

		mc.setTimestamp((Number) jsob.get("tme"), actionType);
		mc.setMemorySize((Number) jsob.get("mem"), actionType);

		if (actionType == STATE_ENTER) {
			String[] mtd = splitMethodName((String) jsob.get("nme"));

			// 5 method name, 6 INTERNAL(0)/USERDEFINED(1), 7 filepath, 8 line
			// number, 9 number of arguments
			mc.setUserDefined((Number)jsob.get("ext"));

			mc.setFunctionTypeId((Number) jsob.get("ftp"));
			mc.setNameArray(mtd);
//			mc.setMethodName(mtd[0]);
//			mc.setClassName(mtd[1]);
//			mc.setNamespace(mtd[2]);
			mc.setFilepath((String) jsob.get("fle"));

			mc.setLineNumber((Number) jsob.get("lne"));

			mc.setIncludeFile((String) jsob.get("inc"));
		}
		return mc;
	}

	protected String[] splitMethodName(String mtd) {
		String[] r = new String[3];
		String[] r1 = null;
		if (mtd.indexOf(OBJECT_OPERATOR) >= 0) {
			r1 = mtd.split(OBJECT_OPERATOR);
		} else {
			r1 = mtd.split(OBJECT_OPERATOR_STATIC);
		}
		r[0] = r1[r1.length - 1];
		if (r1.length == 2) {
			int i = r1[0].lastIndexOf(NAMESPACE_SEPARATOR);
			r[1] = r1[0].substring(i + 1);
			r[2] = i != -1 ? r1[0].substring(0, i) : "";
		}
		return r;
	}

	@SuppressWarnings("unchecked")
	public ProgramData parseDataFile(String fileName, IProgressMonitor monitor) {
		monitor.subTask("Opening file");
		File file = new File(fileName);
		initData(file.length());
		FileReader fis = null;
		try {
			fis = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		if (fis != null) {
			// long bufferSize = 8192; // Default value in BufferedReader
			BufferedReader reader = new BufferedReader(fis);

			// CSVReader reader = new CSVReader(fis, '\t',
			// CSVParser.NULL_CHARACTER, CSVParser.NULL_CHARACTER, 4);
			String nextLine;
			JSONObject jsob;
			ArrayList<?> jsa;
			Integer type;
			Number actionId;
			BasicOperation<?> basop;
			Number scope = null;
			String scopeType = "";
			String name = null;
			Number aid=null;
			MethodCall mc = null;
			VariableData vd = null;
			
			
			monitor.worked(1);

			monitor.subTask("Reading data sets");

			try {
				while ((nextLine = reader.readLine()) != null) {
					if(nextLine.length()<30){
						continue;
					}
					try {
						jsob = (JSONObject)parser.parse(nextLine);
					} catch (ParseException e) {
						System.err.println(nextLine);
						e.printStackTrace();
						continue;
					}
					
					actionId = (Number) jsob.get("aid");
					type = ((Number) jsob.get("atp")).intValue();
					basop = prd.getMethodById(actionId.longValue());
					if (basop == null) {
						System.err.println("Skipped id " + actionId+", no method found.");
						continue;
					}
					
					if (jsob.containsKey("id")) {
						scope = (Number) jsob.get("id");
						scopeType = "id";
					} else if (jsob.containsKey("ast")) {
						scope = (Number) jsob.get("ast");
						scopeType = "ast";
					} else if(jsob.containsKey("cid")){
						scope = 0L;
						scopeType = jsob.get("cid").toString();
					}else{
						scope = actionId;
						scopeType = "aid";
					}

					
					switch (type) {
					case 0: // Methodcall
						// object scope (can be null)
						// basop.getDataNode().add();
						mc = (MethodCall) basop;
						vd = null;
						if (mc.isUserDefined()) {
//							vd = new VariableData(basop.getDataNode(), scope, scopeType, "this", actionId, Modifier.This);
							vd = new VariableData(basop.getDataNode(), "this", actionId.longValue(), Modifier.This);
							handleDataJSONObject(vd, jsob.get("obj"), actionId.longValue());
						}
						// arguments (can be empty)
						jsa = (ArrayList<?>) jsob.get("arg");
						if (jsa != null && jsa.size() > 0) {
//							vd = new VariableData(basop.getDataNode(), scope, scopeType, "Arguments", actionId, Modifier.Arguments);
							vd = new VariableData(basop.getDataNode(), "Arguments", actionId.longValue(), Modifier.Arguments);
							handlePropsJSONObject(vd, jsa, actionId.longValue(), scope.longValue(), scopeType);
						}
						break;
					case 1: // Methodreturn
						// return value (can be null)
						aid=actionId;
						if(basop.getChildCount()>0){
							aid=((BasicOperation<?>) basop.getLastChild()).getActionId();
						}
//						vd = new VariableData(basop.getDataNode(), scope, scopeType, "Return", aid, Modifier.Return);
						vd = new VariableData(basop.getDataNode(), "Return", aid.longValue(), Modifier.Return);
						handleDataJSONObject(vd, jsob.get("val"), actionId.longValue());
						break;
					case 2: // Assignment
						name = ((Assignment) basop).getVariableName();
						jsob.put("nme", name);
						if(jsob.get("obj")!=null){
//							vd = new VariableData(basop.getDataNode(), scope, scopeType, "this", actionId, Modifier.This);
							vd = new VariableData(basop.getDataNode(), "this", actionId.longValue(), Modifier.This);
							handleDataJSONObject(vd, jsob.get("obj"), actionId.longValue());
						}
						handlePropJSONObject(basop.getDataNode(), jsob, actionId.longValue(), scope.longValue(), scopeType);
						break;
					default:
						System.err.println("Unexpected actionType: "+type);
					}
					if (monitor.isCanceled())
						break;
					monitor.worked(1);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} finally {
				System.out.println("Data file: "+file.length());
				close(reader);
				close(fis);
				file = null;
			}
			return pd;
		}
		return null;
	}

	/**
	 * Handles a data object, which can contain values as array, object, atom
	 * and unary values as void or null.
	 * 
	 * @param jso
	 * @param parent
	 */
	protected TreeNode handleDataJSONObject(VariableData parent, Object obj, Long id) {
		ValueCoreData node = null;
		if (obj instanceof HashMap<?,?>) {
			HashMap<?,?> jso = (HashMap<?,?>) obj;
			Object type = jso.get("typ");
			if (type==null && jso.containsKey("id")) {
				node = this.pd.getValueData(parent, (Long) jso.get("id"), id);
			} else if (type == null) {
				System.err.println("Type==null: "+jso);
			} else if ("bool".equals(type)) {
				node = this.pd.addValueData(parent, (Long) jso.get("id"),
						AtomType.Boolean, id, null, jso.get("val")
								.toString());
			} else if ("int".equals(type)) {
				node = this.pd.addValueData(parent, (Long) jso.get("id"),
						AtomType.Integer, id, null,
						jso.get("val").toString());
			} else if ("double".equals(type)) {
				node = this.pd
						.addValueData(parent, (Long) jso.get("id"),
								AtomType.Double, id, null, jso.get("val")
										.toString());
			} else if ("string".equals(type)) {
				node = this.pd
						.addValueData(parent, (Long) jso.get("id"),
								AtomType.String, id, null, jso.get("val")
										.toString());
			} else if ("object".equals(type)) {
				Long oid = (Long) jso.get("id");
				if (oid == null) {
					oid = -1L;
				}
				String name=(String) jso.get("nme");
				node = this.pd.addValueData(parent, oid,
						AtomType.Object, id, name, null);
				handlePropsJSONObject(node, (ArrayList<?>) jso.get("val"), id, oid, name);
			} else if ("array".equals(type)) {
				node = this.pd.addValueData(parent, (Long) jso.get("id"),
						AtomType.Array, id, (String) jso.get("nme"), null);
				handleArrayFieldJSONObject(node, (ArrayList<?>) jso.get("val"), (Long) jso.get("id"), id);
			} else if ("resource".equals(type)) {
				node = this.pd
						.addValueData(parent, (Long) jso.get("id"),
								AtomType.Resource, id,
								(String) jso.get("nme"), null);
			} else if ("void".equals(type)) {
				node = this.pd.addValueData(parent, (Long) jso.get("id"),
						AtomType.Void, id, null, "void");
			} else if ("NULL".equals(type)) {
				node = this.pd.addValueData(parent, (Long) jso.get("id"),
						AtomType.Null, id, null, "NULL");
			} else if ("...".equals(type)) {
				node = this.pd.addValueData(parent, (Long) jso.get("id"),
						AtomType.Undefined, id, null, "...");
			} else {
				System.err.println("Undefined: "+jso);
			}
		} else if ("???".equals(obj)) {
			node = this.pd.addValueData(parent, -1L,
					AtomType.Undefined, id, null, "???");
		} else {
			throw new IllegalArgumentException("Unexpected object type was passed as argument: " + obj.getClass()
					+ ". Expected was JOSNObject or JSONArray.");
		}
		if (node != null) {
			parent.addTimeEntry(id, node);
		} else {
			System.err.println("Node null " + id);
		}
		return node;
	}

	protected void handleArrayFieldJSONObject(AbstractData parent, ArrayList<?> jsa, Long scope, Long actionId) {
		if (jsa == null) {
			return;
		}
		HashMap<?,?> jso=null;
		VariableData vd=null;
		for (int i = 0; i < jsa.size(); i++) {
			jso = (HashMap<?,?>) jsa.get(i);
			vd = this.pd.getVariable(parent, scope, "id", (String) jso.get("nme"), actionId, Modifier.ArrayField);
			handleDataJSONObject(vd, jso.get("val"), actionId);
		}
	}

	protected void handlePropsJSONObject(AbstractData parent, ArrayList<?> jsa, Long actionId, Long scope, String scopeType) {
		if (jsa == null || jsa.size()==0) {
			return;
		}
		for (int i = 0; i < jsa.size(); i++) {
			handlePropJSONObject(parent, (HashMap<?,?>) jsa.get(i), actionId, scope, scopeType);
		}
	}

	protected void handlePropJSONObject(AbstractData parent, HashMap<?,?> jso, Long actionId, Long scope, String scopeType) {
		String name = (String) jso.get("nme");

		Modifier mod;
		if(jso.get("mod")==null){
			mod=Modifier.Local;
		}else{
			mod= Modifier.getModifier(jso.get("mod"));
		}
		if (scope == null) {
			if ((scope=(Long)jso.get("ast"))!=null) {
				scopeType="ast";
//				scope = (Long) jso.get("ast");
			} else if ((scope=(Long)jso.get("id"))!=null) {
//				scope = (Long) jso.get("id");
				scopeType="id";
			} else if(jso.containsKey("cid")) {
				scopeType = jso.get("cid").toString();
				scope = 0L;
			}
		}
		VariableData vd = this.pd.getVariable(parent, scope, scopeType, name, actionId, mod);
		handleDataJSONObject(vd, jso.get("val"), actionId);

	}

	protected void handleVarJSONObject(AbstractData parent, HashMap<?,?> jso, Long actionId, Long scope, String scopeType) {
		String name = (String) jso.get("nme");
		Modifier mod;
		if(jso.get("mod")==null){
			mod=Modifier.Local;
		}else{
			mod= Modifier.getModifier(jso.get("mod"));
		}
		if (scope == null) {
			if ((scope=(Long)jso.get("ast"))!=null) {
				scopeType="ast";
//				scope = (Long) jso.get("ast");
			} else if ((scope=(Long)jso.get("id"))!=null) {
//				scope = (Long) jso.get("id");
				scopeType="id";
			} else if(jso.containsKey("cid")) {
				scopeType = jso.get("cid").toString();
				scope = 0L;
			}
		}
		VariableData vd = this.pd.getVariable(parent, scope, scopeType, name, actionId, mod);
		handleDataJSONObject(vd, jso.get("val"), actionId);
		if(jso.containsKey("vvl")){
			handleDataJSONObject(vd, jso.get("vvl"), actionId);
		}
	}
	
}
