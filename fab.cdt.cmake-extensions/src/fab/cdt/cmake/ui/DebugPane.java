package fab.cdt.cmake.ui;

import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import fab.cdt.cmake.core.Activator;
import fab.cdt.cmake.core.CMakeOptionsStore;

public class DebugPane {
	
	private IProject project;
	private CMakeOptionsStore store;
	private Text subFormText;
	private ToolBarManager toolBarManager;

	public void createControl(Composite parent) {
		toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.add(createCMakeOptsDumpAction());
		toolBarManager.add(createCConfigDumpAction());
		ToolBar toolBar = toolBarManager.createControl(parent);
		toolBar.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		subFormText = new Text(parent, SWT.NONE | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		subFormText.setText("<empty>");
		Font textFont = JFaceResources.getTextFont();
		subFormText.setFont(textFont);
		subFormText.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	public void setStore(CMakeOptionsStore store) {
		this.store = store;
	}

	private String getActiveBuildconfigName() {
		try {
			return project.getActiveBuildConfig().getName();
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return "";
	}

	private Action createCConfigDumpAction() {
		Action cdtConfigsAction = new Action("C") {
			@Override
			public void run() {
				String text;
				if (project != null) {
					text = "Project: " + project.getName() + " {\n";
					String activeBuildconfigName = getActiveBuildconfigName();
					try {
						ICBuildConfigurationManager service = Activator.getService(ICBuildConfigurationManager.class);
						IBuildConfiguration[] buildConfigs = project.getBuildConfigs();
						for (IBuildConfiguration bc : buildConfigs) {
							text += "  BuildConfig: \"" + bc.getName() + "\" {\n";
							if (activeBuildconfigName.equals(bc.getName())) {
								text += "    active=true\n";
							}
							ICBuildConfiguration cbc = service.getBuildConfiguration(bc);
							if (cbc != null) {
								text += "    CBuildConfig: {\n";
								text += "      class=" + cbc.getClass().getCanonicalName() +"\n";
								text += "      toolchain=" + cbc.getToolChain().getName() + "\n";
								text += "      toolchainId=" + cbc.getToolChain().getId() + "\n";
								text += "      launchMode=" + cbc.getLaunchMode() + "\n";
								Map<String, String> properties = cbc.getProperties();
								for (String key : properties.keySet()) {
									text += "      " + key + "=" + properties.get(key) + "\n";
								}
								text += "    }\n";
							}
							text += "  }\n";
						}
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					text += "}";
				} else {
					text = ("CDT Dump: no project");
				}
				subFormText.setText(text);
			}
		};
		cdtConfigsAction.setToolTipText("Dump cdt configs to sub-form");
		return cdtConfigsAction;
	}


	private IAction createCMakeOptsDumpAction() {
		Action dumpAction = new Action("D") {
			@Override
			public void run() {
				if (store != null) {
					store.dump();
					subFormText.setText(store.dumpToString());
				} else {
					System.out.println("Dump: no store");
				}
			}
		};
		dumpAction.setToolTipText("Dump model content to stdout and sub-form");
		return dumpAction;
	}

}
