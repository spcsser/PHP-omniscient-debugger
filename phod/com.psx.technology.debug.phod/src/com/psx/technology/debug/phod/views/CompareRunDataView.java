package com.psx.technology.debug.phod.views;

import java.io.File;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IOpenEventListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.psx.technology.debug.phod.Activator;
import com.psx.technology.debug.phod.content.Assignment;
import com.psx.technology.debug.phod.content.BasicOperation;
import com.psx.technology.debug.phod.content.MethodCall;
import com.psx.technology.debug.phod.content.ProgramCalls;
import com.psx.technology.debug.phod.content.ProgramCalls.SearchResult;
import com.psx.technology.debug.phod.content.data.Modifier;
import com.psx.technology.debug.phod.content.data.TreeNode;
import com.psx.technology.debug.phod.content.data.ValueData.ValueCoreData;
import com.psx.technology.debug.phod.content.data.VariableData;
import com.psx.technology.debug.phod.content.parser.PHPFunctionType;
import com.psx.technology.debug.phod.content.parser.XDebugParser;
import com.psx.technology.debug.phod.ui.OpenEditorAtLineAction;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class CompareRunDataView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.psx.technology.debug.phod.views.CompareRunDataView";

	private TreeViewer viewer;
	private Action action1;
	private Action action2;
	private IProgressMonitor monitor;
	private ProgramCalls prd;
	private TreeViewer context;

	private Text content;

	private Text searchText;

	private Action action3;

	private Action action4;

	private Button useRegexButton;

	private Job searchJob;

	private Button actionNaviStepInto;

	private Button actionNaviStepForward;

	private Button actionNaviStepBackward;

	private Button actionNaviStepOut;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class NameSorter extends ViewerSorter {
	}

	public static final String MY_STYLER = "my_styler";

	private static final int NAVI_STEP_OUT = 4;

	private static final int NAVI_STEP_BACKWARD = 3;

	private static final int NAVI_STEP_FORWARD = 2;

	private static final int NAVI_STEP_INTO = 1;

	/**
	 * The constructor.
	 */
	public CompareRunDataView() {
		JFaceResources.getColorRegistry().put(MY_STYLER, new RGB(128, 128, 128));
	}

	/**
	 * view This is a callback that will allow us to create the viewer and
	 * initialize it.
	 */
	public void createPartControl(Composite main) {
		SashForm parent=new SashForm(main, SWT.HORIZONTAL);
		//parent.setLayout(new GridLayout(2, false));

		Group leftParent = new Group(parent, SWT.NONE | SWT.RESIZE);
		leftParent.setText("Execution path");
		leftParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		leftParent.setLayout(new org.eclipse.swt.layout.GridLayout(1, true));

		createNavigationPanel(leftParent);

		viewer = new TreeViewer(leftParent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setContentProvider(new ViewContentProvider());

		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		column.setLabelProvider(new ViewLabelProvider(0));
		column.getColumn().setText("Method");
		column.getColumn().setWidth(350);

		column = new TreeViewerColumn(viewer, SWT.RIGHT, 1);
		column.setLabelProvider(new ViewLabelProvider(1));
		column.getColumn().setText("Timestart");
		column.getColumn().setWidth(100);

		column = new TreeViewerColumn(viewer, SWT.RIGHT, 2);
		column.setLabelProvider(new ViewLabelProvider(2));
		column.getColumn().setText("Memorysize");
		column.getColumn().setWidth(100);

		// viewer.setLabelProvider(new ViewLabelProvider(0));
		// viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		Activator.getDefault().setRemotePath("/var/www/workspace/");
		Activator.getDefault().setLocalPath("D:\\workspace\\php\\");

		OpenStrategy handler = new OpenStrategy(viewer.getControl());
		handler.addOpenListener(new IOpenEventListener() {
			public void handleOpen(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					final BasicOperation<?> mc = (BasicOperation<?>) ((TreeSelection) viewer.getSelection())
							.getFirstElement();
					String path = mc.getFilePath();
					path = path.substring(Activator.getDefault().getRemotePath().length());
					path = Activator.getDefault().getLocalPath() + File.separator + path;
					path = path.replaceAll("([^\\\\])(\\\\)([^\\\\]|)", "$1$2$3");
					path = path.replaceAll("/", "\\\\");
					System.out.println(path);
					String methodName = "";
					PHPFunctionType fType = PHPFunctionType.Unknown;
					if (mc instanceof MethodCall) {
						MethodCall m = ((MethodCall) mc);
						fType = m.getFunctionType();
						if (m.getFunctionType().equals(PHPFunctionType.New)) {
							methodName = m.getClassName() + "()";
						} else {
							methodName = m.getMethodName();
						}
					} else {
						methodName = ((Assignment) mc).getVariableName();
					}

					Action a = createOpenEditorAction(path, mc.getLineNumber(), methodName, fType);
					if (a != null) {
						a.run();
					}
				}
			}
		});

		monitor = Job.getJobManager().createProgressGroup();

		Group searchParent = new Group(parent, SWT.NONE | SWT.RESIZE);
		searchParent.setText("Method Details");
		searchParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		searchParent.setLayout(new org.eclipse.swt.layout.GridLayout(1, true));

		createSearchPanel(searchParent);

		context = new TreeViewer(searchParent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalSpan = 1;
		tree = context.getTree();
		tree.setLayoutData(gd);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		context.setContentProvider(new ViewDataStructureContentProvider());

		column = new TreeViewerColumn(context, SWT.LEFT, 0);
		column.setLabelProvider(new ViewDataStructureLabelProvider((ViewDataStructureContentProvider) context
				.getContentProvider(), 0));
		column.getColumn().setText("Variable");
		column.getColumn().setWidth(400);

		column = new TreeViewerColumn(context, SWT.LEFT, 1);
		column.setLabelProvider(new ViewDataStructureLabelProvider((ViewDataStructureContentProvider) context
				.getContentProvider(), 1));
		column.getColumn().setText("Type");
		column.getColumn().setWidth(300);

		// createDataStructureTree(context.getTree());
		// context.setInput(viewer.getSelection());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			BasicOperation<?> oldNode;

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// udpate the context
				// 1. remove all content
				// 2. if there's a selection add content
				TreeSelection ts = (TreeSelection) event.getSelection();
				if (!ts.isEmpty()) {
					BasicOperation<?> ds = (BasicOperation<?>) ts.getFirstElement();
					if (!ds.equals(oldNode)) {
						context.setInput(ds);
						oldNode = ds;
					}
				} else {

				}
			}
		});

		context.addSelectionChangedListener(new ISelectionChangedListener() {
			TreeNode oldNode = null;

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				TreeSelection ts = (TreeSelection) event.getSelection();
				if (!ts.isEmpty()) {
					if (ts.getFirstElement() instanceof VariableData) {
						VariableData obj = (VariableData) ts.getFirstElement();
						if (obj != null && !obj.equals(oldNode)) {
							ViewDataStructureContentProvider vcp = (ViewDataStructureContentProvider) context
									.getContentProvider();
							Long id = vcp.getActionId() > obj.getActionId() ? vcp.getActionId() : obj.getActionId();
							ValueCoreData vcd = obj.getValueDataForAction(id);
							if (vcd != null) {
								content.setText(vcd.getString());
							} else {
								content.setText("");
							}
							oldNode = obj;
						} else {
							content.setText("");
						}
					} else if (ts.getFirstElement() instanceof ValueCoreData) {
						ValueCoreData obj = (ValueCoreData) ts.getFirstElement();
						content.setText(obj.getString());
						oldNode = obj;
					}
				} else {
					content.setText("");
				}
			}
		});

		content = new Text(searchParent, SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		content.setLayoutData(gd);
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.psx.technology.debug.c3po.viewer");
		makeActions();
		hookContextMenu();
		contributeToActionBars();

	}

	private void createNavigationPanel(Composite leftParent) {
		Group searchPanel = new Group(leftParent, SWT.NONE);
		searchPanel.setText("Navigate");
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.CENTER;
		gd.horizontalSpan = 2;
		searchPanel.setLayoutData(gd);

		searchPanel.setLayout(new org.eclipse.swt.layout.RowLayout());

		makeNavigationActions(searchPanel);

		
	}

	private Image getImageForPath(String path){
		ImageDescriptor id=Activator.getImageDescriptor(path);
		if(id!=null){
			return id.createImage();
		}else{
			return null;
		}
	}
	
	private void makeNavigationActions(Composite searchPanel) {
		
		actionNaviStepForward = new Button(searchPanel, SWT.PUSH|SWT.FLAT);
		//actionNaviStepForward.setText("Step Forward");
		actionNaviStepForward.setToolTipText("Step forward to the next executed method");
		actionNaviStepForward.setImage(getImageForPath("icons/full/elcl16/stepover_co.gif"));
		actionNaviStepForward.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				naviStepForward();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				naviStepForward();
			}
		});
		
		actionNaviStepInto = new Button(searchPanel, SWT.PUSH|SWT.FLAT);
		//actionNaviStepInto.setText("Step Into");
		actionNaviStepInto.setToolTipText("Step into selected method");
		actionNaviStepInto.setImage(getImageForPath("icons/full/elcl16/stepinto_co.gif"));
		actionNaviStepInto.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				naviStepInto();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				naviStepInto();
			}
		});

		actionNaviStepOut = new Button(searchPanel, SWT.PUSH|SWT.FLAT);
		//actionNaviStepOut.setText("Step Out");
		actionNaviStepOut.setToolTipText("Step out of current method");
		actionNaviStepOut.setImage(getImageForPath("icons/full/elcl16/stepreturn_co.gif"));
		actionNaviStepOut.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				naviStepOut();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				naviStepOut();
			}
		});
		
		actionNaviStepBackward = new Button(searchPanel, SWT.PUSH|SWT.FLAT);
		//actionNaviStepBackward.setText("Step Backward");
		actionNaviStepBackward.setToolTipText("Step backward to the previous executed method");
		actionNaviStepBackward.setImage(getImageForPath("icons/full/elcl16/stepback_co.gif"));
		actionNaviStepBackward.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				naviStepBackward();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				naviStepBackward();
			}
		});
	}

	protected void naviStepOut() {
		doNavigationSteps(NAVI_STEP_OUT);
	}

	protected void naviStepBackward() {
		doNavigationSteps(NAVI_STEP_BACKWARD);
	}

	protected void naviStepForward() {
		doNavigationSteps(NAVI_STEP_FORWARD);
	}

	protected void naviStepInto() {
		doNavigationSteps(NAVI_STEP_INTO);
	}

	private void doNavigationSteps(int direction) {
		TreeSelection ts=(TreeSelection)viewer.getSelection();
		if(ts.isEmpty()){
			return;
		}
		TreeNode node=(TreeNode)ts.getFirstElement(), selection=null;
		Vector<TreeNode> children=null;
		int index=0;
		
		if(node.getParent()==null){
			return;
		}
		
		switch (direction) {
		case NAVI_STEP_INTO:
			if(node.hasChildren()){
				selection=node.getChildrenVector().firstElement();
			}
			break;
		case NAVI_STEP_OUT:
			selection=node.getParent();
			break;
		case NAVI_STEP_FORWARD:
			do{
				children=node.getParent().getChildrenVector();
				index=children.indexOf(node);
				index++;
				if(index>=children.size()){
					node=node.getParent();
				}else{
					selection=children.get(index);
				}	
			}while(selection==null && node.getParent() != null);
			break;
		case NAVI_STEP_BACKWARD:
			do{
				children=node.getParent().getChildrenVector();
				index=children.indexOf(node);
				index--;
				if(index<0){
					node=node.getParent();
				}else{
					selection=children.get(index);
				}	
			}while(selection==null && node.getParent() != null);
			break;
		default:
			break;
		}
		if(selection!=null){
			ts=new TreeSelection(new TreePath(new TreeNode[]{selection}));
		
			viewer.setSelection(ts, true);
		}
	}

	private void createSearchPanel(Composite searchParent) {
		Group searchPanel = new Group(searchParent, SWT.NONE);
		searchPanel.setText("Search");
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.CENTER;
		gd.horizontalSpan = 2;
		searchPanel.setLayoutData(gd);

		searchPanel.setLayout(new org.eclipse.swt.layout.RowLayout());

		// Label searchLabel= new Label(searchPanel,SWT.NONE);
		// searchLabel.setText("Search:");
		// gd=new GridData(SWT.LEFT,SWT.FILL,true,true);
		// searchLabel.setData(gd);
		searchText = new Text(searchPanel, SWT.SEARCH | SWT.ICON_SEARCH);
		searchText.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == '\r') {
					performSearch(searchText.getText());
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		RowData rd = new RowData(150, 20);
		searchText.setLayoutData(rd);

		useRegexButton = new Button(searchPanel, SWT.CHECK);
		useRegexButton.setText("Regular Expression");
	}

	protected Job getSearchJob() {
		if (searchJob == null) {
			searchJob = new Job("Search") {
				protected String searchString = null;
				protected ISelection selection = null;

				public void setProperty(QualifiedName key, Object value) {
					searchString = searchText.getText();
					if (!useRegexButton.getSelection()) {
						searchString = ".*" + Matcher.quoteReplacement(searchString) + ".*";
					}
					selection = viewer.getSelection();
					super.setProperty(key, value);
				}

				@Override
				public IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Searching for text: " + searchString, IProgressMonitor.UNKNOWN);
					BasicOperation<?> bo = null;
					SearchResult result = null;

					if (!selection.isEmpty()) {
						bo = (BasicOperation<?>) ((TreeSelection) selection).getFirstElement();
					} else {
						bo = prd.getRootCall();
					}

					try {
						result = prd.searchString(searchString, bo, monitor);
					} catch (CancellationException e) {
					} finally {
						monitor.done();
					}

					if (result != null) {
						StructuredSelection viewerSelect, contextSelect;
						viewerSelect = new StructuredSelection(result.getBasicOperation());
						if (result.getAbstractData() != null) {
							contextSelect = new StructuredSelection(result.getAbstractData());
						} else {
							contextSelect = new StructuredSelection();
						}
						setViewerSelection(viewerSelect, contextSelect);
					} else {
						System.out.println("Item could not be found");
					}
					return Status.OK_STATUS;
				}
			};
		}
		return searchJob;
	}

	protected void performSearch(String text) {
		if (getSearchJob().getState() == Job.NONE) {
			getSearchJob().setProperty(null, null);
			getSearchJob().schedule();
		}
	}

	private void setViewerSelection(final StructuredSelection viewerSelection,
			final StructuredSelection contextSelection) {
		UIJob job = new UIJob("SetSelection") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				viewer.setSelection(viewerSelection, true);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		job = new UIJob("SetSelection") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				context.setSelection(contextSelection, true);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private Action createOpenEditorAction(String path, int lineNo, String methodName, PHPFunctionType fType) {
		try {
			if (lineNo != -1) {
				String fileName = path;
				return new OpenEditorAtLineAction(this, fileName, lineNo, methodName, fType);
			}
		} catch (NumberFormatException e) {
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#contextPopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				if (!context.getSelection().isEmpty()) {
					TreeNode ad = (TreeNode) ((TreeSelection) context.getSelection()).getFirstElement();
					if (ad instanceof VariableData) {
						VariableData vd = (VariableData) ad;
						if (vd.getModifier().compareTo(Modifier.Return) <= 0) {
							boolean isVariable = vd.getModifier().compareTo(Modifier.Local) <= 0
							/* && vd.getParent().getParent() == null */;
							CompareRunDataView.this.fillContextMenu(manager, isVariable);
						}
					} else {
						CompareRunDataView.this.fillContextMenu(manager, false);
					}
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(context.getControl());
		context.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, context);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager, boolean isVariable) {
		if (isVariable) {
			manager.add(action3);
		}
		manager.add(action4);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				performRead();
			}
		};
		action1.setText("Open data");
		action1.setToolTipText("Open data from file");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));

		action2 = new Action() {
			public void run() {
				performEditSettings();
			}
		};
		action2.setText("Settings");
		action2.setToolTipText("Settings");
		action2.setImageDescriptor(Activator.getImageDescriptor("icons/full/etool16/search.gif"));

		action3 = new Action() {
			public void run() {
				findVariableAssignment();
			}
		};
		action3.setText("Where is this value assigned to this variable?");
		action3.setToolTipText("Refresh data");
		action3.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

		action4 = new Action() {
			public void run() {
				findValueCreation();
			}
		};
		action4.setText("Where is this value created?");
		action4.setToolTipText("Refresh data");
		action4.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_LCL_LINKTO_HELP));
	}

	protected void performEditSettings() {
		DirectoryDialog dialog = new DirectoryDialog(viewer.getControl().getShell());
		dialog.setText("Select corresponding local path");
		String workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		dialog.setFilterPath(workspaceRoot);

		final String filename = dialog.open();

		Activator.getDefault().setLocalPath(filename);
	}

	protected void findVariableAssignment() {
		if (!context.getSelection().isEmpty()) {
			TreeNode ad = (TreeNode) ((TreeSelection) context.getSelection()).getFirstElement();
			if (ad instanceof VariableData) {
				VariableData vd = (VariableData) ad;
				// ArrayField
				Long actionId = ((ViewDataStructureContentProvider) context.getContentProvider()).getActionId();
				actionId = vd.findAssignmentTime(actionId);
				// actionId=vcd.getActionId();
				BasicOperation<?> bo = null;
				if (vd.getModifier().compareTo(Modifier.This) <= 0
						&& vd.getModifier().compareTo(Modifier.ArrayField) > 0) {// Not
																					// This
																					// or
																					// Return
																					// or
																					// Parameter
					bo = prd.getMethodById(actionId);
				} else if (vd.getModifier().equals(Modifier.Return)) {
					bo = prd.getMethodById(vd.getParent().getActionId());
				}
				if (bo != null) {
					viewer.setSelection(new StructuredSelection(bo), true);
				}
			} else {
				System.out.println("Not allowed.");
			}
		}
	}

	protected void findValueCreation() {
		// selection not empty
		if (!context.getSelection().isEmpty()) {
			TreeNode ad = (TreeNode) ((TreeSelection) context.getSelection()).getFirstElement();
			if (ad instanceof VariableData) {
				VariableData vd = (VariableData) ad;
				if (vd.getModifier().compareTo(Modifier.Return) <= 0) {// Not
																		// This
																		// or
																		// Return
																		// or
																		// Parameter
					Long actionId = ((ViewDataStructureContentProvider) context.getContentProvider()).getActionId();
					ValueCoreData vcd = vd.getValueDataForAction(actionId > vd.getActionId() ? actionId : vd
							.getActionId());
					BasicOperation<?> bo = prd.getMethodById(vcd.getCreationVcd().getActionId());//
					viewer.setSelection(new StructuredSelection(bo), true);
				}
			} else if (ad instanceof ValueCoreData) {
				ValueCoreData vcd = (ValueCoreData) ad;
				BasicOperation<?> bo = prd.getMethodById(vcd.getCreationVcd().getActionId());
				viewer.setSelection(new StructuredSelection(bo), true);
			}
		}
	}

	protected boolean performRead() {
		FileDialog dialog = new FileDialog(viewer.getControl().getShell());
		dialog.setText("Select file to parse");
		dialog.setFilterExtensions(new String[] { "*.xt" });
		final String filename = dialog.open();

		if (filename != null) {
			Job job = new Job("Reading files") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					final int ticks = IProgressMonitor.UNKNOWN;
					monitor.beginTask("Processing program run data", ticks);
					try {
						prd = readData(filename, monitor);
					} catch (CoreException e) {
						return Status.CANCEL_STATUS;
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			};
			job.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent e) {
					refreshUITree(prd);
					prd.getProgramData().clearData();
				}

				public void aboutToRun(IJobChangeEvent e) {
					clearUI();
				}
			});
			job.setUser(true);
			job.schedule();
			job.setProgressGroup(monitor, 0);

			return true;
		}
		return false;
	}

	protected void clearUI() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				prd = null;
				viewer.getTree().removeAll();
				context.getTree().removeAll();
				System.gc();
			}
		});
	}

	protected void refreshUITree(final ProgramCalls mc) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (mc != null) {
					viewer.setInput(prd);
				}
			}
		});
	}

	protected void refreshUIContext(final BasicOperation<?> mc) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (mc != null) {
					context.setInput(mc);
				}
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	protected ProgramCalls readData(String filename, IProgressMonitor monitor) throws CoreException {
		System.gc(); /*
					 * System.gc(); System.gc(); System.gc(); System.gc();
					 * System.gc(); System.gc(); System.gc(); System.gc();
					 * System.gc(); System.gc(); System.gc(); System.gc();
					 * System.gc(); System.gc(); System.gc();
					 */
		long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long time0 = new Date().getTime();
		XDebugParser xdp = XDebugParser.createParser();
		ProgramCalls pc = xdp.parseCallFile(filename, monitor);

		// Collect current timestamp
		long time1 = new Date().getTime();
		// Run garbage collection, to hide evidence
		System.gc();
		// Calculate memory consumption, after discarding dead objects
		long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		String datafilename = filename.replaceAll("\\.xt$", "_data.xt");
		xdp.parseDataFile(datafilename, monitor);

		long time2 = new Date().getTime();

		xdp = null;
		System.gc(); /*
					 * System.gc(); System.gc(); System.gc(); System.gc();
					 * System.gc(); System.gc(); System.gc(); System.gc();
					 * System.gc(); System.gc(); System.gc(); System.gc();
					 * System.gc(); System.gc(); System.gc();
					 */

		long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		pc.getProgramData().getDataStatistics();
		System.out.println(mem0 + " Byte\t" + mem1 + " Byte\t" + mem2 + " Byte\t\t" + time0 + " ms\t" + time1 + " ms\t"
				+ time2 + " ms");

		return pc;
	}
}