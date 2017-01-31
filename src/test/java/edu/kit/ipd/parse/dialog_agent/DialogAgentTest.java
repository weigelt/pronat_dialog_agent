package edu.kit.ipd.parse.dialog_agent;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseNode;
import edu.kit.ipd.parse.luna.graph.ParseNodeType;

public class DialogAgentTest {

	@Ignore
	@Test // the RELIABLE_CONFIDENCE_THRESHOLD has to be 0.8 for this test
	public void testGetMainNodesWithLowConfidence() {
		// call target method
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Sat Dec 10 16:35:41 CET 2016.flac"));
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Sat Dec 10 16:35:41 CET 2016.flac"));	
		da.init();
		da.reliableConfidenceThreshold = 0.8;
		List<INode> lowConfMainNodes = da.getMainNodesWithLowConfidence();
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
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Mon Dec 12 22:36:56 CET 2016.flac"));	
		da.init();
		da.reliableConfidenceThreshold = 0.8;
		da.minimumConfidenceThreshold = 0.1;
		List<INode> lowConfMainNodes = new ArrayList<INode>();
		lowConfMainNodes = da.getMainNodesWithLowConfidence();
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

	@Ignore
	@Test
	public void testOneWordGetAnswer1() {
		// call target method
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Dec 13 12:32:40 CET 2016.flac"));
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Dec 13 12:32:40 CET 2016.flac"));	
		da.init();
		INode iNode = null;
		try {
			iNode = da.getOneWordAnswer(bg.getGraph());			
		} catch(MissingDataException mde) {
			mde.printStackTrace();
		}
		// create test word as a String
		String correctWord = "bank";
		// compare
		assertEquals(iNode.getAttributeValue("value"), correctWord);
	}
	
	@Ignore
	@Test
	public void testOneWordGetAnswer2() {
		// call target method
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Mon Dec 12 22:36:56 CET 2016.flac"));
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Mon Dec 12 22:36:56 CET 2016.flac"));	
		da.init();
		INode iNode = null;
		try {
			iNode = da.getOneWordAnswer(bg.getGraph());			
		} catch(MissingDataException mde) {
			mde.printStackTrace();
		}
		assertNull(iNode);
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer1() {
		// yes 
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 09:07:39 CET 2017.flac"));
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 09:07:39 CET 2017.flac"));	
		da.init();
		String correctAnswer = "";
		try {
			correctAnswer = da.examineYesNoAnswer(bg.getGraph());			
		} catch(MissingDataException mde) {
			mde.printStackTrace();
		}assertEquals(correctAnswer, "YES");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer2() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 10:27:11 CET 2017.flac"));
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 10:27:11 CET 2017.flac"));	
		da.init();
		String correctAnswer = "";
		try {
			correctAnswer = da.examineYesNoAnswer(bg.getGraph());			
		} catch(MissingDataException mde) {
			mde.printStackTrace();
		}assertEquals(correctAnswer, "NO");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer3() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 20:19:20 CET 2017.flac"));
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 10:27:11 CET 2017.flac"));	
		da.init();
		String correctAnswer = "";
		try {
			correctAnswer = da.examineYesNoAnswer(bg.getGraph());			
		} catch(MissingDataException mde) {
			mde.printStackTrace();
		}
		assertEquals(correctAnswer, "WORD_NOT_UNDERSTOOD");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer4() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 08:16:01 CET 2017.flac"));
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 10:27:11 CET 2017.flac"));	
		da.init();
		String correctAnswer = "";
		try {
			correctAnswer = da.examineYesNoAnswer(bg.getGraph());			
		} catch(MissingDataException mde) {
			mde.printStackTrace();
		}
		assertEquals(correctAnswer, "TO_MANY_WORDS");
	}
	
	@Ignore // check if the new confidence is in the correct node
	@Test 
	public void testVerifyNode() {
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Dialogmanager/audio/answerWed Jan 11 14:08:24 CET 2017.flac"));	
		da.init();
		da.verifyNode((INode) da.graph.getNodes().toArray()[0]);
		
		double correctDouble = -1;
		INode iNode = (INode) da.graph.getNodes().toArray()[0];
		correctDouble = Double.parseDouble(iNode.getAttributeValue("asrConfidence").toString()); 
		assertEquals(correctDouble, 1.0, 0.00001);
	}
	
	@Ignore // check if the alternative nodes are deleted
	@Test 
	public void testRemoveAlternativeNodes() {
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Dialogmanager/audio/answerFri Jan 27 15:40:28 CET 2017.flac"));	
		da.init();
		// Befor node verification we had 18 node afterwards there should be 10
		Object[] array = da.graph.getNodes().toArray();	
		System.out.println(da.graph.getNodes().size());
		da.removeAlternativeNodes((INode) array[2]);
		
		System.out.println(da.graph.getNodes().size());
		assertEquals(da.graph.getNodes().size(), 10);
	}
	
	@Ignore
	@Test 
	public void testReplaceNode() {
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Dec 13 12:32:40 CET 2016.flac"));
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Dialogmanager/audio/answerFri Jan 27 15:40:28 CET 2017.flac"));	
		da.init();
		
		INode newNode = null;
		try {
			newNode = (INode) bg.getGraph().getNodes().toArray()[0];			
		} catch(MissingDataException mde) {
			mde.printStackTrace();		
		}
		da.replaceNode((INode) da.graph.getNodes().toArray()[1], newNode);
		
		INode replacedNode = (INode) da.graph.getNodes().toArray()[17];
		assertEquals(replacedNode.getAttributeValue("value"), "bank");
	}
	
////	@Ignore
//	@Test 
//	public void testReplaceNode2() {
//		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Dec 13 12:32:40 CET 2016.flac"));
//		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Dialogmanager/audio/answerFri Jan 27 15:40:28 CET 2017.flac"));	
//		da.init();
//
//		Object[] iNodeArray1 = da.graph.getNodes().toArray();
//		for (Object iNode : iNodeArray1) {
//			INode node = (INode) iNode;
//			System.out.println(node.getAttributeValue("value")); 
//		}
//		INode newNode = (INode) da.graph.getNodes().toArray()[0];
//		newNode.getAttributeValue("value");
//		da.replaceNode((INode) da.graph.getNodes().toArray()[1], newNode);
//		System.out.println(da.graph.showGraph());
//		INode firstNode = (INode) da.graph.getNodes().toArray()[0];
//		INode secondNode = (INode) da.graph.getNodes().toArray()[1];
//		System.out.println(firstNode.getAttributeValue("value"));
//		System.out.println(secondNode.getAttributeValue("value"));
//		System.out.println("");
//		Object[] iNodeArray = da.graph.getNodes().toArray();
//		for (Object iNode : iNodeArray) {
//			INode node = (INode) iNode;
//			System.out.println(node.getAttributeValue("value")); 
//		}
//		
//		assertEquals(firstNode.getAttributeValue("value"), secondNode.getAttributeValue("value"));
//	}
}
