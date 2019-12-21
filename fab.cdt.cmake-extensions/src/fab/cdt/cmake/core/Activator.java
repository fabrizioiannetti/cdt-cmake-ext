package fab.cdt.cmake.core;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

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

	/**
	 * Return the OSGi service with the given service interface.
	 * 
	 * @param service
	 *            service interface
	 * @return the specified service or null if it's not registered
	 */
	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

}
