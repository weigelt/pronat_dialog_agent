package edu.kit.ipd.parse.dialog_agent.main;

import java.nio.file.Path;
import java.nio.file.Paths;

import edu.kit.ipd.parse.dialog_agent.DialogAgent;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
public class MainClass {

	public static void main(String[] args) {
		// path to audio.flac file, in future versions via config
		Path path = Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 08:16:01 CET 2017.flac");
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/s7p01.flac");
//		Path path = Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 31 17:20:30 CET 2017.flac");
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.17_11:07:50:303answer.flac");
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.17_13:44:15:990answer.flac");
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.17_19:39:22:613answer.flac");
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.17_19:47:45:603answer.flac");
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.22_15:24:32:293answer.flac");   // Manhatten ner
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.22_15:37:57:262answer.flac");   // Paris Hilton, important for ner and context
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.22_15:49:03:724answer.flac");   // Munich, use contextEntity as indicator for ner
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.22_15:54:14:807answer.flac");   // just Paris
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.22_16:34:00:704answer.flac");   // Ali ner person
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.22_16:39:07:416answer.flac");   // ERROOOOOOOOOR Muhammed Ali! - habe ich gesagt!
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.22_16:44:43:929answer.flac");   // Muhammed Ali ner person
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.22_16:53:05:834answer.flac");   // apple computer ner
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.22_19:22:29:094answer.flac");   // paris best football club

//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.24_16:34:51:349answer.flac");   // wsd bank - credit insitute
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.24_18:01:57:771answer.flac");   // contextAnalyzer bar
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.24_18:04:42:580answer.flac");   // contextAnalyzer old bar
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.24_18:10:48:554answer.flac");   // contextAnalyzer bar please is action instead of give

//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.24_22:14:24:637answer.flac");   // coref cup and it
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.24_22:42:44:702answer.flac");   // coref car and pen and it

//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.28_10:23:57:825answer.flac");   // conditionDetector if then
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.02.28_10:45:37:413answer.flac");   // conditionDetector if then not recognized
		
		
		
		// start dialog agent and give it the graph
		DialogAgent da = new DialogAgent(path);
		da.init();
		da.exec();
	}
}
