package edu.kit.ipd.parse.dialog_agent;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import edu.kit.ipd.parse.conditionDetection.ConditionDetector;
import edu.kit.ipd.parse.contextanalyzer.ContextAnalyzer;
import edu.kit.ipd.parse.corefanalyzer.CorefAnalyzer;
import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;
import edu.kit.ipd.parse.dialog_agent.util.GraphOperationsTest;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.multiasr.MultiASRPipelineStage;
import edu.kit.ipd.parse.ner.NERTagger;
import edu.kit.ipd.parse.shallownlp.ShallowNLP;
import edu.kit.ipd.parse.srlabeler.SRLabeler;
import edu.kit.ipd.parse.wsd.Wsd;

public class WordWrongInterpretedTest {

	public IGraph buildGraph(Path path) { 
		PrePipelineData ppd = new PrePipelineData();
		ppd.setInputFilePath(path);
		
	    MultiASRPipelineStage masr = new MultiASRPipelineStage();
		masr.init();
		ShallowNLP snlp = new ShallowNLP();
		snlp.init();
		NERTagger nerTagger = new NERTagger();
		nerTagger.init();
	    SRLabeler srLabeler = new SRLabeler();
		srLabeler.init();
		GraphBuilder gb = new GraphBuilder();
		gb.init();
		ContextAnalyzer contextAnalyzer = new ContextAnalyzer();
		contextAnalyzer.init();
		Wsd wsd = new Wsd();
		wsd.init();
		ConditionDetector conditionDetector = new ConditionDetector();
		conditionDetector.init();
		CorefAnalyzer corefAnalyzer = new CorefAnalyzer();
		corefAnalyzer.init();

		IGraph graph = null;
		try {
			masr.exec(ppd);
			snlp.exec(ppd);
			nerTagger.exec(ppd);
			srLabeler.exec(ppd);
			gb.exec(ppd);
			
			try {
				graph = ppd.getGraph();
			} catch (MissingDataException mde) {
				mde.printStackTrace();
			}
			wsd.setGraph(graph);
			wsd.exec();
			contextAnalyzer.setGraph(graph);
			contextAnalyzer.exec();
			conditionDetector.setGraph(graph);
			conditionDetector.exec();
			corefAnalyzer.setGraph(graph);
			corefAnalyzer.exec();
			// multiple calls of the agents!
			for (int i = 0; i < 3; i++) {
				wsd.exec();
				contextAnalyzer.exec();
				conditionDetector.exec();
				corefAnalyzer.exec();
			}
		} catch (final PipelineStageException e) {
			e.printStackTrace();
		}
		return graph;
	}
	
//	@Ignore
	@Test // the RELIABLE_CONFIDENCE_THRESHOLD has to be 0.8 for this test
	public void testGetMainNodesWithLowConfidence() {
		// call target method
		IGraph graph = buildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Sat Dec 10 16:35:41 CET 2016.flac"));
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		wwi.reliableConfidenceThreshold = 0.8;
		wwi.analyseGraph(graph);
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
	
////	@Ignore
//	@Test // RELIABLE_CONFIDENCE_THRESHOLD = 0.8, MINIMUM_CONFIDENCE_THRESHOLD = 0.1
//	public void testDetermineAlternatives() {
//		// call target method
//		IGraph graph = buildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Mon Dec 12 22:36:56 CET 2016.flac"));	
//		WordWrongInterpreted wwi = new WordWrongInterpreted();
//		wwi.reliableConfidenceThreshold = 0.8;
//		wwi.minimumConfidenceThreshold = 0.1;
//		wwi.analyseGraph(graph);
//		List<INode> lowConfMainNodes = wwi.getTokenNodesWithLowConfidence();
//		List<INode> lowConfidenceNodesAlternatives = wwi.determineAlternatives(lowConfMainNodes.get(0));
//		HashSet<String> resultSet = new HashSet<String>();
//		for (INode nodes : lowConfidenceNodesAlternatives) {
//			resultSet.add(nodes.getAttributeValue("value").toString());
//		}
//		System.out.println(resultSet);
//		// create test set
//		HashSet<String> correctSet = new HashSet<String>();
//		correctSet.add("bed");
//		correctSet.add("bat");
//		System.out.println(correctSet);
//		// compare
//		assertEquals(resultSet, correctSet);
//	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer1() {
		// yes 
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 09:07:39 CET 2017.flac"), true);
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		IGraph graph = bg.getGraph();
		String correctAnswer = wwi.examineYesNoAnswer(graph);		
		assertEquals(correctAnswer, "YES");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer2() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/answerTue Jan 10 10:27:11 CET 2017.flac"), true);
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		String correctAnswer = wwi.examineYesNoAnswer(bg.getGraph());	
		assertEquals(correctAnswer, "NO");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer3() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 20:19:20 CET 2017.flac"), true);
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		String correctAnswer = wwi.examineYesNoAnswer(bg.getGraph());	
		assertEquals(correctAnswer, "WORD_NOT_UNDERSTOOD");
	}
	
	@Ignore
	@Test
	public void testExamineYesNoAnswer4() {
		// no
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Parse/AudioArchive/voiceRecord-Tue Jan 10 08:16:01 CET 2017.flac"), true);
		WordWrongInterpreted wwi = new WordWrongInterpreted();
		String correctAnswer = wwi.examineYesNoAnswer(bg.getGraph());	
		assertEquals(correctAnswer, "TO_MANY_WORDS");
	}
}
