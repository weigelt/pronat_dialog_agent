package edu.kit.ipd.parse.dialog_agent;

import edu.kit.ipd.parse.luna.graph.IGraph;

public interface IDefectCategory { 
	
	public void processDefectCategory(IGraph graph);	
	public void nextDefectCategory(IDefectCategory nextDefectCategory); 
}
