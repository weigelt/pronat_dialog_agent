package edu.kit.ipd.parse.dialog_agent;

import edu.kit.ipd.parse.luna.graph.IGraph;

public class CorefWrongReferenced extends AbstractDefectCategory {
	
	IGraph graph;
	
	protected boolean analyseGraph(IGraph graph) {
		this.graph = graph;
		System.out.println("Coref");
		return false;
	}
	
	protected void solveDefectCategory() {
	}
}
