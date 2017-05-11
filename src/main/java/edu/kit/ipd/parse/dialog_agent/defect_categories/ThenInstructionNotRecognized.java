package edu.kit.ipd.parse.dialog_agent.defect_categories;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.dialog_agent.AbstractDefectCategory;
import edu.kit.ipd.parse.dialog_agent.util.GainUserAnswer;
import edu.kit.ipd.parse.dialog_agent.util.Synthesizer;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class ThenInstructionNotRecognized extends AbstractDefectCategory {
	
	private static final Logger logger = LoggerFactory.getLogger(ThenInstructionNotRecognized.class);

	private List<INode> textPart = new ArrayList<INode>();

	@Override
	protected boolean analyseGraph(IGraph graph) {
		// set attributes of the defect category
		for (INode iNode : graph.getNodes()) {
			if (iNode.getType().getName().equals("token")) {
				if (!iNode.getType().containsAttribute("commandTypeVerified", "boolean")) {
					iNode.getType().addAttributeToType("boolean", "commandTypeVerified");		
					iNode.setAttributeValue("commandTypeVerified", false);
				} else {
					if (iNode.getAttributeValue("commandTypeVerified") == null) {
						iNode.setAttributeValue("commandTypeVerified", false);					
					}		
				}
				
				if (!iNode.getType().containsAttribute("commandTypeNotProcessable", "boolean")) {
					iNode.getType().addAttributeToType("boolean", "commandTypeNotProcessable");		
					iNode.setAttributeValue("commandTypeNotProcessable", false);
				} else {
					if (iNode.getAttributeValue("commandTypeNotProcessable") == null) {
						iNode.setAttributeValue("commandTypeNotProcessable", false);					
					}		
				}
			}
		} 
	
		// get the first token node of the graph
		INode firstNode = null;
		for (INode iNode : graph.getNodes()) {
			if (iNode.getType().getName().equals("token")) {
				firstNode = iNode;
				break;
			}
		} 
		boolean search = true; 
		while (search) {
			search = false;
			for (IArc iArc : firstNode.getIncomingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					firstNode = iArc.getSourceNode();
					search = true;
				}
			}
		}
		
		// checks if there is an if-condition without a then-condition
		boolean ifCondition = false;
		boolean ifConditionFinished = false;
		boolean hasNodes = true;
		INode iteratorNode = firstNode;
		while (hasNodes) {
			// this if condition prevents of trying the same issue over and over again
			if (!iteratorNode.getAttributeValue("commandTypeNotProcessable").equals(true) && !iteratorNode.getAttributeValue("commandTypeVerified").equals(true)) { 
				if (ifConditionFinished) {
					if (iteratorNode.getAttributeValue("commandType").toString().equals("IF")) {
						// new if before then -> then is missing
						return true;
					} else if (iteratorNode.getAttributeValue("commandType").toString().equals("THEN")) {
						// everything is all right
						ifConditionFinished = false;
						textPart = new ArrayList<INode>();
					}
					else {
						textPart.add(iteratorNode);					
					}
				}
				if (iteratorNode.getAttributeValue("commandType").toString().equals("IF")) { 
					// an if-condition started
					ifCondition = true;
					textPart.add(iteratorNode);
				} 
				if (ifCondition) {
					if (iteratorNode.getAttributeValue("commandType").toString().equals("THEN")) { 
						// if an "if"-part a "then"-part follows everything is all right
						ifCondition = false;
						ifConditionFinished = false;
						textPart = new ArrayList<INode>();
					} 
					else if (!iteratorNode.getAttributeValue("commandType").toString().equals("IF")) { 
						// check, if there is a then-condition before the next if-part
						ifConditionFinished = true;
						ifCondition = false;
						if (!textPart.contains(iteratorNode))
							textPart.add(iteratorNode);		
					} 
					else {
						if (!textPart.contains(iteratorNode))
							textPart.add(iteratorNode);						
					}
				}
				
				// takes the next token node in the graph, till the last one is reached
				hasNodes = false;
			} else {
				break;
			}
			for (IArc iArc : iteratorNode.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					iteratorNode = iArc.getTargetNode();
					hasNodes = true;
				}
			}
		}
		if (ifConditionFinished || ifCondition) { // graph ends
			return true;
		}
		return false;
	}

	@Override
	protected void solveDefectCategory() {
		logger.info("Start solving ConditionDetector issues - IF-Condition without THEN-Block found");
		for (INode iNode : textPart) {
			logger.debug("Affected passage " + iNode);
		}
		
		List<INode> realMatch = new ArrayList<INode>();
		for (int i = 0; i < 4; i++) { // ask the user at most three times (the fourth loop is just for reaching the if part again)
			if (realMatch.isEmpty()) {
				String question = "";
				if (i == 0) { // first question
					question = question + "Please just say the then condition of the following part again:  ";
					for (INode iNode : textPart) {
						question = question + iNode.getAttributeValue("value") + "  ";
					}
				} else if (i == 1) { // second question
					question = question + "I did not get it. Focus, your words contain a condition. Please just repeat the part, which tells me what I"
							+ " have to do if the condition is true. Your words were:  ";
					for (INode iNode : textPart) {
						question = question + iNode.getAttributeValue("value") + "  ";
					}
				} else if (i == 2) { // third question
					question = question + "Ok, last attempt. Please repeat the instruction in your statement I have to perform, if the following condition is correct:  ";
					for (INode iNode : textPart) {
						question = question + iNode.getAttributeValue("value") + "  ";
					}
				} else { // this part is reqched if the third answer is not understood
					for (INode iNode : textPart) {
						iNode.setAttributeValue("commandTypeNotProcessable", true);
					}
					break;
				}
				
				for (INode iNode : textPart) {
					logger.debug("Condition question " + iNode);
				}

				// ask the user and get the answer graph
				Synthesizer.enunciateQuestion(question);
				IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();
				for (INode iNode : userAnswerGraph.getNodes()) {
					logger.debug("Condition answer " + iNode);
				}
				
				// check if the then-condition was recognized properly
				List<INode> matchingNodes = compareNodes(textPart, userAnswerGraph);
				realMatch = checkMatch(textPart, matchingNodes);
			} else {
				// set commandType = THEN and conditionVerified = true
				for (INode iNode : realMatch) {
					logger.debug("Then-Block node: " + iNode);
					iNode.setAttributeValue("commandType", "THEN");
				}
				for (INode iNode : textPart) {
					iNode.setAttributeValue("commandTypeVerified", true);
				}
				logger.info("Then-Block recognized and integrated into the graph.");
				break;
			}
		}
	}
	
	// detect all matching nodes - which are in the question and the answer
	protected List<INode> compareNodes(List<INode> questionablePart, IGraph graph) {
		List<INode> matchingNodes = new ArrayList<INode>();
		for (INode iNode : questionablePart) {
			for (INode answerNode : graph.getNodes()) {
				if (iNode.getAttributeValue("value").equals(answerNode.getAttributeValue("value"))) {
					if (!matchingNodes.contains(iNode)) {
						matchingNodes.add(iNode);						
					}
				} 
			}
		}
		return matchingNodes;
	}
	
	// returns the useful part of the match
	protected List<INode> checkMatch(List<INode> questionablePart, List<INode> matchingNodes) {
		List<INode> realMatch = new ArrayList<INode>();
		boolean twoInARow = false;
		// the first two nodes in a row that match will be the starting point of the then-condition
		// then all further matching nodes are added to this words
		for (int i = 0; i < questionablePart.size(); i++) {
			if (matchingNodes.contains(questionablePart.get(i))) {
				if (twoInARow) {
					if (matchingNodes.contains(questionablePart.get(i - 1))) {
						realMatch.add(questionablePart.get(i));
					}
				} else if (i + 1 < questionablePart.size()) {
					if (matchingNodes.contains(questionablePart.get(i + 1))) {
						realMatch.add(questionablePart.get(i));
						twoInARow = true;						
					}
				}
			}
		}
		
		// this part adds the last word of a question, if the words before are added - useful if the last word is hard to understand
		if (realMatch.contains(questionablePart.get(questionablePart.size() - 2))) {
			realMatch.add(questionablePart.get(questionablePart.size() - 1));
		}
		
		// if the whole if-condition is mentioned in the answer, then the user did not understand the answer
		if (realMatch.contains(questionablePart.get(0))) {
			realMatch = new ArrayList<INode>();
		}
		return realMatch;
	}
}
