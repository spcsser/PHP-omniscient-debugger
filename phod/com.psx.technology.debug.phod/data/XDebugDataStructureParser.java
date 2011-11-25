package com.psx.technology.debug.c3po.content.parser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.psx.technology.debug.c3po.content.ArrayElementDataStructure;
import com.psx.technology.debug.c3po.content.DataStructure;
import com.psx.technology.debug.c3po.content.DataStructure.AtomType;
import com.psx.technology.debug.c3po.content.FieldDataStructure;
import com.psx.technology.debug.c3po.content.FieldDataStructure.Modifier;
import com.psx.technology.debug.c3po.content.ObjectDataStructure;

public class XDebugDataStructureParser {

	protected JSONParser parser;
	
	protected XDebugDataStructureParser() {
		parser=new JSONParser();
	}
	
	public static XDebugDataStructureParser createParser() {
		return new XDebugDataStructureParser();
	}
	
	public DataStructure parseReturnString(String param){
		FieldDataStructure root=null;
		try {
			Object object=parser.parse(param);
			if(object instanceof JSONObject){
				root=new FieldDataStructure("Return value", AtomType.Undefined);
				handleDataJSONObject(object,root);
			}else if(object instanceof JSONArray){
				root=new FieldDataStructure("Return value", AtomType.Array);
				handleDataJSONObject(object,root);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
		
		return root;
	}
	
	public DataStructure parseParameterString(String[] data){
		FieldDataStructure root=new FieldDataStructure("Parameter List",AtomType.Mixed);
		for(String s:data){
			try {
				parseInternFunction(s, root);
			} catch (ParseException e) {
				if(e.toString().matches("Unexpected token END OF FILE at position \\d+\\.")){
					try {
						parseInternFunction(s+"}",root);
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}else if(e.toString().matches("Unexpected token RIGHT BRACE\\(\\}\\) at position \\d+\\.")){
					try {
						parseInternFunction(s.substring(0, s.length()-1),root);
					} catch (ParseException e1) {
						e1.printStackTrace();
					} catch (NullPointerException e2){
						e2.printStackTrace();
					}
				}else{
					e.printStackTrace();
				}
			} catch (NullPointerException e){
				e.printStackTrace();
			}
		}
		
		return root;
	}
	
	protected void parseInternFunction(String s,FieldDataStructure root) throws ParseException{
		JSONObject jso=(JSONObject)parser.parse(s);
		FieldDataStructure node=new FieldDataStructure(jso.get("name").toString(), Modifier.PARAMETER, AtomType.Mixed, root);
		handleDataJSONObject(jso.get("value"), node);
	}
	
	/**
	 * Handles a data object, which can contain values as array, object, atom and unary values as void or null.
	 * @param jso
	 * @param parent
	 */
	protected void handleDataJSONObject(Object obj, DataStructure parent){
		if(obj instanceof JSONObject){
			JSONObject jso=(JSONObject)obj;
			Object type=jso.get("type");
			if("void".equals(type)){
				parent.setAtomType(AtomType.Void);
				parent.setValue("void");
			}else if("NULL".equals(type)){
				parent.setAtomType(AtomType.Null);
				parent.setValue("NULL");
			}else if("bool".equals(type)){
				parent.setAtomType(AtomType.Boolean);
				parent.setValue(jso.get("value"));
			}else if("int".equals(type)){
				parent.setAtomType(AtomType.Integer);
				parent.setValue(jso.get("value"));
			}else if("double".equals(type)){
				parent.setAtomType(AtomType.Double);
				parent.setValue(jso.get("value"));
			}else if("string".equals(type)){
				parent.setAtomType(AtomType.String);
				parent.setValue(jso.get("value"));
			}else if("object".equals(type)){
				parent.setAtomType(AtomType.Object);
				parent.setValue(jso.get("name"));
				ObjectDataStructure ods=new ObjectDataStructure(jso.get("name").toString(), parent);
				ods.setClassId(jso.get("cid"));
				ods.setObjectId(jso.get("oid"));
				if("...".equals(jso.get("value"))){
					//TODO - what todo?
				}else{
					handlePropsJSONObject((JSONArray)jso.get("value"), ods);
				}
			}else if("array".equals(type)){
				
			}else if("...".equals(type)){
				parent.setAtomType(AtomType.Undefined);
				parent.setValue("...");
			}else if(type==null){
				System.out.println(jso);
			}
		}else if("???".equals(obj)){
			parent.setAtomType(AtomType.Undefined);
			parent.setValue("???");
		}else if(obj instanceof JSONArray){
			parent.setAtomType(AtomType.Array);
			JSONArray jsa=(JSONArray)obj;
			for(Object o:jsa){
				handleArrayFieldJSONObject((JSONObject)o, parent);
			}
		}else if(obj==null){
			//throw new NullPointerException("Object was null, not expected JOSNObject or JSONArray.");
		}else{
			throw new IllegalArgumentException("Unexpected object type was passed as argument: "+obj.getClass()+". Expected was JOSNObject or JSONArray.");
		}
	}
	
	protected void handleArrayFieldJSONObject(JSONObject obj, DataStructure parent){
		ArrayElementDataStructure ds=new ArrayElementDataStructure(AtomType.Undefined, obj.get("name").toString(), "", parent);
		handleDataJSONObject(obj.get("value"), ds);
	}
	
	protected void handlePropsJSONObject(JSONArray jsa, DataStructure parent){
		for(Object o:jsa){
			JSONObject jso=(JSONObject)o;
			FieldDataStructure fds=new FieldDataStructure(jso.get("name").toString(), Modifier.getModifier(jso.get("modifier").toString()), AtomType.Undefined, parent);
			handleDataJSONObject(jso.get("value"), fds);
		}
	}
}
