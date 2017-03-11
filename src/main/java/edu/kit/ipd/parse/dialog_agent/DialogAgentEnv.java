package edu.kit.ipd.parse.dialog_agent;

import edu.kit.ipd.parse.luna.graph.IGraph;

public class DialogAgentEnv {

	IDefectCategory firstDefectCategory;
	
	public DialogAgentEnv() { // init?
		firstDefectCategory = new WordWrongInterpreted();
		CorefWrongReferenced corefWrongReferenced = new CorefWrongReferenced();
		firstDefectCategory.nextDefectCategory(corefWrongReferenced);
		ThenInstructionNotRecognized thenInstructionNotRecognized = new ThenInstructionNotRecognized();
		corefWrongReferenced.nextDefectCategory(thenInstructionNotRecognized);
	}
	
	// exec?
	public void dialogAgentInputHandler (IGraph graph){
		firstDefectCategory.processDefectCategory(graph);
	}
}
