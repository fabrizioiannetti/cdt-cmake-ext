package fab.cdt.cmake.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.eclipse.cdt.cmake.core.internal.CompileCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

@SuppressWarnings("restriction")
public class CMakeOptionsModel {
	public static final String CMAKEOPTS_JSON = "cmakeopts.json";

	private Path optionsPath;
	private Path cmakeRoot;
	private HashMap<String, CompileCommand> compileCommands = new HashMap<>();
	private CMakeOptions options;

	public CMakeOptionsModel(Path optionsFile) {
		super();
		this.optionsPath = optionsFile;
		loadOptionsFile(optionsFile);
	}

	public void save() {
		try (FileWriter writer = new FileWriter(optionsPath.toFile())) {
			Gson gson = new Gson();
			gson.toJson(options, writer);
		} catch (JsonIOException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public CMakeOptions getOptionsFile(IProgressMonitor monitor) {
		if (options == null)
				loadOptionsFile(cmakeRoot.resolve(CMAKEOPTS_JSON));
		return options;
	}

	public HashMap<String, CompileCommand> getCompileCommands(CMakeBuildTypeOptions optionsConfig) {
		return compileCommands;
	}

	private void initializeWithDefault() {
		options = new CMakeOptions();
		options.buildTypes = new CMakeBuildTypeOptions[1];
		options.buildTypes[0] = new CMakeBuildTypeOptions();
		options.buildTypes[0].buildType = "Default";
		options.buildTypes[0].cmakeArgs = "";
	}
	private void loadOptionsFile(Path optionsPath) {
		if (Files.exists(optionsPath)) {
//			if (monitor != null)
//				monitor.setTaskName("Reading CMake Options file");
			File file = optionsPath.toFile();
			if (file.length() != 0) {
				try (FileReader reader = new FileReader(file)) {
					Gson gson = new Gson();
					options = gson.fromJson(reader, CMakeOptions.class);
				} catch (IOException e) {
//				TODO: log
//				throw new CoreException(Activator.errorStatus(
//						String.format("Reading CMake Options file %s", optionsFile), e));
				} catch (JsonSyntaxException e) {
					// the file is not valid, what to do??
					// the file is be empty     -> generate default options
					// the file has a syntax error -> ??
				}
				
			}
		}

		if (options == null) {
			initializeWithDefault();
		}
	}

	private void loadCompileCommandsFile(Path commandsFile, IProgressMonitor monitor) throws CoreException {
//		Path commandsFile = getBuildDirectory().resolve("compile_commands.json"); //$NON-NLS-1$
		if (Files.exists(commandsFile)) {
			monitor.setTaskName("Reading compile commands");
			try (FileReader reader = new FileReader(commandsFile.toFile())) {
				Gson gson = new Gson();
				CompileCommand[] commands = gson.fromJson(reader, CompileCommand[].class);
				compileCommands.clear();
				for (CompileCommand command : commands) {
					compileCommands.put(command.getFile(), command);
				}
			} catch (IOException e) {
				throw new CoreException(Activator.errorStatus(
						String.format("Reading compile commands %s", commandsFile), e));
			}
		}
	}

}
