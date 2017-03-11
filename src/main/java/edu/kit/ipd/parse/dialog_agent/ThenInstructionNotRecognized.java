package edu.kit.ipd.parse.dialog_agent;

import edu.kit.ipd.parse.luna.graph.IGraph;

public class ThenInstructionNotRecognized extends AbstractDefectCategory {
	
	IGraph graph;
	
	protected boolean analyseGraph(IGraph graph) {
		this.graph = graph;
		System.out.println("Condition");
		return false;
	}
	
	protected void solveDefectCategory() {
	}
}
