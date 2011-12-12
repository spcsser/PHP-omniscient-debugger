package com.psx.technology.debug.phod.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.psx.technology.debug.phod.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class PhodPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	protected MappingFieldEditor mfe=null;
	
	public PhodPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("PHOD Preferences");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		mfe=new MappingFieldEditor(PreferenceConstants.P_MAPPING,"Map server to local Path", new String[]{"Server path","Local path"},new int[]{200,200},getFieldEditorParent());
		addField(mfe);
		/*
		addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
				"&Directory preference:", getFieldEditorParent()));
		
		addField(
			new BooleanFieldEditor(
				PreferenceConstants.P_BOOLEAN,
				"&An example of a boolean preference",
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceConstants.P_CHOICE,
			"An example of a multiple-choice preference",
			1,
			new String[][] { { "&Choice 1", "choice1" }, {
				"C&hoice 2", "choice2" }
		}, getFieldEditorParent()));
		addField(
			new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
			*/
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	@Override
	protected void checkState(){
		super.checkState();
		if(!isValid()){
			setErrorMessage(mfe.getErrorMessage());
			return;
		}
		if(!mfe.isStateSane()){
			setErrorMessage(mfe.getErrorMessage());
			setValid(false);
		}else{
			setErrorMessage(null);
			setValid(true);
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e){
		super.propertyChange(e);
		if(e.getProperty().equals(FieldEditor.VALUE)){
			if(e.getSource() == mfe){
				checkState();
			}
		}
	}
}