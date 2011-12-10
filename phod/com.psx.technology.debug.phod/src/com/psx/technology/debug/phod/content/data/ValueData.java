package com.psx.technology.debug.phod.content.data;

import java.io.Serializable;
import java.util.Vector;
import java.util.regex.Pattern;

public class ValueData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8734052753641070249L;

	public class ValueCoreData extends AbstractData {

		/**
		 * 
		 */
		private static final long serialVersionUID = 217007122411285356L;
		protected AtomType typ;
		protected Long id;
		protected Long classId;
		protected String name;
		protected String atomValue;// can be any: ValueData, String, Integer,
									// Array or even null

		public ValueCoreData(AbstractData parent, Long id, AtomType typ, Long actionId, String name) {
			this(parent, id, typ, actionId, name, null);
		}

		public ValueCoreData(AbstractData parent, Long id, AtomType typ, Long actionId, String name, String atomValue) {
			super(parent, actionId);
			if (name == null && (AtomType.Object.equals(typ) || AtomType.ArrayField.equals(typ))) {
				throw new IllegalArgumentException("Name must not be null!");
			}
			this.id = id;
			this.typ = typ;
			this.name = name;
			this.atomValue = atomValue;
		}

		public void setParent(TreeNode parent) {
			if (parent instanceof VariableData) {
				this.parent = parent;
			} else {
				super.setParent(parent);
			}
		}

		public AbstractData getCreationVcd() {
			ValueCoreData vcd = null;
			for (ValueCoreData v : timeEntrys) {
				if (v != null && v.equalsIgnoreActionId(this)) {
					vcd=v;
					break;
				}
				vcd = v;
			}
			return vcd;
		}

		public AtomType getType() {
			return typ;
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getString() {
			if (atomValue != null) {
				return atomValue;
			}
			return typ.name() + " : " + (name != null ? name : "");
		}

		public boolean equalsIgnoreActionId(Object object) {
			if (object instanceof ValueCoreData) {
				ValueCoreData vcd = (ValueCoreData) object;
				// if(!compareObjects(actionId,vcd.actionId)) return false;
				if (!compareObjects(atomValue, vcd.atomValue))
					return false;
				if (!compareObjects(id, vcd.getId()))
					return false;
				if (!compareObjects(typ, vcd.getType()))
					return false;
				return true;
			}
			return false;
		}

		protected boolean compareObjects(Object obj1, Object obj2) {
			if (obj1 == null && obj2 == null) {
				return true;
			} else if (obj1 == null || obj2 == null) {
				return false;
			} else {
				return obj1.equals(obj2);
			}
		}

		@Override
		public boolean containsValue(Pattern searchItem) {
			if (_containsValue(searchItem))
				return true;

			if (checkVal(atomValue, searchItem))
				return true;
			if (checkVal(id, searchItem))
				return true;
			if (checkVal(name, searchItem))
				return true;
			if (checkVal(typ, searchItem))
				return true;

			return false;
		}
		
		public Long getClassId() {
			return classId;
		}

		public void setClassId(Long classId) {
			this.classId = classId;
		}
		
		public void setClassId(Number id){
			this.setClassId(id.longValue());
		}
	}

	protected Long id;
	protected Vector<ValueCoreData> timeEntrys;

	public ValueData(AbstractData parent, Long id, AtomType typ, Long actionId, String name, String value) {
		this.id = id;
		this.timeEntrys = new Vector<ValueCoreData>();
		this.addTimeEntry(parent, id, typ, actionId, name, value);
	}

	public ValueCoreData getValueDataForAction(Long action) {
		ValueCoreData vcd = null;
		for (ValueCoreData v: timeEntrys) {
			if (v.getActionId() > action && vcd != null) {
				break;
			}
			vcd = v;
		}
		return vcd;
	}

	public ValueCoreData addTimeEntry(AbstractData parent, Long id, AtomType typ, Long actionId, String name,
			String value) {
		ValueCoreData vcd = new ValueCoreData(parent, id, typ, actionId, name, value);
		timeEntrys.add(vcd);
		return vcd;
	}

	public ValueCoreData getLastValue() {
		return timeEntrys.lastElement();
	}
}