package edu.kit.ipd.parse.dialog_agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.parse.dialog_agent.util.GainUserAnswer;
import edu.kit.ipd.parse.dialog_agent.util.Synthesizer;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class CorefWrongReferenced extends AbstractDefectCategory {
	
	IGraph graph;
	double CONFIDENCE_CORRECT_THRESHOLD = 1.0;
	double CONFIDENCE_HIGH_THRESHOLD = 0.5;
	
	List<INode> questionableContextEntities = new ArrayList<INode>();
	
	@Override
	protected boolean analyseGraph(IGraph graph) {
		return false;
//		this.graph = graph;
//		boolean result = false;
//		
//		System.out.println(graph.showGraph());
//		for (INode iNode : graph.getNodes()) {
//			System.out.println(iNode);
//		}
////		
////		System.out.println("");
////		System.out.println("");
////		for (IArc iArc : graph.getArcs()) {
////			if (iArc.getType().getName().equals("contextRelation")) {
////				if (iArc.getAttributeValue("name").equals("anaphoraReferent")) {
////					INode anaphoraSourceNode = iArc.getSourceNode();
////					System.out.println(detectContextEntityElements(anaphoraSourceNode));
//////					for (IArc sourceOutgoingArc : anaphoraSourceNode.getOutgoingArcs()) {
//////						if (sourceOutgoingArc.getType().getName().equals("reference"))
//////							System.out.println("anaphora source token " + sourceOutgoingArc.getTargetNode());
//////					}
////					System.out.println(" anaphora source node " + anaphoraSourceNode);
////					System.out.println("  " + arcToString(iArc));
////					INode anaphoraTargetNode = iArc.getTargetNode();
////					System.out.println(" anaphora target node " + anaphoraTargetNode);
////					System.out.println(detectContextEntityElements(anaphoraTargetNode));
//////					for (IArc targetOutgoingArc : anaphoraTargetNode.getOutgoingArcs()) {
////////						System.out.println(arcToString(targetOutgoingArc));
//////						if (targetOutgoingArc.getType().getName().equals("reference"))
//////							System.out.println("anaphora target token " + targetOutgoingArc.getTargetNode());
////////							for (IArc targetOutgoingArc2 : targetOutgoingArc.getTargetNode().getOutgoingArcs()) {
//////////								System.out.println(arcToString(targetOutgoingArc2));
////////								if (targetOutgoingArc2.getType().getName().equals("reference"))
////////									System.out.println("anaphora target token " + targetOutgoingArc2.getTargetNode());
////////							}
//////					}
////					System.out.println("");
////				}
////			}
////		}
//		
//		
//		
//		// get all contextEntities with outgoing anaphoraReferent arcs
//		Set<INode> contextEntities = new HashSet<INode>();
//		for (INode iNode : graph.getNodes()) { 
//			if (iNode.getType().getName().equals("contextEntity")) { // get just the contextEntity nodes
//				for (IArc iArc : iNode.getOutgoingArcs()) {
//					if (iArc.getType().getName().equals("contextRelation")) { 
//						if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { // with outgoing anaphoraReferent arcs
//							contextEntities.add(iNode);
//						}
//					}
//				}
//			}
//		}
//		
//		// fills the list questionableContextEntities with contextEntities that are questionable
//		for (INode iNode : contextEntities) {
//			List<INode> confidenceCorrectList = new ArrayList<INode>();
//			List<INode> confidenceHighList = new ArrayList<INode>();
//			List<INode> confidenceLowList = new ArrayList<INode>();
//			for (IArc iArc : iNode.getOutgoingArcs()) {
//				if (iArc.getType().getName().equals("contextRelation")) { 
//					if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
//						double confidence = Double.parseDouble(iArc.getAttributeValue("confidence").toString());
//						if (confidence == CONFIDENCE_CORRECT_THRESHOLD) {
//							for (IArc iArcReference : iArc.getTargetNode().getOutgoingArcs()) {
//								if (iArcReference.getType().getName().equals("reference")) {
//									confidenceCorrectList.add(iArcReference.getTargetNode());
//								}
//							}
//						}
//						if (confidence < CONFIDENCE_CORRECT_THRESHOLD && confidence >= CONFIDENCE_HIGH_THRESHOLD) {
//							for (IArc iArcReference : iArc.getTargetNode().getOutgoingArcs()) {
//								if (iArcReference.getType().getName().equals("reference")) {
//									confidenceHighList.add(iArcReference.getTargetNode());	
//								}
//							}
//						}
//						if (confidence < CONFIDENCE_HIGH_THRESHOLD) {
//							for (IArc iArcReference : iArc.getTargetNode().getOutgoingArcs()) {
//								if (iArcReference.getType().getName().equals("reference")) {
//									confidenceLowList.add(iArcReference.getTargetNode());
//								}
//							}
//						}
//					}
//				}
//			}
//			
//			if (confidenceCorrectList.size() > 1) {
//				questionableContextEntities.add(iNode);
//				result = true;
//			} else if (confidenceCorrectList.size() == 0 && confidenceHighList.size() > 1) {
//				questionableContextEntities.add(iNode);
//				result = true;
//			} else if (confidenceCorrectList.size() == 0 && confidenceHighList.size() == 0 && confidenceLowList.size() > 1) {
//				questionableContextEntities.add(iNode);
//				result = true;
//			}
//			
////			System.out.println(iNode);
////			System.out.println("size " + confidenceCorrectList.size());
////			System.out.println("size " + confidenceHighList.size());
////			System.out.println("size " + confidenceLowList.size());
//		}
//		
////		for (int i = 0; i < questionableContextEntities.size(); i++) {
////			System.out.println(questionableContextEntities.get(i));	
////		}
//		return result;
	}
	
	@Override
	protected void solveDefectCategory() {
		System.out.println("Just do it!");
		INode contextEntityNode = questionableContextEntities.get(0);
		int countAnaphoraArcs = 0;
		for (IArc iArc : contextEntityNode.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("contextRelation")) { 
				if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
					countAnaphoraArcs++;
				}
			}
		}
		if (countAnaphoraArcs == 2) {
			yesNoQuestion(contextEntityNode);
		} else {
			// ask alternative question
			yesNoQuestion(contextEntityNode);
		}
	}
	
	protected void yesNoQuestion(INode iNode) {
		System.out.println("in the question part");
		INode endNode = null; // is the token of the contextEntity
		
		// this part extracts the tokens where the observed contextEntity = iNode refers to (all anaphoraReferent -> contextEntity -> reference -> tokens)
		List<INode> contextTokens = new ArrayList<INode>();
		List<Double> contextTokensConfidence = new ArrayList<Double>();
		for (IArc iArc : iNode.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("reference")) {
				endNode = iArc.getTargetNode();
			}
			if (iArc.getType().getName().equals("contextRelation")) { 
				if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
					for (IArc iArcTargetContextEntity : iArc.getTargetNode().getOutgoingArcs()) {
						if (iArcTargetContextEntity.getType().getName().equals("reference")) {
							contextTokens.add(iArcTargetContextEntity.getTargetNode());
						}
					}
					contextTokensConfidence.add(Double.parseDouble(iArc.getAttributeValue("confidence").toString()));
				}
			}
		} 
		
		List<INode> textPart = getQuestionableTextPart(iNode, contextTokens, endNode);

		// phrase question
		String question = "In the following part, where does the ";
		List<INode> tokensEntityToAskForList = new ArrayList<INode>();
		detectContextEntityTokens(tokensEntityToAskForList, iNode);
		if (tokensEntityToAskForList.size() > 1) {
			question = question + "phrase  ";
			for (INode iNodeTokenToAskFor : tokensEntityToAskForList) {
				question = question + iNodeTokenToAskFor.getAttributeValue("value") + "  ";
			}
		} else if (tokensEntityToAskForList.size() == 1) {
			question = question + "word  ";
			question = question + tokensEntityToAskForList.get(0).getAttributeValue("value") + "  ";
		} else {
			// something went wrong - logger!
		}
		question = question + "refers to?  ";
		for (int i = 0; i < textPart.size(); i++) {
			question = question + textPart.get(textPart.size() - i - 1).getAttributeValue("value") + "  ";
		}
		
		Synthesizer.enunciateQuestion(question);
		IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();
		System.out.println(userAnswerGraph.showGraph());
		
		// include answer into graph
	}
	
	// returns the part of the text which is included in the question
	protected List<INode> getQuestionableTextPart(INode iNode, List<INode> contextTokens, INode endNode) {
		List<INode> textPart = new ArrayList<INode>(); // text part which is later used for the question
		INode startNode = endNode;
		textPart.add(startNode);
		int counter = 0;
		// iterate in the graph backwords till first reference
		while(counter < contextTokens.size()) {
			for (IArc iArc : startNode.getIncomingArcs()) {
				if (iArc.getType().getName().equals("relation")) { 
					startNode = iArc.getSourceNode();
					for (INode contextToken : contextTokens) {
						if (startNode.equals(contextToken)) {
							counter++;
						}
					}
				}
			}
			textPart.add(startNode);
		}
		
		// iterate till the textPart begins with a verb phrase
		while (!startNode.getAttributeValue("chunkName").equals("VP")) {
			if (!startNode.getIncomingArcs().isEmpty()) {
				for (IArc iArc : startNode.getIncomingArcs()) {
					if (iArc.getType().getName().equals("relation")) {
						startNode = iArc.getSourceNode();
					}
 				}
				textPart.add(startNode);
			}
			else {
				break;
			}
		}
	
		// iterate till the textPart ends with a complete noun phrase
		boolean search = true;
		while (search) {
			if (!endNode.getOutgoingArcs().isEmpty() && search) {
				boolean inNP = false;
				if (!inNP) { // add nodes till a NP is reached
					for (IArc iArc : endNode.getOutgoingArcs()) {
						if (iArc.getType().getName().equals("relation")) {
							endNode = iArc.getTargetNode();
						}
			 		}
					textPart.add(0, endNode);
					if (endNode.getAttributeValue("chunkName").equals("NP")) {
						inNP = true;
					}
				} 
				else {
					for (IArc iArc : endNode.getOutgoingArcs()) {
						if (iArc.getType().getName().equals("relation")) {
							endNode = iArc.getTargetNode();
						}
			 		}
					if (!endNode.getAttributeValue("chunkName").equals("NP")) {
						search = false; // because the next NP of the contextEntity is completely included in the textPart
					}
					else {
						textPart.add(0, endNode);
					}
				}
			} 
			else {
				search = false; // because there are no tokens left in the graph
			}
		}
		return textPart;
	}
	
	// add all tokens of a contextEntity to a list
	protected void detectContextEntityTokens(List<INode> list, INode contextEntity) {
		for (IArc iArc : contextEntity.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("reference")) {
				list.add(iArc.getTargetNode());
				detectContextEntityTokens(list, iArc.getTargetNode());
			} else  {
				// do nothing - will lead to the end of this loop, if there is no more reference 
			}
		}
	}
	
	// returns all tokens of a contextEntity as a String
	protected String detectContextEntityElements(INode contextEntity) {
		for (IArc iArc : contextEntity.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("reference"))
				return "" + iArc.getTargetNode() + "\n" + detectContextEntityElements(iArc.getTargetNode());
		}
		return "";
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
