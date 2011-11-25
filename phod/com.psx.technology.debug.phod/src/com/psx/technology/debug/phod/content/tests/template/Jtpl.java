/*
   Copyright 2009 jtpl.sourceforge.net

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.psx.technology.debug.phod.content.tests.template;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>Jtpl: a very simple template engine for Java</b><br>
 * Contact: <a href="mailto:emmanuel.alliel@gmail.com">emmanuel.alliel@gmail.com</a><br>
 * Web: <a href="http://jtpl.sourceforge.net">http://jtpl.sourceforge.net</a><br>
 * 
 * @version $LastChangedRevision$
 * @author Emmanuel ALLIEL
 * @author Staffan Olsson
 * 
 * <p>
 * Template syntax:<br>
 * &nbsp;&nbsp;&nbsp;Variables:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>{VARIABLE_NAME}</code><br>
 * &nbsp;&nbsp;&nbsp;Blocks:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>&lt;!-- BEGIN: BlockName --&gt;</code><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>&lt;!-- BEGIN: SubBlockName --&gt;</code><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>&lt;!-- END: SubBlockName --&gt;</code><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>&lt;!-- END: BlockName --&gt;</code><br>
 * <p>
 * License: Apache 2.0<br>
*/

public class Jtpl 
{
	private String _varOpen="<\\$";
	private String _varClose="\\$>";
	private String _varOpenNotEsc="<$";
	private String _varCloseNotEsc="$>";
	private String _blockOpen="<!--";
	private String _blockClose="-->";
	
	private HashMap<String,String> blocks = new HashMap<String,String>();
	private HashMap<String,String> parsedBlocks = new HashMap<String,String>();
	private HashMap<String,String> subBlocks = new HashMap<String,String>();
	private HashMap<String,String> vars = new HashMap<String,String>();
	// flag for backwards compatibility, will probably be reomved in future releases
	private boolean failSilently = false;
	private boolean implicitMain = false;
	
	/**
	* Constructs a Jtpl object and reads the template from a file.
	* For backwards compatibility this constructor enables 'failSilently'.
	* @param fileName the <code>file name</code> of the template, exemple: "java/folder/index.tpl"
	* @throws IOException when an i/o error occurs while reading the template.
	*/
	public Jtpl(String fileName) throws IOException
	{
		this(new File(fileName));
		// this is the old constructor so it enables the old (pre-1.3) behavior on errors
		this.failSilently = true;
	}

	/**
	* Constructs a Jtpl object and reads the template from a file.
	* @param file readable file containing template source
	* @throws IOException when an i/o error occurs while reading the template.
	*/
	public Jtpl(File file) throws IOException
	{
		FileReader fr = new FileReader(file);
		String fileText = readFile(fr);
		makeTree(fileText);
	}	
	
	/**
	* Constructs a Jtpl object and reads the template from arbitrary input source.
	* @param template the template source
	* @throws IOException when an i/o error occurs while reading the template.
	*/	
	public Jtpl(Reader template) throws IOException
	{
		String fileText = readFile(template);
		makeTree(fileText);
	}

	public void setVarOpen(String varOpen) {
		this._varOpen = varOpen;
	}

	public void setVarClose(String varClose) {
		this._varClose = varClose;
	}

	public void setBlockOpen(String blockOpen) {
		this._blockOpen = blockOpen;
	}

	public void setBlockClose(String blockClose) {
		this._blockClose = blockClose;
	}

	
	public void setPHPTemplateSeparator(){
		setVarOpen("<\\$");
		setVarClose("\\$>");
		setBlockOpen("<--!");
		setBlockClose("-->");
	}
	
	/**
	* Assign a template variable.
	* For variables that are used in blocks, the variable value
	* must be set before <code>parse</code> is called.
	* @param varName the name of the variable to be set.
	* @param varData the new value of the variable.
	*/
	public void assign(String varName, String varData)
	{
		vars.put(varName, varData);
	}
	
	/**
	* Generates the HTML page and return it into a String.
	*/
	public String out()
	{
		if (this.implicitMain) {
			this.parse("main");
		}
		Object main = parsedBlocks.get("main");
		if (main == null) {
			throw new IllegalStateException("'main' block not parsed");
		}
		return(main.toString());
	}
	
	/**
	* Parse a template block.
	* If the block contains variables, these variables must be set
	* before the block is added.
	* If the block contains subblocks, the subblocks
	* must be parsed before this block.
	* @param blockName the name of the block to be parsed.
	* @throws IllegalArgumentException if the block name is not found (and failSiletly==false)
	*/
	public void parse(String blockName) throws IllegalArgumentException
	{
		String copy = "";
		if (implicitMain && !"main".equals(blockName) && !blockName.startsWith("main.")) {
			blockName = "main." + blockName;
		}
		try {
			copy = blocks.get(blockName).toString();
		} catch (NullPointerException e) {
			if (!this.failSilently) throw new IllegalArgumentException(
					"Block '" + blockName + "' not found." +
							" Matches " + locateBlock(blockName));
		}
		Pattern pattern = Pattern.compile(_varOpen+"([\\w\\.]+)"+_varClose);
		Matcher matcher = pattern.matcher(copy);
		pattern = Pattern.compile("_BLOCK_\\.(.+)");
		for (Matcher matcher2; matcher.find();)
		{
			String match = matcher.group(1);
			matcher2 = pattern.matcher(match);
			if (matcher2.find())
			{
				if (parsedBlocks.containsKey(matcher2.group(1)))
				{
					copy = copy.replaceFirst(_varOpen+match+_varClose, escape(
							parsedBlocks.get(matcher2.group(1)).toString()));
				}
				else
				{
					copy = copy.replaceFirst(_varOpen+match+_varClose, "");
				}
			}
			else
			{
				if (vars.containsKey(match))
				{
					copy = copy.replaceFirst(_varOpen+match+_varClose, escape(
							vars.get(match).toString()));
				}
				else
				{
					// Leave unchanged because it might be wanted in output.
					// Can always be removed by assigning empty value.					
//					copy = copy.replaceFirst("\\{"+match+"\\}", "");
				}
			}
		}
		if (parsedBlocks.containsKey(blockName))
		{
			parsedBlocks.put(blockName, parsedBlocks.get(blockName) + copy);
		}
		else
		{
			parsedBlocks.put(blockName, copy);
		}
		if (subBlocks.containsKey(blockName))
		{
			parsedBlocks.put(subBlocks.get(blockName), "");
		}
	}
	
	/**
	 * Template parsing uses regex replace to insert result text,
	 * which means that special characters in replacement string must be escaped.
	 * @param replacement The text that should appear in output.
	 * @return Text escaped so that it works as String.replaceFirst replacement.
	 */
	protected String escape(String replacement) {
		return replacement.replace("\\", "\\\\").replace("$", "\\$");
	}
	
	/**
	 * Lists the blocks that end with the given blockName.
	 * @param blockName The name as used in parse
	 * @return Blocks where blockName is the child
	 *  (the Set's toString lists the full names)
	 */
	protected Set<String> locateBlock(final String blockName) {
		Set<String> matches = new java.util.HashSet<String>();
		for (Iterator<String> it = blocks.keySet().iterator(); it.hasNext(); ) {
			String b = it.next();
			if (b.toString().endsWith('.' + blockName)) matches.add(b);
		}
		return matches;
	}
	
	private String readFile(Reader fr) throws IOException
	{
		StringBuffer content = new StringBuffer();
		for (int c; (c = fr.read()) != -1; content.append((char)c));
		fr.close();
		return content.toString();
	}
	
	private void makeTree(String fileText)
	{
		// BEGIN: implicit main
		if (!Pattern.compile(".*"+_blockOpen+"\\s*BEGIN\\s*:\\s*main\\s*"+_blockClose+".*", Pattern.DOTALL)
				.matcher(fileText).matches()) {
			this.implicitMain  = true; // affects parse(block) and out()
			fileText = _blockOpen+" BEGIN: main "+_blockClose + fileText + _blockOpen + " END: main "+_blockClose;
		}
		// END: implicit main
		Pattern pattern = Pattern.compile(_blockOpen+"\\s*(BEGIN|END)\\s*:\\s*(\\w+)\\s*"+_blockClose+"(.*?)(?=(?:"+_blockOpen+"\\s*(?:BEGIN|END)\\s*:\\s*\\w+\\s*"+_blockClose+")|(?:\\s*$))", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(fileText);
		ArrayList<String> blockNames = new ArrayList<String>();
		String parentName = "";
		int lastlength = 0; // used in newline trimming to see if one block immediately follows the previous
		while (matcher.find())
		{
			// BEGIN: newline trimming
			String after = matcher.group(3); // contents after tag
			if (lastlength == 0 || fileText.charAt(matcher.start() - 1) == '\n') {
				after = after.replaceFirst("^\\r?\\n", "");
			}
			lastlength = after.length();
			// END: newline trimming
			if (matcher.group(1).toUpperCase().equals("BEGIN"))
			{
				parentName = implode(blockNames);
				blockNames.add(matcher.group(2));
				String currentBlockName = implode(blockNames);
				if (blocks.containsKey(currentBlockName))
				{
					blocks.put(currentBlockName, blocks.get(currentBlockName) + after);
				}
				else
				{
					blocks.put(currentBlockName, after);
				}
				if (blocks.containsKey(parentName))
				{
					blocks.put(parentName, blocks.get(parentName) + _varOpenNotEsc+"_BLOCK_." + currentBlockName +_varCloseNotEsc);
				}
				else
				{
					blocks.put(parentName, _varOpenNotEsc+"_BLOCK_." + currentBlockName + _varCloseNotEsc);
				}
				subBlocks.put(parentName, currentBlockName);
				subBlocks.put(currentBlockName, "");
			}
			else if (matcher.group(1).toUpperCase().equals("END"))
			{
				blockNames.remove(blockNames.size()-1);
				parentName = implode(blockNames);
				if (blocks.containsKey(parentName))
				{
					blocks.put(parentName, blocks.get(parentName) + after);
				}
				else
				{
					blocks.put(parentName, after);
				}
			}
		}
	}
	
	private String implode(ArrayList<String> al)
	{
		String ret = "";
		for (int i = 0; al.size() > i; i++)
		{
			if (i != 0)
			{
				ret += ".";
			}
			ret += al.get(i);
		}
		return (ret);
	}
	
}
