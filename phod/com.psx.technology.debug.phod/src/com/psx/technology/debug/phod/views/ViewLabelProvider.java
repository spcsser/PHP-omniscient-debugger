package com.psx.technology.debug.phod.views;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Hashtable;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.psx.technology.debug.phod.Activator;
import com.psx.technology.debug.phod.content.Assignment;
import com.psx.technology.debug.phod.content.BasicOperation;
import com.psx.technology.debug.phod.content.MethodCall;
import com.psx.technology.debug.phod.content.data.VariableData;
import com.psx.technology.debug.phod.content.parser.PHPFunctionType;

class ViewLabelProvider extends ColumnLabelProvider {

	protected int index;
	protected Hashtable<String, Image> imageTable;
	private NumberFormat doubleNumberFormat;
	private NumberFormat integerNumberFormat;

	public ViewLabelProvider(int index) {
		this.index = index;
		imageTable = new Hashtable<String, Image>();
	}

	public String getText(Object obj) {
		return getColumnText(obj, index);
	}

	public String getColumnText(Object obj, int index) {
		String result = "";
		BasicOperation<?> bo = (BasicOperation<?>) obj;
		switch (index) {
		case 0:
			if (bo instanceof MethodCall) {
				MethodCall mc = (MethodCall) bo;
				if (mc.isUserDefined()) {
					if (mc.getNamespace() != null) {
						result += mc.getNamespace() + "\\";
					}
					if (mc.getClassName() != null && mc.getClassName().length() != 0) {
						result += mc.getClassName();
						switch (mc.getFunctionType()) {
						case StaticMember:
							result += "::";
							break;
						case Member:
							;
						case New:
						case Normal:
						default:
							result += "->";
							break;
						}
					}
					result += mc.getMethodName();
				} else if (mc.getFunctionType().getId() >= PHPFunctionType.Eval.getId()) {
					result = mc.getMethodName() + " " + mc.getIncludeFile();
				} else {
					result = mc.getMethodName();
				}
			} else if (bo instanceof Assignment) {
				Assignment as = (Assignment) bo;
				result = as.getVariableName() + " = ";
			}
			break;
		case 1:

			result = getDoubleNumberFormat().format(bo.getTimestampOnEntry(), new StringBuffer(),
					new FieldPosition(java.text.NumberFormat.Field.DECIMAL_SEPARATOR, 3)).toString();
			break;
		case 2:
			result = getIntegerNumberFormat().format(bo.getMemorySizeOnEntry(), new StringBuffer(),
					new FieldPosition(java.text.NumberFormat.Field.GROUPING_SEPARATOR, 3)).toString();
			break;
		}
		return result;
	}

	protected NumberFormat getDoubleNumberFormat() {
		if (doubleNumberFormat == null) {
			doubleNumberFormat = DecimalFormat.getNumberInstance();
			doubleNumberFormat.setMinimumFractionDigits(3);
		}
		return doubleNumberFormat;
	}

	protected NumberFormat getIntegerNumberFormat() {
		if (integerNumberFormat == null) {
			integerNumberFormat = DecimalFormat.getIntegerInstance();
		}
		return integerNumberFormat;
	}

	@Override
	public String getToolTipText(Object object) {
		String result = "";
		if (object instanceof MethodCall) {
			MethodCall mc = (MethodCall) object;
			if (mc.isUserDefined()) {
				if (mc.getNamespace() != null) {
					result += mc.getNamespace() + "\\";
				}
				if (mc.getClassName() != null && mc.getClassName().length() != 0) {
					result += mc.getClassName();
					switch (mc.getFunctionType()) {
					case StaticMember:
						result += "::";
						break;
					case Member:
						;
					case New:
					case Normal:
					default:
						result += "->";
						break;
					}
				}
				result += mc.getMethodName();
			} else {
				result += mc.getMethodName();
			}
		} else if (object instanceof Assignment) {
			Assignment as = (Assignment) object;
			result += as.getVariableName();
		}
		return result;
	}

	public Image getColumnImage(Object obj, int index) {
		if (index != 0) {
			return null;
		}
		if (obj instanceof BasicOperation) {
			BasicOperation<?> bo = (BasicOperation<?>) obj;
			Image imgd = null;
			if (bo instanceof MethodCall) {
				MethodCall mc = (MethodCall) bo;
				if (mc.isUserDefined()) {
					imgd = getImageDescriptor("icons/full/obj16/phpfunctiondata_pub.gif",
							mc.getFunctionType() == PHPFunctionType.StaticMember ? "icons/full/ovr16/static_co.gif"
									: null);
				} else {
					imgd = getImageDescriptor("icons/full/obj16/phpfunction_internal.gif",
							mc.getFunctionType() == PHPFunctionType.New ? "icons/full/ovr16/constr_ovr.gif" : null);
				}
				return imgd;
				// if (mc.getFunctionType() == PHPFunctionType.StaticMember) {
				// imgd = new OverlayImageDescriptor(imgd,
				// Activator.getImageDescriptor("icons/full/ovr16/static_co.gif"));
				// } else if (mc.getFunctionType() == PHPFunctionType.New) {
				// imgd = new OverlayImageDescriptor(imgd,
				// Activator.getImageDescriptor("icons/full/ovr16/constr_ovr.gif"));
				// }
			} else if (bo instanceof Assignment) {
				VariableData vd = (VariableData) bo.getDataNode().getChildren()[bo.getDataNode().getChildren().length - 1];
				String path, overlay = null;
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
				default:
					path = "icons/full/obj16/warning_obj.gif";
					System.out.println(vd.getModifier());
					break;
				}
				return getImageDescriptor(path, overlay);
			} else {
				return getImageDescriptor("icons/full/obj16/field_default_obj.gif");
			}
		} else {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		}
	}

	private Image getImageDescriptor(String basic) {
		return getImageDescriptor(basic, null);
	}

	private Image getImageDescriptor(String basic, String overlay) {
		if (overlay != null) {
			return new OverlayImageDescriptor(Activator.getImageDescriptor(basic),
					Activator.getImageDescriptor(overlay)).createImage();
		} else {
			return Activator.getImageDescriptor(basic).createImage();
		}
	}

	public Image getImage(Object obj) {
		return getColumnImage(obj, index);
	}
}