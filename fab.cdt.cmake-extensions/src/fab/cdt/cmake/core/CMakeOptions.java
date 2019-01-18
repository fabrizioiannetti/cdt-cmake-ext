package fab.cdt.cmake.core;

import java.nio.file.Path;

/**
 * Representation of a File that stores configurations
 * on how to launch CMake. 
 * 
 * @author fab
 *
 */
public class CMakeOptions {
	public Path topLevelCMake;
	public Path binaryDir;
	public Path toolchainFile;
	public String cmakeArgs;
	public CMakeBuildTypeOptions[] buildTypes;
}
