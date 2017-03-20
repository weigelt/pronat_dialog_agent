package edu.kit.ipd.parse.dialog_agent.main;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import edu.kit.ipd.parse.conditionDetection.ConditionDetector;
import edu.kit.ipd.parse.contextanalyzer.ContextAnalyzer;
import edu.kit.ipd.parse.corefanalyzer.CorefAnalyzer;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.Pair;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.StringToHypothesis;
import edu.kit.ipd.parse.multiasr.MultiASRPipelineStage;
import edu.kit.ipd.parse.ner.NERTagger;
import edu.kit.ipd.parse.shallownlp.ShallowNLP;
import edu.kit.ipd.parse.srlabeler.SRLabeler;
import edu.kit.ipd.parse.wsd.Wsd;

public class BuildGraph {
	
	Path inputPath; // path to the input audio.flac file	
	private boolean agentMode;
	private boolean audioFile;
	private MultiASRPipelineStage masr;
	private ShallowNLP snlp;
	private NERTagger nerTagger;
	private SRLabeler srLabeler;
	private GraphBuilder gb;
	private ContextAnalyzer contextAnalyzer;
	private Wsd wsd;
	private ConditionDetector conditionDetector;
	private CorefAnalyzer corefAnalyzer;
	PrePipelineData ppd;
	
	// constructor for audio files
	public BuildGraph(Path inputPath, boolean agentMode) { 
		this.inputPath = inputPath;
		this.agentMode = agentMode;
		this.audioFile = true;
		init();
		ppd = new PrePipelineData();
		ppd.setInputFilePath(inputPath);
	}
	
	// constructor for text files
	public BuildGraph(String text) {
		this.agentMode = false;
		this.audioFile = false;
		init();
		ppd = new PrePipelineData();
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(text));		
	}
	
	public void init() {
		if (audioFile) {
			masr = new MultiASRPipelineStage();
			masr.init();
		}
		snlp = new ShallowNLP();
		snlp.init();
		nerTagger = new NERTagger();
		nerTagger.init();
		srLabeler = new SRLabeler();
		srLabeler.init();
		gb = new GraphBuilder();
		gb.init();
		if (!agentMode) {
			contextAnalyzer = new ContextAnalyzer();
			contextAnalyzer.init();
			wsd = new Wsd();
			wsd.init();
			conditionDetector = new ConditionDetector();
			conditionDetector.init();
			corefAnalyzer = new CorefAnalyzer();
			corefAnalyzer.init();
		}
	}
	
	public IGraph getGraph() throws MissingDataException {
		try {
			if (audioFile) {
				masr.exec(ppd);
			}
			snlp.exec(ppd);
			if (!agentMode) {
				nerTagger.exec(ppd);
				srLabeler.exec(ppd);
			}
			gb.exec(ppd);
			IGraph graph = ppd.getGraph();
			if (!agentMode) {
				wsd.setGraph(graph);
				wsd.exec();
				contextAnalyzer.setGraph(graph);
				contextAnalyzer.exec();
				conditionDetector.setGraph(graph);
				conditionDetector.exec();
				corefAnalyzer.setGraph(graph);
				corefAnalyzer.exec();
				// multiple calls of the agents!
				for (int i = 0; i < 5; i++) {
					wsd.exec();
					contextAnalyzer.exec();
					conditionDetector.exec();
					corefAnalyzer.exec();
				}
			}
		} catch (final PipelineStageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		try {
////			System.out.println(ppd.getGraph().showGraph());
//			Object[] array = ppd.getGraph().getNodes().toArray();
//			for (int i = 0; i < array.length; i++) {
//				INode iNode = (INode) array[i];
//				System.out.println(iNode.toString()); // ########
////				System.out.println(iNode.getAllAttributeNamesAndValuesAsPair().toString());
////				System.out.println(iNode.getAttributeValue("asrConfidence"));
//			}
//			Object[] arrayArcs = ppd.getGraph().getArcs().toArray();
//			for (int i = 0; i < arrayArcs.length; i++) {
//				IArc iArc = (IArc) arrayArcs[i];
//				System.out.println(iArc.toString()); 
//			}
//		} catch (final MissingDataException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		IGraph graph = null;
		try {
			graph = ppd.getGraph();
		} catch (MissingDataException mde) {
			mde.printStackTrace();
		}
		if (graph.equals(null)) {
			throw new MissingDataException();
		}
		return graph;
	}
}
