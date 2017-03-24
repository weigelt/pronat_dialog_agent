package edu.kit.ipd.parse.dialog_agent.evaluation;

import java.nio.file.Path;
import java.nio.file.Paths;

import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;

public class CorefCorrection {

	public static void main(String[] args) {
		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/s6p02.flac"); 		
		new BuildGraph(path, false);
	}
}
