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
		VoiceRecorder voiceRecorder = new VoiceRecorder();
		System.out.println(voiceRecorder.getAnswer());
		Path path = Paths.get("/Users/Mario/Dialogmanager/audio/answerWed Jan 11 14:36:06 CET 2017.flac");
//		// build the graph, in future done by the framework
//		BuildGraph bg = new BuildGraph(path);
//		bg.buildGraph();
//		PrePipelineData ppd = bg.getGraph(); // just works after building the graph
//		try {
//			System.out.println(ppd.getGraph().showGraph());
//			Object[] array = ppd.getGraph().getNodes().toArray();
//			for (int i = 0; i < array.length; i++) {
//				INode iNode = (INode) array[i];
//				System.out.println(iNode.toString()); // ########
////				System.out.println(iNode.getAllAttributeNamesAndValuesAsPair().toString());
////				System.out.println(iNode.getAttributeValue("asrConfidence"));
//			}
//		} catch (final MissingDataException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
