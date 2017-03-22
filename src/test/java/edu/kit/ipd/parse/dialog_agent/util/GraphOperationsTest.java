package edu.kit.ipd.parse.dialog_agent.util;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import edu.kit.ipd.parse.conditionDetection.ConditionDetector;
import edu.kit.ipd.parse.contextanalyzer.ContextAnalyzer;
import edu.kit.ipd.parse.corefanalyzer.CorefAnalyzer;
import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;
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

public class GraphOperationsTest {
	
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
	
	@Ignore // check if the new confidence is in the correct node
	@Test 
	public void testVerifyNode1() {
		IGraph graph = buildGraph(Paths.get("/Users/Mario/Dialogmanager/audio/answerWed Jan 11 14:08:24 CET 2017.flac"));
		GraphOperations.asrVerifyNode(graph, (INode) graph.getNodes().toArray()[0]);
		double correctDouble = -1;
		INode iNode = (INode) graph.getNodes().toArray()[0];
		correctDouble = Double.parseDouble(iNode.getAttributeValue("asrConfidence").toString()); 
		assertEquals(correctDouble, 1.0, 0.00001);
	}
	
	@Ignore // check if verifiedByDialogAgent is true
	@Test 
	public void testVerifyNode2() {
		IGraph graph = buildGraph(Paths.get("/Users/Mario/Dialogmanager/audio/answerWed Jan 11 14:08:24 CET 2017.flac"));
		GraphOperations.asrVerifyNode(graph, (INode) graph.getNodes().toArray()[0]);
		INode iNode = (INode) graph.getNodes().toArray()[0];
		boolean verifiedByDialogAgent = Boolean.parseBoolean(iNode.getAttributeValue("verifiedByDialogAgent").toString()); 
		assertEquals(verifiedByDialogAgent, true);
	}
	
	@Ignore // check if the alternative nodes are deleted
	@Test 
	public void testRemoveAlternativeNodes() {
		BuildGraph bg = new BuildGraph(Paths.get("/Users/Mario/Dialogmanager/audio/answerFri Jan 27 15:40:28 CET 2017.flac"), true);
		IGraph graph = bg.getGraph();
		// Befor node verification we had 18 node afterwards there should be 10
		Object[] array = graph.getNodes().toArray();	
		System.out.println(graph.getNodes().size());
		GraphOperations.removeAlternativeNodes(graph, (INode) array[2]);
		
		System.out.println(graph.getNodes().size());
		assertEquals(graph.getNodes().size(), 10);
	}
	
	@Ignore // checks if the node is correctly replaced
	@Test 
	public void testReplaceNode1() {
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
	
	@Ignore // checks if verifiedByDialogAgent is true
	@Test 
	public void testReplaceNode2() {
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
		boolean verifiedByDialogAgent = Boolean.parseBoolean(replacedNode.getAttributeValue("verifiedByDialogAgent").toString());
		assertEquals(verifiedByDialogAgent, true);
	}
}
