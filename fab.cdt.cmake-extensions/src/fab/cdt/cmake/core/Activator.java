package fab.cdt.cmake.core;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
	private static Activator plugin = null;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		Activator.plugin = this;
//		bundleContext.registerService(ICMakeToolChainManager.class, new CMakeToolChainManager(), null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		Activator.plugin = null;
	}

	public static String getId() {
		Activator thePlugin = plugin;
		return (thePlugin != null) ?
			thePlugin.getBundle().getSymbolicName() :
			"No Activator ID";
	}

	public static IStatus errorStatus(String format, IOException e) {
		return new Status(IStatus.ERROR, getId(), format, e);
	}

}
