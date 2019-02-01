package fab.cdt.cmake.core;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {
	private static final String ID = "fab.cdt.cmake-extensions";
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

	public static ImageDescriptor getImage(String key) {
		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(ID, key);
		return descriptor;
	}

	public static IStatus errorStatus(String format, IOException e) {
		return new Status(IStatus.ERROR, getId(), format, e);
	}

}
