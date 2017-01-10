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
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Sat Dec 10 16:35:41 CET 2016.flac"));	
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
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Mon Dec 12 22:36:56 CET 2016.flac"));	
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

	@Ignore
	@Test
	public void testOneWordGetAnswer1() {
		// call target method
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Dec 13 12:32:40 CET 2016.flac"));
		bg.buildGraph();
		PrePipelineData ppd = bg.getGraph();
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Dec 13 12:32:40 CET 2016.flac"));	
		da.init();
		INode iNode = da.getOneWordAnswer(ppd);
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
		bg.buildGraph();
		PrePipelineData ppd = bg.getGraph();
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Mon Dec 12 22:36:56 CET 2016.flac"));	
		da.init();
		INode iNode = da.getOneWordAnswer(ppd);
		// assert condition
		assertNull(iNode);
	}
	
//	@Ignore
//	@Test
//	public void testExamineYesNoAnswer1() {
//		// yes
//		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 08:16:01 CET 2017.flac"));
//		bg.buildGraph();
//		PrePipelineData ppd = bg.getGraph();
//		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 09:07:39 CET 2017.flac"));	
//		da.init();
//		da.examineYesNoAnswer(ppd);
//		// assert condition
////		assertNull(iNode);
//	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer1() {
		// yes 
		// the commented (including the second commented part) part can later be used to check a proper node deletion
//		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 08:16:01 CET 2017.flac"));
//		bg.buildGraph();
//		PrePipelineData ppd = bg.getGraph();
//		Object[] array = null;
//		try {
//			array = ppd.getGraph().getNodes().toArray();			
//		} catch (MissingDataException mde) {
//			mde.printStackTrace();
//		}
//		System.out.println("   " + array[40]);
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 09:07:39 CET 2017.flac"));
		bg.buildGraph();
		PrePipelineData ppdAnswer = bg.getGraph();
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 09:07:39 CET 2017.flac"));	
		da.init();
		String correctAnswer = da.examineYesNoAnswer(ppdAnswer);
//		try {
//			System.out.println(ppd.getGraph().showGraph());
//			Object[] array2 = null;
//			try {
//				array2 = ppd.getGraph().getNodes().toArray();			
//			} catch (MissingDataException mde) {
//				mde.printStackTrace();
//			}
//			for (int i = 0; i < array2.length; i++) {
//				INode iNode = (INode) array2[i];
//				System.out.println(iNode.toString());	
//			}
//		} catch (MissingDataException mde) {
//			mde.printStackTrace();
//		}
		assertEquals(correctAnswer, "YES");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer2() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 10:27:11 CET 2017.flac"));
		bg.buildGraph();
		PrePipelineData ppdAnswer = bg.getGraph();
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 10:27:11 CET 2017.flac"));	
		da.init();
		String correctAnswer = da.examineYesNoAnswer(ppdAnswer);
		assertEquals(correctAnswer, "NO");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer3() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 20:19:20 CET 2017.flac"));
		bg.buildGraph();
		PrePipelineData ppdAnswer = bg.getGraph();
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 10:27:11 CET 2017.flac"));	
		da.init();
		String correctAnswer = da.examineYesNoAnswer(ppdAnswer);
		assertEquals(correctAnswer, "WORD_NOT_UNDERSTOOD");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer4() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 08:16:01 CET 2017.flac"));
		bg.buildGraph();
		PrePipelineData ppdAnswer = bg.getGraph();
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 10:27:11 CET 2017.flac"));	
		da.init();
		String correctAnswer = da.examineYesNoAnswer(ppdAnswer);
		assertEquals(correctAnswer, "TO_MANY_WORDS");
	}
}
