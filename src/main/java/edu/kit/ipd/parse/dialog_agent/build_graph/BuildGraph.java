package edu.kit.ipd.parse.dialog_agent.build_graph;

import edu.kit.ipd.parse.dialog_agent.DialogAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.pronat.babelfy_wsd.Wsd;
import edu.kit.ipd.pronat.condition_detection.ConditionDetector;
import edu.kit.ipd.pronat.context.ContextAnalyzer;
import edu.kit.ipd.pronat.coref.CorefAnalyzer;
import edu.kit.ipd.pronat.graph_builder.GraphBuilder;
import edu.kit.ipd.pronat.multiasr.MultiASRPipelineStage;
import edu.kit.ipd.pronat.ner.NERTagger;
import edu.kit.ipd.pronat.prepipedatamodel.PrePipelineData;
import edu.kit.ipd.pronat.prepipedatamodel.tools.StringToHypothesis;
import edu.kit.ipd.pronat.shallow_nlp.ShallowNLP;
import edu.kit.ipd.pronat.srl.SRLabeler;

import java.nio.file.Path;

public class BuildGraph {
	
	private boolean agentMode;		// is true, except for the initial user instruction
	private boolean audioFile;		// is true if an audio file is processed and false for a text file
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
