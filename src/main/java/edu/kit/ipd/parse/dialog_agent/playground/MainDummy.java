package edu.kit.ipd.parse.dialog_agent.playground;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;

public class MainDummy {

	private Properties props;
	public static void main(String[] args) {
		MainDummy md = new MainDummy();
		md.loadConf();
	}

	public void loadConf() {
		props = ConfigManager.getConfiguration(getClass(), "DialogAgent");
		System.out.println(Double.parseDouble(props.getProperty("RELIABLE_ASR_CONFIDENCE_THRESHOLD")));
	}
}
