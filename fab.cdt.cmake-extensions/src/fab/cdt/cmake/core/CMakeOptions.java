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
		case "binaryDir":
			binaryDir = value;
		case "toolchainFile":
			toolchainFile = value;
		case "cmakeArgs":
			cmakeArgs = value;
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
	private static final Pattern BUILD_TYPE_PATH_PATTERN = Pattern.compile("buildTypes\\[(\\d+)\\]/(.+)");
}
