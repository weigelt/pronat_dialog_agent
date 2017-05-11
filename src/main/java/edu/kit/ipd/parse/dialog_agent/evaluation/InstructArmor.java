package edu.kit.ipd.parse.dialog_agent.evaluation;

import java.nio.file.Path;

import edu.kit.ipd.parse.dialog_agent.build_graph.BuildGraph;
import edu.kit.ipd.parse.dialog_agent.stt.VoiceRecorder;

public class InstructArmor {	// evaluation scenario 8

	public static void main(String[] args) {
		VoiceRecorder voiceRecorder = new VoiceRecorder();
		Path path = voiceRecorder.getAnswer();
		new BuildGraph(path, false);
	}

}
