package edu.kit.ipd.parse.dialog_agent.util;

import java.nio.file.Path;

import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;
import edu.kit.ipd.parse.dialog_agent.stt.VoiceRecorder;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IGraph;

public final class GainUserAnswer {

	// private constructor to prevent instantiation of an utility class
	private GainUserAnswer() {
	}

	// activates the voice recorder to receive the user answer as a file path
	// and returns the graph of this file by using BuildGraph
	public static IGraph getUserAnswer() {
		VoiceRecorder voiceRecorder = new VoiceRecorder();
		Path pathAnswer = voiceRecorder.getAnswer();
		BuildGraph bg = new BuildGraph(pathAnswer, true);
		IGraph graph = null;
		try {
			graph = bg.getGraph();
		} catch (MissingDataException mde) {
			mde.printStackTrace();
			// write in logger, that answerFile is empty
		}
		return graph;
	}
}
