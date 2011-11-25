package com.psx.technology.debug.phod.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.psx.technology.debug.phod.Activator;
import com.psx.technology.debug.phod.content.data.AbstractData;
import com.psx.technology.debug.phod.content.data.AtomType;
import com.psx.technology.debug.phod.content.data.Modifier;
import com.psx.technology.debug.phod.content.data.VariableData;
import com.psx.technology.debug.phod.content.data.ValueData.ValueCoreData;

class ViewDataStructureLabelProvider extends ColumnLabelProvider {

	protected ViewDataStructureContentProvider vcp;
	protected int index;
	
	public ViewDataStructureLabelProvider(ViewDataStructureContentProvider vcp,int index){
		this.vcp=vcp;
		this.index=index;
	}

	public Image getImage(Object element) {
		return getColumnImage(element, index);
	}

	public String getText(Object element) {
		return getColumnText(element, index);
	}

	public String getColumnText(Object element, int columnIndex) {
		String sstr=new String();
		if(element instanceof VariableData){
			VariableData vd=(VariableData)element;
			switch(columnIndex){
			case 0:
				sstr+=(vd.getName()==null?"NULL":vd.getName());
				break;
			case 1:
				ValueCoreData vcd=vd.getValueDataForAction(vcp.getActionId()>vd.getActionId()?vcp.getActionId():vd.getActionId());
				if(vcd!=null){
					if(vcd.getType()==AtomType.Object){
						sstr+=vcd.getName();
					}else{
						sstr+=vcd.getString();
					}
					sstr+=" (id = "+vcd.getId()+")";
				}else if(vd.getModifier().ordinal()>Modifier.This.ordinal()){
					sstr+="Unknown";
				}else{
					sstr+="Undefined";
				}
				break;
			default:
				sstr+="undefined";
				break;
			}
			
		} else if(element instanceof ValueCoreData){
			ValueCoreData vcd=(ValueCoreData)element;
			if(vcd.getType().equals(AtomType.Object)){
				sstr+=vcd.getName();
			} else {
				sstr+=vcd.getType().name();
			}
		}
		return sstr;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if(columnIndex!=0){
			return null;
		}
		String path = null, overlay=null;
		if(element instanceof VariableData){
			VariableData vd=(VariableData) element;
			switch (vd.getModifier()) {
			case ArrayField:
				path = "icons/full/obj16/searchm_obj.gif";
				break;
			case Private:
				path = "icons/full/obj16/phpfunctiondata_pri.gif";
				break;
			case Protected:
				path = "icons/full/obj16/phpfunctiondata_pro.gif";
				break;
			case Static:
				overlay = "icons/full/ovr16/static_co.gif";
			case Public:
				path = "icons/full/obj16/phpfunctiondata_pub.gif";
				break;
			case Constant:
				path = "icons/full/obj16/phpfunctiondata_pub.gif";
				break;
			case Public_Static:
				overlay = "icons/full/ovr16/static_co.gif";
				path = "icons/full/obj16/phpfunctiondata_pub.gif";
				break;
			case Protected_Static:
				overlay = "icons/full/ovr16/static_co.gif";
				path = "icons/full/obj16/phpfunctiondata_pro.gif";
				break;
			case Private_Static:
				overlay = "icons/full/ovr16/static_co.gif";
				path = "icons/full/obj16/phpfunctiondata_pri.gif";
				break;
			case Local:
				path = "icons/full/obj16/localvariable_obj.gif";
				break;
			case Arguments:
				path = "icons/full/obj16/func_group.gif";
				break;
			case Return:
				path = "icons/full/obj16/stepreturn_co.gif";
				break;
			case This:
				path = "icons/full/obj16/interest-decrease.gif";
				break;
			default:
				path = "icons/full/obj16/warning_obj.gif";
				break;
			}
		} else if(element instanceof ValueCoreData){
			AbstractData vcd=(AbstractData)element;
			if(vcd.getType().equals(AtomType.Object)){
				path = "icons/full/obj16/phpclassdata.gif";
			}else if(vcd.getType().equals(AtomType.Array)){
				path = "icons/full/obj16/phparraydata.gif";
			}
		} else {
			path = "icons/full/obj16/warning_obj.gif";
		}
		if(path==null){
			path = "icons/full/obj16/warning_obj.gif";
		}
		if (overlay!=null) {
			OverlayImageDescriptor oid=new OverlayImageDescriptor(Activator.getImageDescriptor(path), Activator.getImageDescriptor(overlay));
			return oid.createImage();
		} else {
			ImageDescriptor id=Activator.getImageDescriptor(path);
			if(id!=null){
				return id.createImage();
			}else{
				return null;
			}
		}
	}
}