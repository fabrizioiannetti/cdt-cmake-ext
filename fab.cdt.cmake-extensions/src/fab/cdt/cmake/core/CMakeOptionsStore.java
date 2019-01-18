package fab.cdt.cmake.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * Class to manage an instance of {@link CMakeOptions}.
 * 
 * It provides functions to load/store in JSON format the
 * stored {@link CMakeOptions} and can detect if the current state
 * is different to the original (or last saved) state.
 * 
 * @author fab
 *
 */
public class CMakeOptionsStore {
	private CMakeOptions options;
	private CMakeOptions originalOptions;
	private Consumer<CMakeOptions> changeObserver = (CMakeOptions options) -> {};

	public CMakeOptionsStore(Path optionsPath) {
		Optional<CMakeOptions> opts = Optional.empty();
		if (Files.exists(optionsPath)) {
			File file = optionsPath.toFile();
			if (file.length() != 0) {
				try (FileReader reader = new FileReader(file)) {
					Gson gson = new Gson();
					opts = Optional.of(gson.fromJson(reader, CMakeOptions.class));
				} catch (IOException e) {
//				TODO: log
				} catch (JsonSyntaxException e) {
					// the file is not valid, what to do??
					// the file is  empty     -> generate default options
					// the file has a syntax error -> ??
				}
				
			}
		}
		originalOptions = opts.orElse(getDefault());
		options = cloneOptions(originalOptions);
	}

	private CMakeOptions cloneOptions(final CMakeOptions opts) {
		CMakeOptions cloned = new CMakeOptions();
		cloned.topLevelCMake = opts.topLevelCMake;
		cloned.binaryDir = opts.binaryDir;
		cloned.toolchainFile = opts.toolchainFile;
		cloned.cmakeArgs = opts.cmakeArgs;
		cloned.buildTypes = new CMakeBuildTypeOptions[opts.buildTypes.length];
		int i = 0;
		for (CMakeBuildTypeOptions btOpts : opts.buildTypes) {
			CMakeBuildTypeOptions bto = new CMakeBuildTypeOptions();
			bto.buildType = btOpts.buildType;
			bto.cmakeArgs = btOpts.cmakeArgs;
			cloned.buildTypes[i++] = bto;
		}
		return cloned;
	}

	public boolean isChanged() {
		return !isEqual(originalOptions.topLevelCMake, options.topLevelCMake) ||
				!isEqual(originalOptions.binaryDir, options.binaryDir) ||
				!isEqual(originalOptions.toolchainFile, options.toolchainFile) ||
				!isEqual(originalOptions.cmakeArgs, options.cmakeArgs) ||
				!isEqual(originalOptions.buildTypes, options.buildTypes);
	}

	private static boolean isEqual(Object a, Object b) {
		if (a instanceof CMakeBuildTypeOptions[] && a instanceof CMakeBuildTypeOptions[]) {
			CMakeBuildTypeOptions[] aa = (CMakeBuildTypeOptions[]) a;
			CMakeBuildTypeOptions[] bb = (CMakeBuildTypeOptions[]) b;
			if (aa.length != bb.length)
				return false;
			for (int i = 0; i < aa.length; i++) {
				if (!isEqual(aa[i].buildType, bb[i].buildType) || !isEqual(aa[i].cmakeArgs, bb[i].cmakeArgs))
					return false;
			}
			return true;
		}
		return (a == b) || (a != null && a.equals(b));
	}

	private CMakeOptions getDefault() {
		CMakeOptions options = new CMakeOptions();
		options.buildTypes = new CMakeBuildTypeOptions[1];
		options.buildTypes[0] = new CMakeBuildTypeOptions();
		options.buildTypes[0].buildType = "Default";
		options.buildTypes[0].cmakeArgs = "";
		return options;
	}

	public void resetToDefault() {
		options = new CMakeOptions();
		options.buildTypes = new CMakeBuildTypeOptions[1];
		options.buildTypes[0] = new CMakeBuildTypeOptions();
		options.buildTypes[0].buildType = "Default";
		options.buildTypes[0].cmakeArgs = "";
	}

	private void commit() {
		originalOptions = cloneOptions(options);
	}

	public void save(Path optionsPath) {
		try (FileWriter writer = new FileWriter(optionsPath.toFile())) {
			Gson gson = new Gson();
			gson.toJson(options, writer);
			commit();
		} catch (JsonIOException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onChanged(Consumer<CMakeOptions> changeObserver) {
		if (changeObserver != null)
			this.changeObserver  = changeObserver;
		else
			this.changeObserver  = (CMakeOptions options) -> {};
	}

	public void execute(Consumer<CMakeOptions> executor) {
		executor.accept(options);
		if (isChanged())
			changeObserver.accept(options);
	}

}
