package fab.cdt.cmake.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CMakeTool {
	public String getVersion() {
		String[] version = {""};
		List<String> cmakeResult = runShortCMake("--version");
		Matcher m = Pattern.compile("cmake version (\\d+\\.\\d+\\.\\d+)").matcher("");
		cmakeResult.forEach((String line) -> {if (m.reset(line).matches()) version[0] = m.group(1);});
		return version[0];
	}

	/**
	 * Run cmake collecting all output in a list of strings, suitable
	 * for short commands like "cmake --version".
	 * 
	 * @param args arguments to cmake
	 * @return the output (including stderr) of cmake
	 */
	private List<String> runShortCMake(String ...args) {
		List<String> out = new ArrayList<>();
		List<String> command = new ArrayList<String>();
		command.add("cmake");
		for (String arg : args) {
			command.add(arg);
		}
		ProcessBuilder pb = new ProcessBuilder(command);
		try {
			Process cmake = pb.start();
			pb.redirectErrorStream(true);
			InputStream stdOut = cmake.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdOut));
			List<String> output = reader.lines().collect(Collectors.toList());
			out.addAll(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}
}
