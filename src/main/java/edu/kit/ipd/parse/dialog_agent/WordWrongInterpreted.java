package edu.kit.ipd.parse.dialog_agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;
import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;
import edu.kit.ipd.parse.dialog_agent.util.GainUserAnswer;
import edu.kit.ipd.parse.dialog_agent.util.GraphOperations;
import edu.kit.ipd.parse.dialog_agent.util.Synthesizer;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class WordWrongInterpreted extends AbstractDefectCategory {

	IGraph graph;
	List<INode> lowConfidenceMainNodes;
	private Properties props;
	protected double reliableConfidenceThreshold;
	protected double minimumConfidenceThreshold;
	protected double repeatQuestionConfidenceThreshold;
	
	@Override
	protected boolean analyseGraph(IGraph graph) {
		props = ConfigManager.getConfiguration(getClass(), "DialogAgent");
		reliableConfidenceThreshold = Double.parseDouble(props.getProperty("RELIABLE_ASR_CONFIDENCE_THRESHOLD"));
		minimumConfidenceThreshold = Double.parseDouble(props.getProperty("MINIMUM_ASR_CONFIDENCE_THRESHOLD"));
		repeatQuestionConfidenceThreshold = Double.parseDouble(props.getProperty("REPEAT_QUESTION_ASR_CONFIDENCE_THRESHOLD"));
		
		this.graph = graph;
		lowConfidenceMainNodes = getTokenNodesWithLowConfidence();	
		if (lowConfidenceMainNodes.isEmpty()) 
			return false; 
		return true;
	}
	
	@Override
	protected void solveDefectCategory() {
		System.out.println(graph.showGraph());
		for (INode iNode : graph.getNodes()) 
			System.out.println(iNode);
		
		// sort lowConfidenceMainNodes by asrConfidence attribute
		Collections.sort(lowConfidenceMainNodes, new Comparator<INode>() {
			@Override
			public int compare(INode node1, INode node2) {
				Double d1 = Double.parseDouble(node1.getAttributeValue("asrConfidence").toString());
				Double d2 = Double.parseDouble(node2.getAttributeValue("asrConfidence").toString());
				return d1.compareTo(d2);
			}
		});
		
		for (INode lowConfidenceMainNode : lowConfidenceMainNodes) {
			System.out.println("lowConfidenceNode " + lowConfidenceMainNode);
		}
		
		// that attribute is necessary to check if the node contains a further node
		for (INode iNode : graph.getNodes()) { 								
			iNode.getType().addAttributeToType("String", "nextNodeValue");
			iNode.getType().addAttributeToType("boolean", "removeNode");
			iNode.setAttributeValue("removeNode", false);
		}
		
		// ask to verify a phrase
//		int counterOfASRQuestions = 0;		// just for the evaluation to prevent user from endless asr verifications
		for (INode lowConfidenceMainNode : lowConfidenceMainNodes) {
			// if the node is processed already do not process again (because we can solve two nodes at a time)
			if (Double.parseDouble(lowConfidenceMainNode.getAttributeValue("asrConfidence").toString()) < reliableConfidenceThreshold) {
				askForPhraseVerification(lowConfidenceMainNode, false);
//				askYesNoQuestion(lowConfidenceMainNode);
//				counterOfASRQuestions++;
//				if (counterOfASRQuestions >= 5) {
//					break;
//				}				
			}
		}
		
		System.out.println(graph.showGraph());
		for (INode iNode : graph.getNodes()) 
			System.out.println(iNode);
		
		// rebuild graph with processed asr correction
		System.out.println("Build graph again!");
		String newGraphText = "";
		for (INode textNode : graph.getNodes()) {
			if (textNode.getType().getName().equals("token")) {
				if ((boolean) textNode.getAttributeValue("removeNode")) {
					// doing nothing will skip this node
				} else {
					newGraphText = newGraphText + textNode.getAttributeValue("value").toString() + " ";
					if (textNode.getAttributeValue("nextNodeValue") != null) {
						newGraphText = newGraphText + textNode.getAttributeValue("value").toString() + " ";	
					}
				}
			}
		}
		System.out.println(newGraphText);
				
		BuildGraph bg = new BuildGraph(newGraphText);
		IGraph graph = null;
		try {
			graph = bg.getGraph();
		} catch (MissingDataException mde) {
			mde.printStackTrace();
		}
		System.out.println(graph.showGraph());
	}
	
	// ask to verify a phrase
	protected void askForPhraseVerification(INode iNode, boolean secondTime) {
		System.out.println(" nowe");
		INode startNode = GraphOperations.getPreviousVerbNode(iNode);
		INode endNode = GraphOperations.getSubsequentNounNode(iNode);
		List<INode> textPart = new ArrayList<INode>();
		while (!startNode.equals(endNode)) {
			for (IArc iArc : startNode.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					System.out.println(startNode);
					textPart.add(startNode);
					startNode = iArc.getTargetNode();
				}
			}
		}
		
		String question = "Please repeat the following part of your words: ";
		for (INode textNode : textPart) {
			question = question + textNode.getAttributeValue("value") + " ";
		}
		
		Synthesizer.enunciateQuestion(question);
		IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();
		System.out.println(userAnswerGraph.showGraph());
		List<INode> answer = new ArrayList<INode>();
		for (INode node : userAnswerGraph.getNodes()) {
			if (node.getType().getName().equals("token")) {
				answer.add(node);
				System.out.println(node);			
			}
		}
		
		// compare textPart with answer
		if (answer.size() >= textPart.size()) {
			int u = -1;
			for (int i = 0; i < textPart.size(); i++) {
				u++;
				System.out.println("graph " + textPart.get(i).getAttributeValue("value"));
				System.out.println("answer " + answer.get(u).getAttributeValue("value"));
				
				if (textPart.get(i).getAttributeValue("value").equals(answer.get(u).getAttributeValue("value"))) { 
					System.out.println("match");
					// if textPart was low confidence try to verify 
					if (lowConfidenceMainNodes.contains(textPart.get(i))) {
						// if the system understood the same not verified value twice
						if (Double.parseDouble(answer.get(u).getAttributeValue("asrConfidence").toString()) > repeatQuestionConfidenceThreshold) {
							textPart.get(i).setAttributeValue("asrConfidence", 1.0);
							INode verificationNode = textPart.get(i);
							GraphOperations.asrVerifyNode(graph, verificationNode);						
							// the node should replaced with two nodes
							if (answer.size() > u + 1 && textPart.get(i).getAttributeValue("value").equals(answer.get(u + 1).getAttributeValue("value"))) { 
								if (Double.parseDouble(answer.get(u + 1).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold) {
									// add additional node as attribute (instead of complex graph operation)	
									textPart.get(i).getType().addAttributeToType("nextNodeValue", answer.get(u + 1).getAttributeValue("value").toString());
								}
							}
						} 
					} else {
						// do nothing just go to the next nodes
					}
				} else {
					if (lowConfidenceMainNodes.contains(textPart.get(i))) {
						// if the system understood the second time the word with a high confidence 
						if (Double.parseDouble(answer.get(u).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold) {
							// replace node = replace value, because the graph is build again with the containing values
							textPart.get(i).setAttributeValue("asrConfidence", 1.0);
							textPart.get(i).setAttributeValue("value", answer.get(u).getAttributeValue("value"));
							
							// the node should replaced with two nodes
							if (answer.size() > u + 1 && textPart.get(i).getAttributeValue("value").equals(answer.get(u + 1).getAttributeValue("value"))) { 
								if (Double.parseDouble(answer.get(u + 1).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold) {
									// add additional node as attribute (instead of complex graph operation)	
									textPart.get(i).getType().addAttributeToType("nextNodeValue", answer.get(u + 1).getAttributeValue("value").toString());
								}
							}
						} else if (Double.parseDouble(textPart.get(i).getAttributeValue("asrConfidence").toString()) > repeatQuestionConfidenceThreshold) {
							askYesNoQuestion(textPart.get(i)); // we ask for the graph node, because in many cases we just try to verify correct nodes
						} else { 
							// ask again but just once
							if (!secondTime) {
								askForPhraseVerification(iNode, true); 
							}
						}
					}
				}
			}
		} else { // textPart.size() > answer.size() (if this is correct nodes should be merged)
			int u = -1;			
			for (int i = 0; i < textPart.size(); i++) { 
				u++;
				if (answer.size() == 0) {
					break;
				}
				if (lowConfidenceMainNodes.contains(textPart.get(i)) && lowConfidenceMainNodes.contains(textPart.get(i + 1))) {
					if (u < answer.size() - 1 && Double.parseDouble(answer.get(u).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold) {
						if (i > 0 && i < textPart.size() - 2) {
							if (u > 0 && u < answer.size() - 2 && textPart.get(i - 1).getAttributeValue("value").equals(answer.get(u - 1).getAttributeValue("value"))
									&& textPart.get(i + 2).getAttributeValue("value").equals(answer.get(u + 1).getAttributeValue("value"))) {
								textPart.get(i).setAttributeValue("value", answer.get(u));
								textPart.get(i + 1).setAttributeValue("removeNode", true);
								u--;
							}
						} else if (i == 0 && i < textPart.size() - 2) {
							if (u < answer.size() - 2 && textPart.get(i + 2).getAttributeValue("value").equals(answer.get(u + 1).getAttributeValue("value"))) {
								textPart.get(i).setAttributeValue("value", answer.get(u));
								textPart.get(i + 1).setAttributeValue("removeNode", true);
								u--;
							}	
						} else if (i > 0 && i == textPart.size() - 2) {
							if (u == answer.size() - 1 && textPart.get(i - 1).getAttributeValue("value").equals(answer.get(u - 1).getAttributeValue("value"))) {
								textPart.get(i).setAttributeValue("value", answer.get(u));
								textPart.get(i + 1).setAttributeValue("removeNode", true);
								u--;
							}
						} else {
							// in other cases we do not merge, otherwise we merge might not be useful			
						} 
					} else {
						// ask again but just once
						if (!secondTime) {
							askForPhraseVerification(iNode, true);
						}
					}
				}
			}
		}
	}
	
	// returns all nodes with confidence between given confidence thresholds
	protected List<INode> getTokenNodesWithLowConfidence() {
		List<INode> lowConfMainNodes = new ArrayList<INode>();
		for (Iterator<INode> iterator = graph.getNodes().iterator(); iterator.hasNext();) {
			INode iNode = iterator.next();
			if (iNode.getType().getName().toString().equals("token")
					&& ((Double) iNode.getAttributeValue("asrConfidence") < reliableConfidenceThreshold)) {
				lowConfMainNodes.add(iNode);
			}
		}
		return lowConfMainNodes;
	}

	// returns the alternative nodes for a node
	protected List<INode> determineAlternatives(INode iNode) {
		List<INode> alternatives = new ArrayList<INode>();
		System.out.println(iNode.getNumberOfOutgoingArcs()); // ########
		for (IArc iArc : iNode.getOutgoingArcs()) {
			double targetNodeConfidence = (Double) iArc.getTargetNode().getAttributeValue("asrConfidence");
			if (iArc.getTargetNode().getType().getName().toString().equals("alternative_token")
					&& minimumConfidenceThreshold < targetNodeConfidence
					&& targetNodeConfidence < reliableConfidenceThreshold) {
				alternatives.add(iArc.getTargetNode());
				System.out.println(iArc.getTargetNode().toString()); // ########
			}
		}
		return alternatives;
	}

	// asks a yes or no question BUT can also initiate open questions
	protected void askYesNoQuestion(INode iNode) {
		System.out.println("low confidence " + iNode.toString());
		// replace with a question pattern which uses the other nodes of this
		// sentence
		String question = "Did you mean " + iNode.getAttributeValue("value") + "?";
		Synthesizer.enunciateQuestion(question);
		IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();

		if (examineYesNoAnswer(userAnswerGraph).equals("YES")) {
			GraphOperations.asrVerifyNode(graph, iNode);
		} else if (examineYesNoAnswer(userAnswerGraph).equals("NO")) {
			askOpenQuestion(iNode);
		} else {
			// the user did not answer properly lets ask again with advice
			String adviceQuestion = "Please answer the following question just with yes or no. " + question;
			Synthesizer.enunciateQuestion(adviceQuestion);
			userAnswerGraph = GainUserAnswer.getUserAnswer();
			if (examineYesNoAnswer(userAnswerGraph).equals("YES")) {
				GraphOperations.asrVerifyNode(graph, iNode);
			} else if (examineYesNoAnswer(userAnswerGraph).equals("NO")) {
				askOpenQuestion(iNode);
			} else {
				// break the user did not answered properly again we stop here
			}
		}
	}
	
//	// asks a yes or no question BUT can also initiate open questions
//	protected void askYesNoQuestion(INode iNode, INode targetNode) {
//		// replace with a question pattern which uses the other nodes of this sentence
//		String question = "Did you mean " + iNode.getAttributeValue("value") + "?";
//		Synthesizer.enunciateQuestion(question);
//		IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();
//
//		if (examineYesNoAnswer(userAnswerGraph).equals("YES")) {
//			targetNode.setAttributeValue("value", iNode.getAttributeValue("value").toString()); // replace node, by replacing value
//		} else if (examineYesNoAnswer(userAnswerGraph).equals("NO")) {
//			askOpenQuestion(targetNode);
//		} else {
//			// the user did not answer properly lets ask again with advice
//			String adviceQuestion = "Please answer the following question just with yes or no. " + question;
//			Synthesizer.enunciateQuestion(adviceQuestion);
//			userAnswerGraph = GainUserAnswer.getUserAnswer();
//			if (examineYesNoAnswer(userAnswerGraph).equals("YES")) {
//				targetNode.setAttributeValue("value", iNode.getAttributeValue("value").toString()); // replace node, by replacing value
//			} else if (examineYesNoAnswer(userAnswerGraph).equals("NO")) {
//				askOpenQuestion(targetNode);
//			} else {
//				// break the user did not answered properly again we stop here
//			}
//		}
//	}

	// examine if the answer was yes or no
	protected String examineYesNoAnswer(IGraph graph) {
		if (GraphOperations.countNodes("token", graph) == 1) { // if the answer
																// contains one
																// word
			INode iNode = (INode) graph.getNodes().iterator().next();
			if (iNode.getAttributeValue("value").equals("yes")) {
				return "YES";
			} else if (iNode.getAttributeValue("value").equals("no")) {
				return "NO";
			} else {
				return "WORD_NOT_UNDERSTOOD";
				// add advice that this is a yes or no question
			}
		} else {
			return "TO_MANY_WORDS";
			// add advice that this is a yes or no question (just one word)
		}
	}

	// examine if an open question consists of one word
	protected String examineOpenAnswer(IGraph graph) {
		if (GraphOperations.countNodes("token", graph) == 1) { // if the answer
																// contains one
																// word
			INode iNode = graph.getNodes().iterator().next();
			if (Double.parseDouble(iNode.getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold) {
				return "REPLACE";
			} else {
				return "WORD_NOT_UNDERSTOOD";
			}
		} else {
			return "TO_MANY_WORDS";
		}
	}

	// asks open questions
	protected void askOpenQuestion(INode iNode) {
		String openQuestion = "What did you mean instead of " + iNode.getAttributeValue("value") + "? ";
		Synthesizer.enunciateQuestion(openQuestion);
		IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();

		if (examineOpenAnswer(userAnswerGraph).equals("REPLACE")) {
			INode answerNode = userAnswerGraph.getNodes().iterator().next();
			iNode.setAttributeValue("value", answerNode.getAttributeValue("value").toString()); // replace node, by replacing value
		} else if (examineOpenAnswer(userAnswerGraph).equals("WORD_NOT_UNDERSTOOD")) {
			String adviceQuestion = "Sorry, I did not understand you. Please say it again.";
			Synthesizer.enunciateQuestion(adviceQuestion);
			userAnswerGraph = GainUserAnswer.getUserAnswer();
			if (examineOpenAnswer(userAnswerGraph).equals("REPLACE")) {
				INode answerNode = userAnswerGraph.getNodes().iterator().next();
				iNode.setAttributeValue("value", answerNode.getAttributeValue("value").toString()); // replace node, by replacing value
			} else {
				// break the user did not answered properly again we stop here
			}
		} else if (examineOpenAnswer(userAnswerGraph).equals("TO_MANY_WORDS")) {
			String adviceQuestion = "Please answer again but just with one word.";
			Synthesizer.enunciateQuestion(adviceQuestion);
			userAnswerGraph = GainUserAnswer.getUserAnswer();
			if (examineOpenAnswer(userAnswerGraph).equals("REPLACE")) {
				INode answerNode = userAnswerGraph.getNodes().iterator().next();
				iNode.setAttributeValue("value", answerNode.getAttributeValue("value").toString()); // replace node, by replacing value
			} else {
				// break the user did not answered properly again we stop here
			}
		}
	}
}
