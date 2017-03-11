package edu.kit.ipd.parse.dialog_agent;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class WordWrongInterpretedTest {

	@Ignore
	@Test // the RELIABLE_CONFIDENCE_THRESHOLD has to be 0.8 for this test
	public void testGetMainNodesWithLowConfidence() {
		// call target method
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Sat Dec 10 16:35:41 CET 2016.flac"), false);
		DialogAgent da = new DialogAgent(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Sat Dec 10 16:35:41 CET 2016.flac"));	
		da.init();
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		wwi.reliableConfidenceThreshold = 0.8;
		wwi.analyseGraph(da.graph);
		List<INode> lowConfMainNodes = wwi.getTokenNodesWithLowConfidence();
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
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		wwi.reliableConfidenceThreshold = 0.8;
		wwi.minimumConfidenceThreshold = 0.1;
		wwi.analyseGraph(da.graph);
		List<INode> lowConfMainNodes = wwi.getTokenNodesWithLowConfidence();
		List<INode> lowConfidenceNodesAlternatives = wwi.determineAlternatives(lowConfMainNodes.get(0));
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
	public void testExamineYesNoAnswer1() {
		// yes 
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 09:07:39 CET 2017.flac"), true);
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		IGraph graph = null;
		try {
			graph = bg.getGraph();
		} catch (MissingDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String correctAnswer = wwi.examineYesNoAnswer(graph);		
		assertEquals(correctAnswer, "YES");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer2() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 10:27:11 CET 2017.flac"), true);
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		String correctAnswer = "";
		try {
			correctAnswer = wwi.examineYesNoAnswer(bg.getGraph());			
		} catch(MissingDataException mde) {
			mde.printStackTrace();
		}assertEquals(correctAnswer, "NO");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer3() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 20:19:20 CET 2017.flac"), true);
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		String correctAnswer = "";
		try {
			correctAnswer = wwi.examineYesNoAnswer(bg.getGraph());			
		} catch(MissingDataException mde) {
			mde.printStackTrace();
		}
		assertEquals(correctAnswer, "WORD_NOT_UNDERSTOOD");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer4() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 08:16:01 CET 2017.flac"), true);
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		String correctAnswer = "";
		try {
			correctAnswer = wwi.examineYesNoAnswer(bg.getGraph());			
		} catch(MissingDataException mde) {
			mde.printStackTrace();
		}
		assertEquals(correctAnswer, "TO_MANY_WORDS");
	}
}
