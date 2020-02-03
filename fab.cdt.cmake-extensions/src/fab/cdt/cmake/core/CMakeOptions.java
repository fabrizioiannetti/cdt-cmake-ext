package fab.cdt.cmake.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of a File that stores configurations
 * on how to launch CMake, an instance of this type can
 * be loaded/stored using json framework, e.g.
 * 
 * options = gson.fromJson(reader, CMakeOptions.class)
 * gson.toJson(options, writer)
 * 
 * @author fabrizio
 *
 */
public class CMakeOptions {
	public String topLevelCMake;
	public String binaryDir;
	public String cmakeArgs;
	public CMakeBuildConfigurationOptions[] buildConfigurations;
	
	public String get(String path) {
		// TODO: use introspection?
		switch (path) {
		case "topLevelCMake":
			return topLevelCMake;
		case "binaryDir":
			return binaryDir;
		case "cmakeArgs":
			return cmakeArgs;
		default:
			Matcher m = BUILD_TYPE_PATH_PATTERN.matcher(path);
			if (m.matches()) {
				int buildTypeIndex = Integer.parseInt(m.group(1));
				if (buildTypeIndex < buildConfigurations.length) {
					return buildConfigurations[buildTypeIndex].get(m.group(2));
				}
			}
			break;
		}
		return "";
	}
	
	public void set(String path, String value) {
		// TODO: use introspection?
		switch (path) {
		case "topLevelCMake":
			topLevelCMake = value;
			break;
		case "binaryDir":
			binaryDir = value;
			break;
		case "cmakeArgs":
			cmakeArgs = value;
			break;
		default:
			// is this a buildConfigurations[<name>]/<field> pattern ?
			Matcher m = BUILD_TYPE_PATH_PATTERN.matcher(path);
			if (m.matches()) {
				String buildConfigurationName = m.group(1);
				for (int i = 0; i < buildConfigurations.length; i++) {
					if (buildConfigurationName.equals(buildConfigurations[i].name))
						buildConfigurations[i].set(m.group(2), value);
				}
			}
			break;
		}
	}

	public boolean equals(Object other) {
		if (other instanceof CMakeOptions) {
			CMakeOptions otherOptions = (CMakeOptions) other;
			return isEqual(this.topLevelCMake, otherOptions.topLevelCMake) &&
					isEqual(this.binaryDir, otherOptions.binaryDir) &&
					isEqual(this.cmakeArgs, otherOptions.cmakeArgs) &&
					isEqual(this.buildConfigurations, otherOptions.buildConfigurations);
		}
		return super.equals(other);
	}

	private static boolean isEqual(String a, String b) {
		return (a == b) || (a != null && a.equals(b));
	}

	private static boolean isEqual(CMakeBuildConfigurationOptions[] aa, CMakeBuildConfigurationOptions[] bb) {
		if (aa == null || bb == null || aa.length != bb.length)
			return false;
		for (int i = 0; i < aa.length; i++) {
			if (!isEqual(aa[i].buildType, bb[i].buildType) ||
					!isEqual(aa[i].toolchainFile, bb[i].toolchainFile) ||
					!isEqual(aa[i].name, bb[i].name) ||
					!isEqual(aa[i].cmakeArgs, bb[i].cmakeArgs))
				return false;
		}
		return true;
	}

	private static final Pattern BUILD_TYPE_PATH_PATTERN = Pattern.compile("buildConfigurations\\[([\\w-]+)\\]/(.+)");
}
