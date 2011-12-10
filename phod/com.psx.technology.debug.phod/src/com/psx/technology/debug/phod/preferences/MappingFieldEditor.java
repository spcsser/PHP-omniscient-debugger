package com.psx.technology.debug.phod.preferences;

import java.io.File;

import org.eclipse.swt.widgets.Composite;

import com.psx.technology.debug.phod.Activator;

public class MappingFieldEditor extends TableFieldEditor {
	
	public MappingFieldEditor(String name, String labelText,
			String[] columnNames, int[] columnWidths, Composite parent) {
		super(name, labelText, columnNames, columnWidths, parent);
	}

	@Override
	protected String createList(String[][] items) {
		String input="";
		for(String[] item:items){
			input+=item[0]+"::"+item[1]+";;";
		}
		return input;
	}

	@Override
	protected String[][] parseString(String string) {
		return Activator.parseStorageString(string);
	}
	
	

	@Override
	protected String[] getNewInputObject() {
		return new String[]{"Server path", "local path"};
	}

}
