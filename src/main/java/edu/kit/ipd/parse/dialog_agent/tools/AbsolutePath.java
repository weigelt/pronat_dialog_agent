package edu.kit.ipd.parse.dialog_agent.tools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AbsolutePath {

	private final String userDirectory = System.getProperty("user.home");
	private final String absolutePath;
	
	public AbsolutePath(String relativePath) {
		absolutePath = userDirectory + relativePath;
		checkAvailabilityOfAbsolutePath(absolutePath);
	}
	
	// creates the targetDirectory if it does not exist
	public void checkAvailabilityOfAbsolutePath(String absolutePath) throws DirectoryAccessibilityException {
		File checkPath = new File(absolutePath);
		if (checkPath.exists());
			// directory already exists
		else if (checkPath.mkdirs());
			// directory was created
		else {
			// directory is not accessible
			throw new DirectoryAccessibilityException("Maybe you do not have write access for " + absolutePath);
		}
	}
	
	public String getAbsolutePathDirectory() {
		return absolutePath;
	}
	
	public String getAbsolutePathFile(String fileName, String format) {
		String absolutePathFile = absolutePath + fileName + "." + format;
		return absolutePathFile;
	}
	
	public String getAbsolutePathFileWithTimestamp(String fileName, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_H:mm:ss:SSS");
		Date now = new Date();
		String timestamp = dateFormat.format(now);
		String absolutePathFileWithTimestamp = absolutePath + timestamp + fileName + "." + format;	
		return absolutePathFileWithTimestamp;
	}
}
