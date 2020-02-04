package fab.cdt.cmake.ui;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
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
import fab.cdt.cmake.core.CMakeBuildConfigurationOptions;
import fab.cdt.cmake.core.CMakeOptions;
import fab.cdt.cmake.core.CMakeOptionsStore;
import fab.cdt.cmake.core.CMakeTool;

public class CmakeConfigurationsEditor extends EditorPart {

	private final class RemoveConfigurationAction extends Action {
		private final String configurationName;

		private RemoveConfigurationAction(String configurationName) {
			super(null, Activator.getImage("icons/remove.png").get());
			setToolTipText("Remove this Configuration");
			this.configurationName = configurationName;
		}

		@Override
		public void run() {
			removeConfigurationSection(configurationName);
			store.execute((CMakeOptions options) -> {
				CMakeBuildConfigurationOptions[] newBuildConfigurations = new CMakeBuildConfigurationOptions[options.buildConfigurations.length - 1];
				for (int i = 0, j = 0; i < options.buildConfigurations.length; i++) {
					if (!options.buildConfigurations[i].name.equals(configurationName))
						newBuildConfigurations[j++] = options.buildConfigurations[i];
				}
				options.buildConfigurations = newBuildConfigurations;
			});
		}
	}
	private final class SetActiveConfigurationAction extends Action {
		private final String configurationName;

		private SetActiveConfigurationAction(String configurationName) {
			super(null, Activator.getImage("icons/configs.png").get());
			setToolTipText("Set this configuration as active");
			this.configurationName = configurationName;
		}

		@Override
		public void run() {
			try {
				IProjectDescription desc = project.getDescription();
				IBuildConfiguration[] buildConfigs = project.getBuildConfigs();
				List<String> configNames = new ArrayList<>();
				for (IBuildConfiguration buildConfiguration : buildConfigs) {
					configNames.add(buildConfiguration.getName());
				}
				if (!configNames.contains(configurationName)) {
					configNames.add(configurationName);
					desc.setBuildConfigs(configNames.toArray(new String[configNames.size()]));
				}
				desc.setActiveBuildConfig(configurationName);
				project.setDescription(desc, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private File optionsFile;
	private FormToolkit toolkit;
	private ScrolledForm form;
	private CMakeOptionsStore store;

	// map configuration name -> Section in form
	private Map<String, Section> configurationSections = new HashMap<>();
	private Text buildFolderTextControl;
	private IProject project;
	private DebugPane debugPane = new DebugPane();

	public CmakeConfigurationsEditor() {
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

	private void createConfigurationGroup(FormToolkit toolkit, Composite parent, int hspan, CMakeOptions options, int buildConfigIndex) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED | Section.TWISTIE);
		CMakeBuildConfigurationOptions buildConfigurationOptions = options.buildConfigurations[buildConfigIndex];
		configurationSections.put(buildConfigurationOptions.name, section);
		
		// add toolbar with actions
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolBar = toolBarManager.createControl(section);
		toolBarManager.add(new SetActiveConfigurationAction(buildConfigurationOptions.name));
		toolBarManager.add(new RemoveConfigurationAction(buildConfigurationOptions.name));
		Action configAction = new BuildAction(store, buildConfigurationOptions.name, project);
		toolBarManager.add(configAction);
		toolBarManager.update(true); 
		section.setTextClient(toolBar);

		// set up section client
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, hspan, 1));
		section.setText(buildConfigurationOptions.name + " (" + buildConfigurationOptions.buildType + ")");
		Composite client = toolkit.createComposite(section);
		client.setLayout(new GridLayout(2, false));
		section.setClient(client);

		// add fields
		String buildBase = computeBuildDirForConfig(options, buildConfigurationOptions.name);
		Text buildDirText = createTextField(toolkit, client, "Builds in:", buildBase, false, null);
		buildDirText.setData("build_dir");
		createPathField(toolkit, client, "Toolchain file:", buildConfigurationOptions.toolchainFile, "buildConfigurations[" + buildConfigurationOptions.name + "]/toolchainFile");
		createTextField(toolkit, client, "Additional CMake args:", buildConfigurationOptions.cmakeArgs, "buildConfigurations[" + buildConfigurationOptions.name + "]/cmakeArgs");
		//toolkit.paintBordersFor(client);
	}

	private String computeBuildDirForConfig(CMakeOptions options, String configName) {
		return (options.binaryDir == null ? "" : (options.binaryDir.toString() + "/")) + configName.toLowerCase();
	}

	private void openAddBuildConfigurationDialog() {
		NewBuildConfigurationDialog dialog = new NewBuildConfigurationDialog(getSite().getShell());
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
						addBuildConfiguration(dialog.getConfigurationName(), buildType);
						updateBuildConfigurationSections();
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

	private void addBuildConfiguration(String name, String buildType) {
		store.execute((CMakeOptions options) -> {
			CMakeBuildConfigurationOptions buildConfigurationOptions = getBuildConfigurationOptions(options, name);
			if (buildConfigurationOptions == null) {
				CMakeBuildConfigurationOptions[] newBuildConfigurations = Arrays.copyOf(options.buildConfigurations, options.buildConfigurations.length + 1);
				CMakeBuildConfigurationOptions newBuildConfigurationOptions = new CMakeBuildConfigurationOptions();
				newBuildConfigurationOptions.name = name;
				newBuildConfigurationOptions.buildType = buildType;
				newBuildConfigurationOptions.cmakeArgs = "";
				newBuildConfigurations[options.buildConfigurations.length] = newBuildConfigurationOptions;
				options.buildConfigurations = newBuildConfigurations;
			}
		});
	}

	private static CMakeBuildConfigurationOptions getBuildConfigurationOptions(CMakeOptions options, String name) {
		CMakeBuildConfigurationOptions retVal = null;
		for (CMakeBuildConfigurationOptions buildConfigurationOptions : options.buildConfigurations) {
			if (buildConfigurationOptions.name.equals(name))
				retVal = buildConfigurationOptions;
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

		// sash to have main/sub forms
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		Composite mainFormParent = new Composite(sashForm, SWT.NONE);
		mainFormParent.setLayout(new FillLayout());
		Composite subFormParent = new Composite(sashForm, SWT.NONE);
		subFormParent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());
		sashForm.setWeights(new int[] {100, 0});
		
		// the side form (just a text area for now)
		debugPane.createControl(subFormParent);
		debugPane.setProject(project);
		debugPane.setStore(store);

		// the main form
		toolkit = new FormToolkit(mainFormParent.getDisplay());
		form = toolkit.createScrolledForm(mainFormParent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText("CMake Options (cmake " + cMakeVersion + ")");
		Action showDebugPaneAction = new Action("D") {
			@Override
			public void run() {
				int[] weights = sashForm.getWeights();
				if (weights != null && weights.length > 1) {
					int debugWeight = weights[1];
					if (debugWeight > 0) {
						// hide
						weights[1] = 0;
					} else {
						// show
						weights[0] = 50;
						weights[1] = 50;
					}
					sashForm.setWeights(weights);
				}
			}
		};
		showDebugPaneAction.setToolTipText("Show/Hide debug pane");
		Action addBuildConfigurationAction = new Action("+") {
			@Override
			public void run() {
				openAddBuildConfigurationDialog();
			}
		};
		addBuildConfigurationAction.setToolTipText("Add a new build configuration");
		form.getToolBarManager().add(showDebugPaneAction);
		form.getToolBarManager().add(addBuildConfigurationAction);
		form.updateToolBar();
		Composite body = form.getBody();
		GridLayout bodyLayout = new GridLayout(3, false);
		bodyLayout.verticalSpacing = 8;
		body.setLayout(bodyLayout);
		
		// fill the configuration form from the options model
		store.execute((CMakeOptions options) -> {
			createPathField(toolkit, body, "Root CMakeLists.txt:",	options.topLevelCMake, "topLevelCMake");
			buildFolderTextControl = createPathField(toolkit, body, "Build folder:", options.binaryDir, "binaryDir");
			createTextField(toolkit, body, "CMake args:", options.cmakeArgs, "cmakeArgs");

			createAllBuildConfigurationSections(options);
		});
		toolkit.paintBordersFor(body);

		// update each build configuration when the root build folder is changed
		buildFolderTextControl.addModifyListener(e -> {
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
		});
	}

	private void createAllBuildConfigurationSections(CMakeOptions options) {
		Composite body = form.getBody();
		if (options != null && options.buildConfigurations != null)
			for (int i = 0; i < options.buildConfigurations.length; i++) {
				createConfigurationGroup(toolkit, body, 3, options, i);
			}
	}

	private void deleteAllBuildConfigurationsSections() {
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

	private void updateBuildConfigurationSections() {
		Composite body = form.getBody();
		body.setRedraw(false);
		store.execute((CMakeOptions options) -> {
			deleteAllBuildConfigurationsSections();
			createAllBuildConfigurationSections(options);
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
