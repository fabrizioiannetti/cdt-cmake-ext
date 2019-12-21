package fab.cdt.cmake.ui;

import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;

import fab.cdt.cmake.core.Activator;
import fab.cdt.cmake.core.CMakeOptionsStore;

final public class BuildAction extends Action {
	private CMakeOptionsStore store;
	private String configName;
	private IProject project;

	public BuildAction(CMakeOptionsStore store, String name, IProject project) {
		super(null, Activator.getImage("icons/build_16.png"));
		this.store = store;
		this.configName = name;
		this.project = project;
	}
	@Override
	public void run() {
		if (project != null) {
//			ICBuildConfigurationManager buildConfigurationManager = Activator.getService(ICBuildConfigurationManager.class);
//			buildConfigurationManager.createBuildConfiguration(provider, project, configName, monitor)
//			IBuildConfiguration[] configs = project.getBuildConfigs();
//			
//			project.getDescription().setBuildConfigs(configNames);
		}
	}
}