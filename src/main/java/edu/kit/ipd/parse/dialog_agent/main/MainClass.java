package edu.kit.ipd.parse.dialog_agent.main;

import java.nio.file.Path;
import java.nio.file.Paths;

import edu.kit.ipd.parse.dialog_agent.DialogAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
public class MainClass {

	public static void main(String[] args) {
		// path to audio.flac file, in future versions via config
//		Path path = Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 08:16:01 CET 2017.flac");
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/s7p01.flac");
//		Path path = Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 31 17:20:30 CET 2017.flac");
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.03.11_22:02:34:119answer.flac");
		
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
		
		

//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/s6p02.flac");   // coref third best szenario but no coref!!! 
		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/s7p07.flac");   // coref second best szenario but no coref!!! 
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/s7p09.flac");   // coref best szenario but no coref!!! 
		
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/s6p09.flac");   // coref szenario
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/s6p03.flac");   //  
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/s7p01.flac");   // to complex
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/s7p02.flac");   // interesting

//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene4_3.flac");   // asr not good, but one condition is not recognized
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene4_4.flac");   // else is not understood
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene4_5.flac");   // THAT'S IT!!!
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene4_6.flac");   // THAT aswell
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene4_7.flac");   // many asr errors
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene4_9.flac");   // works correct also 10, 12, 13, 17, 18, 19
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene4_11.flac");   // bad asr, but maybe
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene4_14.flac");   // no condition
		
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene4_15.flac");   // THAAAAAAAAAAAAAAAAAATS IT!
		
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene4_16.flac");   // interesting! but then still there!
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_1.flac");   // Else not detected, also in 5, 6, 9
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_2.flac");   // thats maybe it!!!!!!!!!!
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_3.flac");   // no condition and asr crap!!!
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_4.flac");   // no condition detected 7, 12, 17
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_17.flac");   // no condition detected 7, 12, 17
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_8.flac");   // works but not helpful 10, 15, 16, 19
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_11.flac");   // then is not directly after if - but worse asr
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_13.flac");   // not correct but to very difficult to solve ######### if clause
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_14.flac");   // not correct but also very messy
//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_18.flac");   // not correct but if then is still there
		

//		Path path = Paths.get("/Users/Mario/Dialogmanager/speeches/scene5_17.flac");   // not correct but if then is still there
//		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/2017.03.14_9:32:39:469answer.flac");   // not correct but if then is still there
		
		// creates a graph out of a flac file
		new BuildGraph(path, false);
		
//		// start dialog agent and give it the graph
//		DialogAgent da = new DialogAgent(path);
//		da.init();
//		da.exec();
	}	
}
