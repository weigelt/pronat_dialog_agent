package edu.kit.ipd.parse.dialog_agent.evaluation;

import java.nio.file.Path;
import java.nio.file.Paths;

import edu.kit.ipd.parse.dialog_agent.build_graph.BuildGraph;

public class ConditionCorrection {		// evaluation scenario 4

	public static void main(String[] args) {
		Path path = Paths.get("src/main/resources/audiofiles/evaluation/scene4_15.flac");
		new BuildGraph(path, false);
	}

}
