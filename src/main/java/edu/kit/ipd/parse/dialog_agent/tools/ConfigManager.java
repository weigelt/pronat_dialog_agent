package edu.kit.ipd.parse.dialog_agent.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class ConfigManager {
	
	static Properties props;
	static Reader reader;
	
	// does automatically refresh the config-file
	public static Properties getConfiguration(Class<?> clazz, String confName) {
		String configPath = "";
		String[] rootPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath().split(File.separator);
		for (int i = 0; i < rootPath.length; i++) {
			if (!rootPath[i].equals("target")) {
				configPath = configPath + rootPath[i] + File.separator;
			} else {
				break; 
			}
		}
		configPath = configPath + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "edu.kit.ipd.parse.dialog_agent." + confName + ".conf";
		props = new Properties();
		try {
			reader = new FileReader(configPath);
			props.load(reader);
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}	
		return props;
	}
	
	public static Properties getConfiguration(Class<?> clazz) {
		String configPath = "";
		String[] rootPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath().split(File.separator);
		for (int i = 0; i < rootPath.length; i++) {
			if (!rootPath[i].equals("target")) {
				configPath = configPath + rootPath[i] + File.separator;
			} else {
				break; 
			}
		}
		String filePath = clazz.getCanonicalName();
		configPath = configPath + "src" + File.separator + "main" + File.separator + "resources" + File.separator + filePath + ".conf";
		props = new Properties();
		try {
			reader = new FileReader(configPath);
			props.load(reader);
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}	
		return props;
	}
}
