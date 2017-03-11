package edu.kit.ipd.parse.dialog_agent.util;

import edu.kit.ipd.parse.dialog_agent.tts.WatsonTTS;

public class Synthesizer {

	// private constructor to prevent instantiation of an utility class
	private Synthesizer() {	
	}

	// invokes WatsonTTS to ask the user a question
	public static void enunciateQuestion(String question) {
		WatsonTTS watsonTTS = new WatsonTTS();
		watsonTTS.synthesizeQuestion(question);
	}

}
