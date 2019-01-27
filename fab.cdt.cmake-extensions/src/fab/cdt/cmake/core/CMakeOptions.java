package fab.cdt.cmake.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of a File that stores configurations
 * on how to launch CMake. 
 * 
 * @author fab
 *
 */
public class CMakeOptions {
	public String topLevelCMake;
	public String binaryDir;
	public String toolchainFile;
	public String cmakeArgs;
	public CMakeBuildTypeOptions[] buildTypes;
	
	public String get(String path) {
		// TODO: use introspection?
		switch (path) {
		case "topLevelCMake":
			return topLevelCMake;
		case "binaryDir":
			return binaryDir;
		case "toolchainFile":
			return toolchainFile;
		case "cmakeArgs":
			return cmakeArgs;
		default:
			Matcher m = BUILD_TYPE_PATH_PATTERN.matcher(path);
			if (m.matches()) {
				int buildTypeIndex = Integer.parseInt(m.group(1));
				if (buildTypeIndex < buildTypes.length) {
					return buildTypes[buildTypeIndex].get(m.group(2));
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
		case "toolchainFile":
			toolchainFile = value;
			break;
		case "cmakeArgs":
			cmakeArgs = value;
			break;
		default:
			Matcher m = BUILD_TYPE_PATH_PATTERN.matcher(path);
			if (m.matches()) {
				int buildTypeIndex = Integer.parseInt(m.group(1));
				if (buildTypeIndex < buildTypes.length) {
					buildTypes[buildTypeIndex].set(m.group(2), value);
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
					isEqual(this.toolchainFile, otherOptions.toolchainFile) &&
					isEqual(this.cmakeArgs, otherOptions.cmakeArgs) &&
					isEqual(this.buildTypes, otherOptions.buildTypes);
		}
		return super.equals(other);
	}

	private static boolean isEqual(String a, String b) {
		return (a == b) || (a != null && a.equals(b));
	}

	private static boolean isEqual(CMakeBuildTypeOptions[] aa, CMakeBuildTypeOptions[] bb) {
		if (aa == null || bb == null || aa.length != bb.length)
			return false;
		for (int i = 0; i < aa.length; i++) {
			if (!isEqual(aa[i].buildType, bb[i].buildType) || !isEqual(aa[i].cmakeArgs, bb[i].cmakeArgs))
				return false;
		}
		return true;
	}

	private static final Pattern BUILD_TYPE_PATH_PATTERN = Pattern.compile("buildTypes\\[(\\d+)\\]/(.+)");
}
