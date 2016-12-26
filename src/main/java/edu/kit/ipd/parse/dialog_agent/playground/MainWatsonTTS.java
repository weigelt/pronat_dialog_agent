package edu.kit.ipd.parse.dialog_agent.playground;

import edu.kit.ipd.parse.dialog_agent.tts.WatsonTTS;

public class MainWatsonTTS {
		
	public static void main(String[] args) {
		WatsonTTS wtts = new WatsonTTS();
		wtts.synthesizeQuestion("Hey Mario. Now we try a different sentence. It is not as bad as I expected ;) The new terms of the ZWS 1.4 are Zero. Hi Armor. Good two.");
	}
}
