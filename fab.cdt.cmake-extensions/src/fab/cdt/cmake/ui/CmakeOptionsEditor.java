package fab.cdt.cmake.ui;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;

import fab.cdt.cmake.core.Activator;
import fab.cdt.cmake.core.CMakeBuildTypeOptions;
import fab.cdt.cmake.core.CMakeOptions;
import fab.cdt.cmake.core.CMakeOptionsModel;
import fab.cdt.cmake.core.CMakeOptionsStore;
import fab.cdt.cmake.core.CMakeTool;

public class CmakeOptionsEditor extends EditorPart {

	private final class RemoveConfigurationAction extends Action {
		private final String buildType;

		private RemoveConfigurationAction(String buildType) {
			super(null, Activator.getImage("icons/remove.png"));
			setToolTipText("Remove this Configuration");
			this.buildType = buildType;
		}

		@Override
		public void run() {
			removeConfigurationSection(buildType);
			store.execute((CMakeOptions options) -> {
				CMakeBuildTypeOptions[] newBuildTypes = new CMakeBuildTypeOptions[options.buildTypes.length - 1];
				for (int i = 0, j = 0; i < options.buildTypes.length; i++) {
					if (!options.buildTypes[i].buildType.equals(buildType))
						newBuildTypes[j++] = options.buildTypes[i];
				}
				options.buildTypes = newBuildTypes;
			});
		}
	}

	private File optionsFile;
	private FormToolkit toolkit;
	private ScrolledForm form;
	private CMakeOptionsModel model;
	private CMakeOptionsStore store;

	// map buildType (name) -> Section in form
	private Map<String, Section> configurationSections= new HashMap<>();
	private Text buildFolderTextControl;
	private IProject project;

	public CmakeOptionsEditor() {
	}

	public CMakeBuildTypeOptions getOptionsForBuildType(String buildType) {
		CMakeBuildTypeOptions[] buildTypes = model.getOptionsFile(null).buildTypes;
		for (CMakeBuildTypeOptions build : buildTypes) {
			if (build.buildType.equals(buildType))
				return build;
		}
		return null;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		store.save(optionsFile.toPath());
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		if (input instanceof IURIEditorInput) {
			setInput(input);
			IURIEditorInput fileInput = (IURIEditorInput) input;
			URI uri = fileInput.getURI();
			optionsFile = new File(uri);
			model = new CMakeOptionsModel(optionsFile.toPath());
			store = new CMakeOptionsStore(optionsFile.toPath());
			store.onChanged((CMakeOptions options) -> {
				firePropertyChange(PROP_DIRTY);
			});
			setPartName(optionsFile.getName());
			setContentDescription(optionsFile.getPath());
			IFile[] ifiles = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri, 0);
			if (ifiles != null && ifiles.length > 0) {
				project = ifiles[0].getProject();
			}
		}
		if (optionsFile == null) {
			throw new PartInitException("Could not read input:" + input.getName());
		}
	}

	@Override
	public boolean isDirty() {
		return store.isChanged();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	private Text createPathField(FormToolkit toolkit, Composite sectionClient, String label, String path, String optPath) {
		return createTextField(toolkit, sectionClient, label, path, true, optPath);
	}

	private Text createTextField(FormToolkit toolkit, Composite sectionClient, String label, String text, String optPath) {
		return createTextField(toolkit, sectionClient, label, text, true, optPath);
	}

	private Text createTextField(FormToolkit toolkit, Composite sectionClient, String label, String text, boolean editable, String optPath) {
		int textHSpan = ((GridLayout)sectionClient.getLayout()).numColumns;
		if (label != null) {
			Label labelControl = toolkit.createLabel(sectionClient, label);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
			layoutData.minimumHeight = 20;
			labelControl.setLayoutData(layoutData);
			textHSpan--;
		}
		if (text == null)
			text = "";
		Text textControl = toolkit.createText(sectionClient, text);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, textHSpan, 1);
		layoutData.heightHint = 20;
		textControl.setLayoutData(layoutData);
		if (optPath != null) {
			textControl.setData("opt_path", optPath);
			textControl.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (!textControl.isDisposed()) {
						String optPath = (String) textControl.getData("opt_path");
						store.execute(options -> options.set(optPath, textControl.getText()));
					}
				}
			});
		}
		if (!editable)
			textControl.setEditable(editable);
		return textControl;
	}

	private void createConfigurationGroup(FormToolkit toolkit, Composite parent, int hspan, CMakeOptions options, int buildTypeIndex) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED | Section.TWISTIE);
		CMakeBuildTypeOptions buildTypeOptions = options.buildTypes[buildTypeIndex];
		configurationSections.put(buildTypeOptions.name, section);
		
		// add toolbar with actions
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolBar = toolBarManager.createControl(section);
		toolBarManager.add(new RemoveConfigurationAction(buildTypeOptions.name));
		Action configAction = new BuildAction(store, buildTypeOptions.name, project);
		toolBarManager.add(configAction);
		toolBarManager.update(true); 
		section.setTextClient(toolBar);

		// set up section client
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, hspan, 1));
		section.setText(buildTypeOptions.name + " (" + buildTypeOptions.buildType + ")");
		Composite client = toolkit.createComposite(section);
		client.setLayout(new GridLayout(2, false));
		section.setClient(client);

		// add fields
		String buildBase = computeBuildDirForConfig(options, buildTypeOptions.name);
		Text buildDirControl = createTextField(toolkit, client, "Builds in:", buildBase, false, null);
		buildDirControl.setData("build_dir");
		createTextField(toolkit, client, "Additional CMake args:", buildTypeOptions.cmakeArgs, "buildTypes[" + buildTypeOptions.buildType + "]/cmakeArgs");
		//toolkit.paintBordersFor(client);
	}

	private String computeBuildDirForConfig(CMakeOptions options, String configName) {
		return (options.binaryDir == null ? "" : (options.binaryDir.toString() + "/")) + configName.toLowerCase();
	}

	private void openAddBuildTypeDialog() {
		NewBuildTypeDialog dialog = new NewBuildTypeDialog(getSite().getShell());
		dialog.setBlockOnOpen(true);
		if (dialog.open() == ListDialog.OK) {
			Object[] objects = dialog.getResult();
			if (objects != null && objects.length > 0) {
				if (objects[0] instanceof String) {
					String buildType = (String) objects[0];
					if ("-- Custom --".equals(buildType)) {
						buildType = openCustomBuildTypeDialog();
					}
					if (buildTypeIsValid(buildType)) {
						addBuildType(dialog.getConfigName(), buildType);
						updateBuildTypeGroups();
					}
				}
			}
		}
	}

	private boolean buildTypeIsValid(String buildType) {
		return buildType != null &&
			buildType.matches("[a-zA-Z_][0-9a-zA-Z_-]*");
	}

	private String openCustomBuildTypeDialog() {
		String buildType = "";
		InputDialog inputDialog = new InputDialog(
				getSite().getShell(),
				"Add Custom Build Type",
				"Enter the name of the custom build type",
				"",
				newText ->  {
					if (newText == null || newText.isEmpty())
						return "Name cannot be empty";
					if (!buildTypeIsValid(newText.substring(0, 1)))
						return "Name must start with any of: a-z, A-Z, _";
					if (!buildTypeIsValid(newText))
						return "Name can only contain: a-z, A-Z, 0-9, _, -";
					return null;
				});
		inputDialog.setBlockOnOpen(true);
		if (inputDialog.open() == InputDialog.OK) {
			buildType = inputDialog.getValue();
		}
		return buildType;
	}

	
	private void addBuildType(String name, String buildType) {
		store.execute((CMakeOptions options) -> {
			CMakeBuildTypeOptions buildTypeOptions = getBuildTypeOptions(options,buildType);
			if (buildTypeOptions == null) {
				CMakeBuildTypeOptions[] newBuildTypes = Arrays.copyOf(options.buildTypes, options.buildTypes.length + 1);
				CMakeBuildTypeOptions newBuildTypeOptions = new CMakeBuildTypeOptions();
				newBuildTypeOptions.name = name;
				newBuildTypeOptions.buildType = buildType;
				newBuildTypeOptions.cmakeArgs = "";
				newBuildTypes[options.buildTypes.length] = newBuildTypeOptions;
				options.buildTypes = newBuildTypes;
			}
		});
	}

	private static CMakeBuildTypeOptions getBuildTypeOptions(CMakeOptions options, String buildType) {
		CMakeBuildTypeOptions retVal = null;
		for (CMakeBuildTypeOptions buildTypeOptions : options.buildTypes) {
			if (buildTypeOptions.buildType.equals(buildType))
				retVal = buildTypeOptions;
		}
		return retVal;
	}

	private static String getCMakeVersion() {
		String version = new CMakeTool().getVersion();
		if (version.isEmpty())
			version = "?";
		return version;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		String cMakeVersion = getCMakeVersion();
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText("CMake Options (cmake " + cMakeVersion + ")");
		Action dumpAction = new Action("D") {
			@Override
			public void run() {
				if (store != null)
					store.dump();
				else
					System.out.println("Dump: no store");
			}
		};
		dumpAction.setToolTipText("Dump model content to stdout");
		Action addBuildTypeAction = new Action("+") {
			@Override
			public void run() {
				openAddBuildTypeDialog();
			}
		};
		addBuildTypeAction.setToolTipText("Add a new build type section");
		form.getToolBarManager().add(dumpAction);
		form.getToolBarManager().add(addBuildTypeAction);
		form.updateToolBar();
		Composite body = form.getBody();
		GridLayout bodyLayout = new GridLayout(3, false);
		bodyLayout.verticalSpacing = 8;
		body.setLayout(bodyLayout);
		store.execute((CMakeOptions options) -> {
			createPathField(toolkit, body, "Root CMakeLists.txt:",	options.topLevelCMake, "topLevelCMake");
			buildFolderTextControl = createPathField(toolkit, body, "Build folder:", options.binaryDir, "binaryDir");
			createPathField(toolkit, body, "Toolchain file:", options.toolchainFile, "toolchainFile");
			createTextField(toolkit, body, "CMake args:", options.cmakeArgs, "cmakeArgs");

			createAllBuildTypeGroups(options);
		});
		toolkit.paintBordersFor(body);
		
		buildFolderTextControl.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Text widget = (Text) e.widget;
				String text = widget.getText();
				for (String configName : configurationSections.keySet()) {
					Section section = configurationSections.get(configName);
					Composite client = (Composite) section.getClient();
					Control[] children = client.getChildren();
					for (Control control : children) {
						if (control instanceof Text) {
							Text textWidget = (Text) control;
							Object data = textWidget.getData();
							if ("build_dir".equals(data)) {
								textWidget.setText(text + "/" + configName);
							}
						}
					}
				}
			}
		});
	}

	private void createAllBuildTypeGroups(CMakeOptions options) {
		Composite body = form.getBody();
		if (options != null && options.buildTypes != null)
			for (int i = 0; i < options.buildTypes.length; i++) {
				createConfigurationGroup(toolkit, body, 3, options, i);
			}
	}

	private void deleteAllBuildTypeGroups() {
		Composite body = form.getBody();
		Control[] children = body.getChildren();
		for (Control control : children) {
			if (control instanceof Section)
				control.dispose();
		}
		configurationSections.clear();
	}

	private void removeConfigurationSection(String name) {
		Composite body = form.getBody();
		Section section = configurationSections.remove(name);
		if (section != null) {
			section.dispose();
		}
		body.requestLayout();
	}

	private void updateBuildTypeGroups() {
		Composite body = form.getBody();
		body.setRedraw(false);
		store.execute((CMakeOptions options) -> {
			deleteAllBuildTypeGroups();
			createAllBuildTypeGroups(options);
		});
		body.setRedraw(true);
		body.requestLayout();
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

	@Override
	public void dispose() {
		if (toolkit != null)
			toolkit.dispose();
		super.dispose();
	}
}
