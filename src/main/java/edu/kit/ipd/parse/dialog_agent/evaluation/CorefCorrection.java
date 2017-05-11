package edu.kit.ipd.parse.dialog_agent.evaluation;

import java.nio.file.Path;
import java.nio.file.Paths;

import edu.kit.ipd.parse.dialog_agent.build_graph.BuildGraph;

public class CorefCorrection {		// evaluation scenario 6

	public static void main(String[] args) {
		Path path = Paths.get("src/main/resources/audiofiles/evaluation/s6p02.flac"); 		
		new BuildGraph(path, false);
	}
}
