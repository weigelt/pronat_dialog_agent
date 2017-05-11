package edu.kit.ipd.parse.dialog_agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.dialog_agent.defect_categories.CorefWrongReferenced;
import edu.kit.ipd.parse.dialog_agent.defect_categories.ThenInstructionNotRecognized;
import edu.kit.ipd.parse.dialog_agent.defect_categories.WordWrongInterpreted;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;

public class DialogAgent extends AbstractAgent {
	
	private static final Logger logger = LoggerFactory.getLogger(DialogAgent.class); 
	IDefectCategory firstDefectCategory;
	
	@Override
	public void init() {
		firstDefectCategory = new WordWrongInterpreted();
		CorefWrongReferenced corefWrongReferenced = new CorefWrongReferenced();
		firstDefectCategory.nextDefectCategory(corefWrongReferenced);
		ThenInstructionNotRecognized thenInstructionNotRecognized = new ThenInstructionNotRecognized();
		corefWrongReferenced.nextDefectCategory(thenInstructionNotRecognized);
	}
	
	@Override
	public void exec() {
		logger.info("Start dialog agent.");
		firstDefectCategory.processDefectCategory(graph);
	}
}
