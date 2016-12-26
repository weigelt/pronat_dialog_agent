package edu.kit.ipd.parse.dialog_agent.main;

import java.nio.file.Path;

import edu.kit.ipd.parse.graphBuilder.GraphBuilder;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.multiasr.MultiASRPipelineStage;
import edu.kit.ipd.parse.shallownlp.ShallowNLP;

public class BuildGraph {
	
	Path inputPath; // path to the input audio.flac file	
	private GraphBuilder gb;
	private ShallowNLP snlp;
	private MultiASRPipelineStage masr;
	PrePipelineData ppd;
	
	// constructor
	public BuildGraph(Path inputPath) { 
		this.inputPath = inputPath;
		gb = new GraphBuilder();
		gb.init();
		snlp = new ShallowNLP();
		snlp.init();
		masr = new MultiASRPipelineStage();
		masr.init();
	}
	
	public void buildGraph() {
		ppd = new PrePipelineData();
		ppd.setInputFilePath(inputPath);
		try {
			masr.exec(ppd);
			snlp.exec(ppd);
			gb.exec(ppd);
		} catch (final PipelineStageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println(ppd.getGraph().showGraph());
			Object[] array = ppd.getGraph().getNodes().toArray();
			for (int i = 0; i < array.length; i++) {
				INode iNode = (INode) array[i];
				System.out.println(iNode.toString()); // ########
//				System.out.println(iNode.getAllAttributeNamesAndValuesAsPair().toString());
//				System.out.println(iNode.getAttributeValue("asrConfidence"));
			}
		} catch (final MissingDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public PrePipelineData getGraph() {
		return ppd;
	}
}
