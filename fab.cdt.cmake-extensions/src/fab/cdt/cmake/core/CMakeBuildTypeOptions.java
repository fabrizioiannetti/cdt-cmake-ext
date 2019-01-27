package fab.cdt.cmake.core;

public class CMakeBuildTypeOptions {
	public String buildType;
	public String cmakeArgs;
	
	public String get(String path) {
		switch (path) {
		case "buildType":
			return buildType;
		case "cmakeArgs":
			return cmakeArgs;
		default:
			break;
		}
		return "";
	}

	public void set(String path, String value) {
		switch (path) {
		case "buildType":
			buildType = value;
		case "cmakeArgs":
			cmakeArgs = value;
		default:
			break;
		}
	}
}