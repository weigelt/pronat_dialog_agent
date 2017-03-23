package edu.kit.ipd.parse.dialog_agent.playground;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.kit.ipd.parse.dialog_agent.DialogAgent;
import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;
import edu.kit.ipd.parse.dialog_agent.stt.VoiceRecorder;
import edu.kit.ipd.parse.dialog_agent.tools.AbsolutePath;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.INode;

public class Main {

	public static void main(String[] args) {
//		VoiceRecorder voiceRecorder = new VoiceRecorder();
//		System.out.println(voiceRecorder.getAnswer());
//		Path path = voiceRecorder.getAnswer();
		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.03.23_18:40:33:749answer.flac");
		
		

//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.03.23_15:07:58:351answer.flac"); // case short question
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.03.23_16:01:04:059answer.flac"); // case b -> y && case b -> x,y
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.03.23_16:51:19:676answer.flac"); // case c -> z && case c -> y,z
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.03.23_17:01:25:553answer.flac"); // case a -> x && case a -> w,x
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.03.23_17:06:23:139answer.flac"); // case b,c -> y (1) && case b,c -> y (2)
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.03.23_17:59:10:650answer.flac"); // case a,b -> x (1) (implicit (2))
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.03.23_18:40:33:749answer.flac"); // case c,d -> z (1) (implicit (2))
		new BuildGraph(path, false);
	}

}
