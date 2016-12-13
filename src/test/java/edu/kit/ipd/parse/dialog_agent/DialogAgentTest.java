package edu.kit.ipd.parse.dialog_agent;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseNode;
import edu.kit.ipd.parse.luna.graph.ParseNodeType;

public class DialogAgentTest {

	@Ignore
	@Test // the RELIABLE_CONFIDENCE_THRESHOLD has to be 0.8 for this test
	public void testGetMainNodesWithLowConfidence() {
		// call target method
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Sat Dec 10 16:35:41 CET 2016.flac"));
		bg.buildGraph();
		PrePipelineData ppd = bg.getGraph();
		DialogAgent da = new DialogAgent(ppd);	
		da.init();
		da.reliableConfidenceThreshold = 0.8;
		List<INode> lowConfMainNodes = da.getMainNodesWithLowConfidence(ppd);
		List<String> lowConfMainNodesType = new ArrayList<String>();
		for (int i = 0; i < lowConfMainNodes.size(); i++) {
			lowConfMainNodesType.add(lowConfMainNodes.get(i).getAttributeValue("value").toString());
		}
		// create test list
		List<String> testList = new ArrayList<String>();
		testList.add("to");
		testList.add("date");
		testList.add("is");
		// compare
		assertEquals(lowConfMainNodesType, testList);
	}
	
	@Ignore
	@Test // RELIABLE_CONFIDENCE_THRESHOLD = 0.8, MINIMUM_CONFIDENCE_THRESHOLD = 0.1
	public void testDetermineAlternatives() {
		// call target method
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Mon Dec 12 22:36:56 CET 2016.flac"));
		bg.buildGraph();
		PrePipelineData ppd = bg.getGraph();
		DialogAgent da = new DialogAgent(ppd);	
		da.init();
		da.reliableConfidenceThreshold = 0.8;
		da.minimumConfidenceThreshold = 0.1;
//		ParseNodeType pnt = new ParseNodeType("token"); // ParseNodeType implements INodeType
//		ParseNode pn = new ParseNode(pnt);
//		pn.setAttributeValue("value", "bet");
//		List<String> lowConfidenceNodesAlternatives = da.determineAlternatives(pn);
		List<INode> lowConfMainNodes = da.getMainNodesWithLowConfidence(ppd);		
		List<INode> lowConfidenceNodesAlternatives = da.determineAlternatives(lowConfMainNodes.get(0));
		HashSet<String> resultSet = new HashSet<String>();
		for (INode nodes : lowConfidenceNodesAlternatives) {
			resultSet.add(nodes.getAttributeValue("value").toString());
		}
		System.out.println(resultSet);
		// create test set
		HashSet<String> correctSet = new HashSet<String>();
		correctSet.add("bed");
		correctSet.add("bat");
		System.out.println(correctSet);
		// compare
		assertEquals(resultSet, correctSet);
	}

//	@Ignore
	@Test
	public void testOneWordGetAnswer1() {
		// call target method
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Dec 13 12:32:40 CET 2016.flac"));
		bg.buildGraph();
		PrePipelineData ppd = bg.getGraph();
		DialogAgent da = new DialogAgent(ppd);	
		da.init();
		INode iNode = da.getOneWordAnswer(ppd);
		// create test word as a String
		String correctWord = "bank";
		// compare
		assertEquals(iNode.getAttributeValue("value"), correctWord);
	}
	
//	@Ignore
	@Test
	public void testOneWordGetAnswer2() {
		// call target method
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Mon Dec 12 22:36:56 CET 2016.flac"));
		bg.buildGraph();
		PrePipelineData ppd = bg.getGraph();
		DialogAgent da = new DialogAgent(ppd);	
		da.init();
		INode iNode = da.getOneWordAnswer(ppd);
		// assert condition
		assertNull(iNode);
	}
}
