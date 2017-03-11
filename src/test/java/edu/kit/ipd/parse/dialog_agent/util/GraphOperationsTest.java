package edu.kit.ipd.parse.dialog_agent.util;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import edu.kit.ipd.parse.dialog_agent.DialogAgent;
import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class GraphOperationsTest {

	@Ignore // check if the new confidence is in the correct node
	@Test 
	public void testVerifyNode() {
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Dialogmanager/audio/answerWed Jan 11 14:08:24 CET 2017.flac"));	
		da.init();
		GraphOperations.verifyNode(da.graph, (INode) da.graph.getNodes().toArray()[0]);
		double correctDouble = -1;
		INode iNode = (INode) da.graph.getNodes().toArray()[0];
		correctDouble = Double.parseDouble(iNode.getAttributeValue("asrConfidence").toString()); 
		assertEquals(correctDouble, 1.0, 0.00001);
	}
	
	@Ignore // check if the alternative nodes are deleted
	@Test 
	public void testRemoveAlternativeNodes() {
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Dialogmanager/audio/answerFri Jan 27 15:40:28 CET 2017.flac"), true);
		IGraph graph = null;
		try {
			graph = bg.getGraph();
		} catch (MissingDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Befor node verification we had 18 node afterwards there should be 10
		Object[] array = graph.getNodes().toArray();	
		System.out.println(graph.getNodes().size());
		GraphOperations.removeAlternativeNodes(graph, (INode) array[2]);
		
		System.out.println(graph.getNodes().size());
		assertEquals(graph.getNodes().size(), 10);
	}
	
	@Ignore
	@Test 
	public void testReplaceNode() {
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Dec 13 12:32:40 CET 2016.flac"), true);
		BuildGraph bg1 = new BuildGraph(Paths.get("/Users/Mario/Dialogmanager/audio/answerFri Jan 27 15:40:28 CET 2017.flac"), true);
		IGraph graph = null;
		INode newNode = null;
		INode oldNode = null;
		try {
			graph = bg1.getGraph();
			oldNode = (INode) graph.getNodes().toArray()[2];
			newNode = (INode) bg.getGraph().getNodes().toArray()[0];	
			GraphOperations.replaceNode(graph, oldNode, newNode);		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		INode replacedNode = (INode) graph.getNodes().toArray()[9];
		assertEquals(replacedNode.getAttributeValue("value"), "bank");
	}
}
