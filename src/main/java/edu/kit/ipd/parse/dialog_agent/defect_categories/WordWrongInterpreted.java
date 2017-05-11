package edu.kit.ipd.parse.dialog_agent.defect_categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.dialog_agent.AbstractDefectCategory;
import edu.kit.ipd.parse.dialog_agent.build_graph.BuildGraph;
import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;
import edu.kit.ipd.parse.dialog_agent.util.GainUserAnswer;
import edu.kit.ipd.parse.dialog_agent.util.GraphOperations;
import edu.kit.ipd.parse.dialog_agent.util.Synthesizer;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class WordWrongInterpreted extends AbstractDefectCategory {

	private static final Logger logger = LoggerFactory.getLogger(WordWrongInterpreted.class);
	
	IGraph graph;
	List<INode> lowConfidenceMainNodes; 	// list that contains all questionable nodes
	private Properties props;	// represents the property file
	protected double reliableConfidenceThreshold;	// threshold for valid words
	protected double twiceSameWordConfidenceThreshold;	// threshold for words if they are equal in the question and the answer
	
	@Override
	protected boolean analyseGraph(IGraph graph) {
		props = ConfigManager.getConfiguration(getClass(), "DialogAgent");
		reliableConfidenceThreshold = Double.parseDouble(props.getProperty("RELIABLE_ASR_CONFIDENCE_THRESHOLD"));
		twiceSameWordConfidenceThreshold = Double.parseDouble(props.getProperty("TWICE_SAME_WORD_ASR_CONFIDENCE_THRESHOLD"));

		for (INode iNode : graph.getNodes()) 
			logger.debug("initial nodes " + iNode);
		
		this.graph = graph;
		lowConfidenceMainNodes = getTokenNodesWithLowConfidence();	
		if (lowConfidenceMainNodes.isEmpty()) 
			return false; 
		return true;
//		return false;
	}
	
	@Override
	protected void solveDefectCategory() {
		logger.info("Start solving asr issues - words with low confidence detected");
		
		// add the attributes nextNodeValue (contains an additional node after the referenced one) and 
		// removeNode (indicates that this node should be removed)
		for (INode iNode : graph.getNodes()) {
			if (iNode.getType().getName().equals("token")) { 
				if (!iNode.getType().containsAttribute("nextNodeValue", "String")) {
					iNode.getType().addAttributeToType("String", "nextNodeValue");	
				}
				if (!iNode.getType().containsAttribute("removeNode", "boolean")) {
					iNode.getType().addAttributeToType("boolean", "removeNode");	
					iNode.setAttributeValue("removeNode", false);
				} else {
					if (iNode.getAttributeValue("removeNode") == null) {
						iNode.setAttributeValue("removeNode", false);					
					}
				}
			}
		}
		
		// sort lowConfidenceMainNodes by asrConfidence attribute
		Collections.sort(lowConfidenceMainNodes, new Comparator<INode>() {
			@Override
			public int compare(INode node1, INode node2) {
				Double d1 = Double.parseDouble(node1.getAttributeValue("asrConfidence").toString());
				Double d2 = Double.parseDouble(node2.getAttributeValue("asrConfidence").toString());
				return d1.compareTo(d2);
			}
		});
		
		// log all lowConfidenceMainNodes
		for (INode lowConfidenceMainNode : lowConfidenceMainNodes) {
			logger.debug("Nodes with low asrConfidence " + lowConfidenceMainNode);
		}
		
		// ask to verify a phrase
		for (INode lowConfidenceMainNode : lowConfidenceMainNodes) {
			if ((boolean) lowConfidenceMainNode.getAttributeValue("removeNode")) {
				// do not process this node, this node should be removed anyway
			} else {
				askForPhraseVerification(lowConfidenceMainNode);
			}	
		}
		
		// rebuild graph with processed asr correction
		String newGraphText = "";
		for (INode textNode : graph.getNodes()) {
			if (textNode.getType().getName().equals("token")) {
				if ((boolean) textNode.getAttributeValue("removeNode")) {
					// doing nothing will skip this node
				} else if (textNode.getAttributeValue("value").equals("%HESITATION")) {
					// do not add this node, just check if this node contains another node and if true, integrate this node
					if (textNode.getAttributeValue("nextNodeValue") != null && !textNode.getAttributeValue("nextNodeValue").equals("%HESITATION")) {
						newGraphText = newGraphText + textNode.getAttributeValue("nextNodeValue").toString() + " ";	
					}
				} else if (textNode.getAttributeValue("value").equals("<eps>")) {
					// do not add this node, just check if this node contains another node and if true, integrate this node
					if (textNode.getAttributeValue("nextNodeValue") != null && !textNode.getAttributeValue("nextNodeValue").equals("<eps>")) {
						newGraphText = newGraphText + textNode.getAttributeValue("nextNodeValue").toString() + " ";	
					}
				} else {
					newGraphText = newGraphText + textNode.getAttributeValue("value").toString() + " ";
					if (textNode.getAttributeValue("nextNodeValue") != null) {
						newGraphText = newGraphText + textNode.getAttributeValue("nextNodeValue").toString() + " ";	
					}
				}
			}
		}
		
		// express the following statement as information for the user
		String motivation = "You processed all speech recognizer problems. Good job!";
		Synthesizer.enunciateQuestion(motivation);
				
		// building the new corrected graph
		BuildGraph bg = new BuildGraph(newGraphText, false);
		graph = bg.getGraph();
		logger.info("Asr correction done - new graph built");
		for (INode iNode : graph.getNodes()) {
			logger.debug("rebuilt graph nodes " + iNode);
		}
	}
	
	// ask to verify a phrase
	protected void askForPhraseVerification(INode iNode) {
		logger.debug("Try to Verify - " + iNode);
		
		// detect the environment of the questionable part
		INode startNode = GraphOperations.getPreviousVerbNode(iNode);
		INode endNode = GraphOperations.getSubsequentNounNode(iNode);
		List<INode> textPart = new ArrayList<INode>();
		textPart.add(startNode);
		while (!startNode.equals(endNode)) {
			for (IArc iArc : startNode.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					startNode = iArc.getTargetNode();
					textPart.add(startNode);
				}
			}
		}
		
		for (int u = 0; u < 2; u++) {
			// create the question
			String question = "";
			if (u == 1) {
				question = question + "Sorry, I did not get it. Just repeat the following words: ";
			} else {
				question = question + "Please repeat the following part of your statement: ";			
			}
			for (INode textNode : textPart) {
				if (textNode.getAttributeValue("value").equals("%HESITATION")) {
					// do not add hesitations
				} else if (textNode.getAttributeValue("value").equals("<eps>")) {
					// do not add <eps> (it is similar to hesitations)
				} else {	
					question = question + textNode.getAttributeValue("value") + " ";
					logger.debug("Question " + textNode);	
				}
			}
			
			// ask the question and get the answer as a graph
			Synthesizer.enunciateQuestion(question);
			IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();
			List<INode> answer = new ArrayList<INode>();
			for (INode node : userAnswerGraph.getNodes()) {
				if (node.getType().getName().equals("token")) {
					answer.add(node);
					logger.debug("Answer " + node);			
				}
			}
			
			// this defect category tries to solve eleven different cases		 
			if (textPart.size() == answer.size()) {
				if (textPart.size() < 4) {
					for (int i = 0; i < textPart.size(); i++) { // case short question
						if (lowConfidenceMainNodes.contains(textPart.get(i))) {
							if ((Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
								// replace
								textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));
								u = 2;
								logger.debug("case short question");
							}
						}
					}
				} else {
					for (int i = 0; i < textPart.size(); i++) {
						if (textPart.get(i).equals(iNode)) { // i have the node to verify
							if (i > 0 && i <= (textPart.size() - 2)) { // case b -> y
								if (textPart.get(i - 1).getAttributeValue("value").equals(answer.get(i - 1).getAttributeValue("value")) &&
										textPart.get(i + 1).getAttributeValue("value").equals(answer.get(i + 1).getAttributeValue("value"))) {
									if ((Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
										// replace
										textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));
										u = 2;
										logger.debug("case b -> y");
									} else if (textPart.get(i).getAttributeValue("value").equals(answer.get(i).getAttributeValue("value")) && 
											(Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > twiceSameWordConfidenceThreshold)) {
										// replace
										textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));	
										u = 2;			
										logger.debug("case b -> y");				
									}				
								}
							} else if (i == 0 && i <= (textPart.size() - 3)) { // case a -> x
								if (textPart.get(i + 1).getAttributeValue("value").equals(answer.get(i + 1).getAttributeValue("value")) &&
										textPart.get(i + 2).getAttributeValue("value").equals(answer.get(i + 2).getAttributeValue("value"))) {
									if ((Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
										// replace
										textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));
										u = 2;
										logger.debug("case a -> x");	
									} else if (textPart.get(i).getAttributeValue("value").equals(answer.get(i).getAttributeValue("value")) && 
											(Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > twiceSameWordConfidenceThreshold)) {
										// replace
										textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));
										u = 2;		
										logger.debug("case a -> x");							
									}			
								}
							} else if (i > 0 && i == (textPart.size() - 1)) { // case c -> z
								if (textPart.get(i - 1).getAttributeValue("value").equals(answer.get(i - 1).getAttributeValue("value")) &&
										textPart.get(i - 2).getAttributeValue("value").equals(answer.get(i - 2).getAttributeValue("value"))) {
									if ((Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
										// replace
										textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));
										u = 2;
										logger.debug("case c -> z");	
									} else if (textPart.get(i).getAttributeValue("value").equals(answer.get(i).getAttributeValue("value")) && 
											(Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > twiceSameWordConfidenceThreshold)) {
										// replace
										textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));	
										u = 2;				
										logger.debug("case c -> z");				
									}			
								}
							}	
							break;
						}
					}	
				}
			} else if ((textPart.size() - 1) == answer.size() && textPart.size() > 3) {
				if (lowConfidenceMainNodes.contains(textPart.get(0))) {
					if (textPart.get(2).getAttributeValue("value").equals(answer.get(1).getAttributeValue("value")) &&
							textPart.get(3).getAttributeValue("value").equals(answer.get(2).getAttributeValue("value"))) {
						if ((Double.parseDouble(answer.get(0).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
							// replace
							textPart.get(0).setAttributeValue("value", answer.get(0).getAttributeValue("value"));
							// remove 
							textPart.get(1).setAttributeValue("removeNode", true);
							u = 2;
							logger.debug("case a,b -> x (1)");
						}
					}
				} else if (lowConfidenceMainNodes.contains(textPart.get(textPart.size() - 1))) { 
					if (textPart.get(textPart.size() - 4).getAttributeValue("value").equals(answer.get(textPart.size() - 4).getAttributeValue("value")) &&
							textPart.get(textPart.size() - 3).getAttributeValue("value").equals(answer.get(textPart.size() - 3).getAttributeValue("value"))) {
						if ((Double.parseDouble(answer.get(textPart.size() - 2).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
							// replace
							textPart.get(textPart.size() - 2).setAttributeValue("value", answer.get(textPart.size() - 2).getAttributeValue("value"));
							// remove 
							textPart.get(textPart.size() - 1).setAttributeValue("removeNode", true);
							u = 2;
							logger.debug("case c,d -> z (1)");
						}
					}
				}
					
				for (int i = 1; i < textPart.size() - 1; i++) { // special indices are important and it works even at the boarders of the graph
					if (textPart.get(i).equals(iNode)) { // i have the node to verify
						if (lowConfidenceMainNodes.contains(textPart.get(i - 1))) {
							if (i > 1 && i <= textPart.size() - 2) { // case b,c -> y
								if (textPart.get(i - 2).getAttributeValue("value").equals(answer.get(i - 2).getAttributeValue("value")) &&
										textPart.get(i + 1).getAttributeValue("value").equals(answer.get(i).getAttributeValue("value"))) {
									if ((Double.parseDouble(answer.get(i - 1).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
										// replace
										textPart.get(i - 1).setAttributeValue("value", answer.get(i - 1).getAttributeValue("value"));
										// remove 
										textPart.get(i).setAttributeValue("removeNode", true);
										u = 2;
										logger.debug("case b,c -> y (1)");
									}
								}
							} else if (i == 1 && i <= textPart.size() - 3) { // case a,b -> x
								if (textPart.get(i + 1).getAttributeValue("value").equals(answer.get(i).getAttributeValue("value")) &&
										textPart.get(i + 2).getAttributeValue("value").equals(answer.get(i + 1).getAttributeValue("value"))) {
									if ((Double.parseDouble(answer.get(i - 1).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
										// replace
										textPart.get(i - 1).setAttributeValue("value", answer.get(i - 1).getAttributeValue("value"));
										// remove 
										textPart.get(i).setAttributeValue("removeNode", true);
										u = 2;
										logger.debug("case a,b -> x (2)");
									}
								}
							} 
						} else if (lowConfidenceMainNodes.contains(textPart.get(i + 1))) {
							if (i > 0 && i <= textPart.size() - 3) { // case b,c -> y
								if (textPart.get(i - 1).getAttributeValue("value").equals(answer.get(i - 1).getAttributeValue("value")) &&
										textPart.get(i + 2).getAttributeValue("value").equals(answer.get(i + 1).getAttributeValue("value"))) {
									if ((Double.parseDouble(answer.get(i - 1).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
										// replace
										textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));
										// remove 
										textPart.get(i + 1).setAttributeValue("removeNode", true);
										u = 2;
										logger.debug("case b,c -> y (2)");
									}
								}
							} else if (i > 2 && i == textPart.size() - 2) { // case c,d -> z
								if (textPart.get(i - 1).getAttributeValue("value").equals(answer.get(i - 1).getAttributeValue("value")) &&
										textPart.get(i - 2).getAttributeValue("value").equals(answer.get(i - 2).getAttributeValue("value"))) {
									if ((Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
										// replace
										textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));
										// remove 
										textPart.get(i + 1).setAttributeValue("removeNode", true);
										u = 2;
										logger.debug("case c,d -> z (2)");
									}
								}
							}
						}
						break;
					}
				}
			} else if (textPart.size() == (answer.size() - 1) && textPart.size() > 3) {
				for (int i = 0; i < textPart.size(); i++) {
					if (textPart.get(i).equals(iNode)) { // i have the node to verify
						if (i > 0 && i <= textPart.size() - 2) { // case b -> x,y
							if (textPart.get(i - 1).getAttributeValue("value").equals(answer.get(i - 1).getAttributeValue("value")) &&
									textPart.get(i + 1).getAttributeValue("value").equals(answer.get(i + 2).getAttributeValue("value"))) {
								if ((Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold) &&
										(Double.parseDouble(answer.get(i + 1).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
									// replace
									textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));
									// insert node
									textPart.get(i).setAttributeValue("nextNodeValue", answer.get(i + 1).getAttributeValue("value").toString());
									u = 2;
									logger.debug("case b -> x,y");
								} 			
							}
						} else if (i == 0 && i <= textPart.size() - 3) { // case a -> w,x
							if (textPart.get(i + 1).getAttributeValue("value").equals(answer.get(i + 2).getAttributeValue("value")) &&
									textPart.get(i + 2).getAttributeValue("value").equals(answer.get(i + 3).getAttributeValue("value"))) {
								if ((Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold) &&
										(Double.parseDouble(answer.get(i + 1).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
									// replace
									textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));
									// insert node
									textPart.get(i).setAttributeValue("nextNodeValue", answer.get(i + 1).getAttributeValue("value").toString());
									u = 2;
									logger.debug("case a -> w,x");
								} 			
							}
						} else if (i > 0 && i == textPart.size() - 1) { // case c -> y,z
							if (textPart.get(i - 1).getAttributeValue("value").equals(answer.get(i - 1).getAttributeValue("value")) &&
									textPart.get(i - 2).getAttributeValue("value").equals(answer.get(i - 2).getAttributeValue("value"))) {
								if ((Double.parseDouble(answer.get(i).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold) &&
										(Double.parseDouble(answer.get(i + 1).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
									// replace
									textPart.get(i).setAttributeValue("value", answer.get(i).getAttributeValue("value"));
									// insert node
									textPart.get(i).setAttributeValue("nextNodeValue", answer.get(i + 1).getAttributeValue("value").toString());
									u = 2;
									logger.debug("case c -> y,z");
								} 			
							}
						}	
						break;
					}
				}	
			} else if (u != 2) { // if nothing worked, check match of the nodes before and after the viewed node - if match and high confidence replace
				for (int i = 0; i < textPart.size(); i++) {
					if (textPart.get(i).equals(iNode)) { // i have the node to verify
						if (i > 1 && i < (textPart.size() - 1)) {
							for (int z = 0; z < answer.size(); z++) {
								if (answer.get(z).getAttributeValue("value").equals(textPart.get(i - 1).getAttributeValue("value"))) {
									if (z <= answer.size() - 3 && answer.get(z + 2).getAttributeValue("value").equals(textPart.get(i + 1).getAttributeValue("value"))) {
										if ((Double.parseDouble(answer.get(z + 1).getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold)) {
											// replace
											textPart.get(i).setAttributeValue("value", answer.get(z + 1).getAttributeValue("value"));
											u = 2;
											logger.debug("case fallback");
											break;
										}
									}
								}
							}
						}
					}
				}				
			} else {
				// not understood ask again
			}
		}
	}
	
	// returns all nodes with a confidence value lower than the threshold
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
}
