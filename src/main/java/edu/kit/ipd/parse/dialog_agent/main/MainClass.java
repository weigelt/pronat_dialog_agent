package edu.kit.ipd.parse.dialog_agent.main;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.kit.ipd.parse.dialog_agent.DialogAgent;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
public class MainClass {

	public static void main(String[] args) {
		// path to audio.flac file, in future versions via config
		Path path = Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Sat Dec 10 16:35:41 CET 2016.flac");
		
		// build the graph, in future done by the framework
		BuildGraph bg = new BuildGraph(path);
		bg.buildGraph();
		PrePipelineData ppd = bg.getGraph(); // just works after building the graph
		
		// start dialog agent and give it the graph
		DialogAgent da = new DialogAgent(ppd);
		da.init();
		da.exec();
	}
}
