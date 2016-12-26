package edu.kit.ipd.parse.dialog_agent.playground;

import java.io.File;

import edu.kit.ipd.parse.dialog_agent.stt.VoiceRecorder;
import edu.kit.ipd.parse.dialog_agent.tools.AbsolutePath;

public class Main {

	public static void main(String[] args) {
		VoiceRecorder voiceRecorder = new VoiceRecorder();
		System.out.println(voiceRecorder.getAnswer());
	}

}
