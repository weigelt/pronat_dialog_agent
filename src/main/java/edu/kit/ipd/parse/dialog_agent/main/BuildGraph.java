package edu.kit.ipd.parse.dialog_agent.main;

import java.nio.file.Path;

import edu.kit.ipd.parse.conditionDetection.ConditionDetector;
import edu.kit.ipd.parse.contextanalyzer.ContextAnalyzer;
import edu.kit.ipd.parse.corefanalyzer.CorefAnalyzer;
import edu.kit.ipd.parse.dialog_agent.DialogAgent;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.StringToHypothesis;
import edu.kit.ipd.parse.multiasr.MultiASRPipelineStage;
import edu.kit.ipd.parse.ner.NERTagger;
import edu.kit.ipd.parse.shallownlp.ShallowNLP;
import edu.kit.ipd.parse.srlabeler.SRLabeler;
import edu.kit.ipd.parse.wsd.Wsd;

public class BuildGraph {
	
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
	private DialogAgent dialogAgent;
	PrePipelineData ppd;
	IGraph graph;
	
	// constructor for audio files
	public BuildGraph(Path inputPath, boolean agentMode) { 
		this.agentMode = agentMode;
		this.audioFile = true;
		init();
		ppd.setInputFilePath(inputPath);
		exec();
	}
	
	// constructor for text files
	public BuildGraph(String text, boolean agentMode) {
		this.agentMode = agentMode;
		this.audioFile = false;
		init();
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(text));		
		exec();
	}
	
	public void init() {
		ppd = new PrePipelineData();
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
			dialogAgent = new DialogAgent();
			dialogAgent.init();
		}
	}
	
	public void exec() {
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
			graph = ppd.getGraph();
			if (!agentMode) {
				wsd.setGraph(graph);
				wsd.exec();
				contextAnalyzer.setGraph(graph);
				contextAnalyzer.exec();
				conditionDetector.setGraph(graph);
				conditionDetector.exec();
				corefAnalyzer.setGraph(graph);
				corefAnalyzer.exec();
				dialogAgent.setGraph(graph);
				while (true) {
					// multiple calls of the agents!
					for (int i = 0; i < 3; i++) {
						wsd.exec();
						contextAnalyzer.exec();
						conditionDetector.exec();
						corefAnalyzer.exec();
					}
					dialogAgent.exec();
				}
			}
		} catch (final PipelineStageException e) {
			e.printStackTrace();
		} catch (MissingDataException mde) {
			mde.printStackTrace();
		}
	}
	
	public IGraph getGraph() {
		return graph;
	}
}
