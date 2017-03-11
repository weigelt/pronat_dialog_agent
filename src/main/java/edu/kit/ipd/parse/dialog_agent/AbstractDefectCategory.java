package edu.kit.ipd.parse.dialog_agent;

import edu.kit.ipd.parse.luna.graph.IGraph;

public abstract class AbstractDefectCategory implements IDefectCategory {

	private IDefectCategory nextDefectCategory;
	
	public void processDefectCategory(IGraph graph) {
		if (analyseGraph(graph)) {
			// solve one issue of this defect category
			solveDefectCategory(); 
		} else {
			// go on to the next defect category no indicator found
			if (nextDefectCategory != null)
				nextDefectCategory.processDefectCategory(graph);	
		}
	}
	
	public void nextDefectCategory(IDefectCategory nextDefectCategory) {
		this.nextDefectCategory = nextDefectCategory;
	}
	
	abstract protected boolean analyseGraph(IGraph graph);
	
	abstract protected void solveDefectCategory();
}
