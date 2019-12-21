package fab.cdt.cmake.core;

public class CMakeBuildTypeOptions {
	public String name;
	public String buildType;
	public String cmakeArgs;
	
	public String get(String path) {
		switch (path) {
		case "name":
			return buildType;
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
		case "name":
			name = value;
			break;
		case "buildType":
			buildType = value;
			break;
		case "cmakeArgs":
			cmakeArgs = value;
			break;
		default:
			break;
		}
	}
}