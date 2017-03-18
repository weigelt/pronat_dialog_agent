package edu.kit.ipd.parse.dialog_agent;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.dialog_agent.util.GainUserAnswer;
import edu.kit.ipd.parse.dialog_agent.util.Synthesizer;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class ThenInstructionNotRecognized extends AbstractDefectCategory {

	IGraph graph;
	List<INode> textPart = new ArrayList<INode>();

	@Override
	protected boolean analyseGraph(IGraph graph) {
		this.graph = graph;
		System.out.println(graph.showGraph());
		for (INode iNode : graph.getNodes()) {
			System.out.println(iNode);
			for (IArc iArc : iNode.getOutgoingArcs())
				System.out.println(arcToString(iArc));
		}
		System.out.println("");
		System.out.println("");
		for (INode iNode : graph.getNodes())
			System.out.println(iNode);

		System.out.println("");
		System.out.println("");
		for (INode iNode : graph.getNodes()) {
			if (iNode.getType().getName().equals("token"))
				System.out.println(iNode.getAttributeValue("value"));
		}
		
		
		System.out.println("");
		System.out.println("");
		for (INode iNode : graph.getNodes()) {
			if (iNode.getType().getName().equals("token")) {
				System.out.print(iNode.getAttributeValue("value"));
				System.out.print("  ");
				System.out.print(iNode.getAttributeValue("commandType"));
				System.out.println("");
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
		System.out.println(" first Node  " + firstNode);
		
		// checks if there is an if-condition without a then-condition
		boolean ifCondition = false;
		boolean ifConditionFinished = false;
		boolean hasNodes = true;
		INode iteratorNode = firstNode;
		while (hasNodes) {
//			System.out.println(iteratorNode);
//			System.out.println(iteratorNode.getAttributeValue("commandType"));
			// this if condition prevents of trying the same issue over and over again
			if (!iteratorNode.getAttributeValue("corefVerified").toString().equals("notProcessable")) { 
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
						// if not we have to check if one comes before the next if-part
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
	//			System.out.println("icifConditionFinishedf " + ifConditionFinished);
	//			System.out.println("ifCondition " + ifCondition);
	//			System.out.println("textPart size " + textPart.size());
				// takes the next token node in the graph, till the last one is reached
				hasNodes = false;
			}
			for (IArc iArc : iteratorNode.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					iteratorNode = iArc.getTargetNode();
					hasNodes = true;
				}
			}
		}
//		System.out.println("icifConditionFinishedf " + ifConditionFinished);
//		System.out.println("ifCondition " + ifCondition);
		if (ifConditionFinished || ifCondition) { // graph ends
			return true;
		}
		return false;
	}

	@Override
	protected void solveDefectCategory() {
		List<INode> realMatch = new ArrayList<INode>();
		for (int i = 0; i < 4; i++) { // ask the user at most three times (the fourth loop is just for reaching the if part again)
			if (realMatch.isEmpty()) {
				String question = "";
				if (i == 0) {
					question = question + "Please just say the then condition of the following part again:  ";
					for (INode iNode : textPart) {
						question = question + iNode.getAttributeValue("value") + "  ";
					}
				} else if (i == 1) {
					question = question + "I did not get it. Focus, I will repeat your words and you should just repeat this part of your words, "
							+ "which contains the then condition. Your words were:  ";
					for (INode iNode : textPart) {
						question = question + iNode.getAttributeValue("value") + "  ";
					}
				} else if (i == 2) {
					question = question + "Ok, last attempt. Please repeat the then part in your statement:  ";
					for (INode iNode : textPart) {
						question = question + iNode.getAttributeValue("value") + "  ";
					}
				} else {
					// this part is reqched if the third answer is not understood
					for (INode iNode : textPart) {
						iNode.setAttributeValue("conditionVerified", "notProcessable");
					}
					break;
				}
				
				Synthesizer.enunciateQuestion(question);
				IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();
				System.out.println(userAnswerGraph.showGraph());
				List<INode> matchingNodes = compareNodes(textPart, userAnswerGraph);
				for (INode iNode : matchingNodes) {
					System.out.println(iNode);
				}
				realMatch = checkMatch(textPart, matchingNodes);
			} else {
				// set commandType = THEN and conditionVerified = true
				System.out.println("realMatch");
				for (INode iNode : realMatch) {
					iNode.setAttributeValue("commandType", "THEN");
				}
				for (INode iNode : textPart) {
					iNode.setAttributeValue("conditionVerified", true);
				}
				break;
			}
		}

		for (INode iNode : graph.getNodes())
			System.out.println(iNode);
	}
	
	// detect all matching nodes - which are in the question and the answer
	protected List<INode> compareNodes(List<INode> questionablePart, IGraph graph) {
		List<INode> matchingNodes = new ArrayList<INode>();
		for (INode iNode : questionablePart) {
			for (INode answerNode : graph.getNodes()) {
				if (iNode.getAttributeValue("value").equals(answerNode.getAttributeValue("value"))) {
					System.out.println(iNode);
					System.out.println(answerNode);
					System.out.println("match");
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
		for (int i = 0; i < questionablePart.size(); i++) {
			if (matchingNodes.contains(questionablePart.get(i))) {
				if (twoInARow) {
					realMatch.add(questionablePart.get(i));
				} else if (i + 1 < questionablePart.size()) {
					if (matchingNodes.contains(questionablePart.get(i + 1))) {
						realMatch.add(questionablePart.get(i));
						twoInARow = true;						
					}
				}
			}
		}
		if (realMatch.contains(questionablePart.get(questionablePart.size() - 2))) {
			realMatch.add(questionablePart.get(questionablePart.size() - 1));
		}
		return realMatch;
	}

	// dummy method because toString of ParseArc is hard to read
	private String arcToString(IArc iArc) {
		String ts = "Arc(type: " + iArc.getType().getName() + " - ";
		for (final String attrName : iArc.getAttributeNames()) {
			ts = ts.concat(" " + attrName + ": " + iArc.getAttributeValue(attrName) + ",");
		}
		ts = ts.substring(0, ts.length() - 1).concat(")");
		return ts;
	}
}
