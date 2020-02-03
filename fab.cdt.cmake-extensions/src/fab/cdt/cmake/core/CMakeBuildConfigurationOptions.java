package fab.cdt.cmake.core;

public class CMakeBuildConfigurationOptions {
	public String name;
	public String buildType;
	public String toolchainFile;
	public String cmakeArgs;
	
	public String get(String path) {
		switch (path) {
		case "name":
			return buildType;
		case "buildType":
			return buildType;
		case "toolchainFile":
			return toolchainFile;
		case "cmakeArgs":
			return cmakeArgs;
		default:
			break;
		}
		return "";
	}

	public void set(String path, String value) {
		switch (path) {
		case "name":
			name = value;
			break;
		case "buildType":
			buildType = value;
			break;
		case "toolchainFile":
			toolchainFile = value;
			break;
		case "cmakeArgs":
			cmakeArgs = value;
			break;
		default:
			break;
		}
	}
}