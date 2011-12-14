/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids: sdavids@gmx.de bug 37333 Failure Trace cannot
 * 			navigate to non-public class in CU throwing Exception
 *******************************************************************************/
package com.psx.technology.debug.phod.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.psx.technology.debug.phod.content.parser.PHPFunctionType;

/**
 * Open a test in the Java editor and reveal a given line
 */
public class OpenEditorAtLineAction extends OpenEditorAction {

	private int fLineNumber;
	private String fMethodName;
	private PHPFunctionType fType;

	public OpenEditorAtLineAction(ViewPart testRunner, String filePath, int line, String methodName, PHPFunctionType type) {
		super(testRunner, filePath);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, "");
		fLineNumber = line;
		fMethodName = methodName;
		fType = type;
	}

	protected void reveal(ITextEditor textEditor) {
		if (fLineNumber >= 0) {
			try {
				IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
				int index=document.getLineOffset(fLineNumber - 1);
				String s=document.get(index-1, document
						.getLineLength(fLineNumber - 1));
				if(fMethodName!=null){
					int i=s.indexOf(fMethodName)-1;
					if(i<0){
						i=document.getLineLength(fLineNumber-1)-document.getLineDelimiter(fLineNumber-1).length();
						textEditor.selectAndReveal(index, i);
					}else{
						textEditor.selectAndReveal(i+index, fMethodName.length());
					}
				}else{
					textEditor.selectAndReveal(index, document.getLineLength(fLineNumber-1));
				}
			} catch (BadLocationException x) {
				// marker refers to invalid text position -> do nothing
			}
		}
	}
}
