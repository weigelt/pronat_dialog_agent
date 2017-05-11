package edu.kit.ipd.parse.dialog_agent.defect_categories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.dialog_agent.AbstractDefectCategory;
import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;
import edu.kit.ipd.parse.dialog_agent.util.GainUserAnswer;
import edu.kit.ipd.parse.dialog_agent.util.Synthesizer;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class CorefWrongReferenced extends AbstractDefectCategory {

	private static final Logger logger = LoggerFactory.getLogger(CorefWrongReferenced.class);
	
	private IGraph graph;
	private Properties props;	// represents the property file
	private double anaphoraReferentCorrectThreshold;	// threshold for a correct coreference
	private double anaphoraReferentMostProbablyCorrectThreshold;	// threshold for an almost correct coreference
	
	private List<INode> questionableContextEntities;
	
	@Override
	protected boolean analyseGraph(IGraph graph) {
		props = ConfigManager.getConfiguration(getClass(), "DialogAgent");
		anaphoraReferentCorrectThreshold = Double.parseDouble(props.getProperty("ANAPHORA_REFERENT_CORRECT_THRESHOLD"));
		anaphoraReferentMostProbablyCorrectThreshold = Double.parseDouble(props.getProperty("ANAPHORA_REFERENT_MOST_PROBABLY_CORRECT_THRESHOLD"));
		
		this.graph = graph;
		boolean result = false;
		questionableContextEntities = new ArrayList<INode>();
		
		// log the coreference arcs and their context
		logCoreferenceContext(graph);
		
		// add the attribute confidenceVerified (with default value false) to the contextRelation arcs, which contain the anaphoraReferent arcs 
		for (IArc iArc : graph.getArcs()) {
			if (iArc.getType().getName().equals("contextRelation")) { 
				if (!iArc.getType().containsAttribute("confidenceVerified", "boolean")) {
					iArc.getType().addAttributeToType("boolean", "confidenceVerified");		
					iArc.setAttributeValue("confidenceVerified", false);
				} else {
					if (iArc.getAttributeValue("confidenceVerified") == null) {
						iArc.setAttributeValue("confidenceVerified", false);					
					}		
				}
			}
		}
		
		// get all contextEntities with outgoing anaphoraReferent arcs, except the already verified ones
		Set<INode> contextEntities = new HashSet<INode>();
		for (INode iNode : graph.getNodes()) { 
			if (iNode.getType().getName().equals("contextEntity")) { // get just the contextEntity nodes
				for (IArc iArc : iNode.getOutgoingArcs()) {
					if (iArc.getType().getName().equals("contextRelation")) { 
						if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { // with outgoing anaphoraReferent arcs
							if (!(boolean) iArc.getAttributeValue("confidenceVerified")) {
								contextEntities.add(iNode);
							}
						}
					}
				}
			}
		}
		
		// fills the list questionableContextEntities with contextEntities that are questionable
		for (INode iNode : contextEntities) {
			List<INode> confidenceCorrectList = new ArrayList<INode>();	
			List<INode> confidenceHighList = new ArrayList<INode>();
			List<INode> confidenceLowList = new ArrayList<INode>();
			for (IArc iArc : iNode.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("contextRelation")) { 
					if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
						double confidence = Double.parseDouble(iArc.getAttributeValue("confidence").toString());
						if (confidence == anaphoraReferentCorrectThreshold) {
							for (IArc iArcReference : iArc.getTargetNode().getOutgoingArcs()) {
								if (iArcReference.getType().getName().equals("reference")) {
									confidenceCorrectList.add(iArcReference.getTargetNode());
								}
							}
						}
						if (confidence < anaphoraReferentCorrectThreshold && confidence >= anaphoraReferentMostProbablyCorrectThreshold) {
							for (IArc iArcReference : iArc.getTargetNode().getOutgoingArcs()) {
								if (iArcReference.getType().getName().equals("reference")) {
									confidenceHighList.add(iArcReference.getTargetNode());	
								}
							}
						}
						if (confidence < anaphoraReferentMostProbablyCorrectThreshold) {
							for (IArc iArcReference : iArc.getTargetNode().getOutgoingArcs()) {
								if (iArcReference.getType().getName().equals("reference")) {
									confidenceLowList.add(iArcReference.getTargetNode());
								}
							}
						}
					}
				}
			}
			
			if (confidenceCorrectList.size() > 1) {	
				questionableContextEntities.add(iNode);
				result = true;
			} else if (confidenceCorrectList.size() == 0 && confidenceHighList.size() > 1) {
				questionableContextEntities.add(iNode);
				result = true;
			} else if (confidenceCorrectList.size() == 0 && confidenceHighList.size() == 0 && confidenceLowList.size() > 1) {
				questionableContextEntities.add(iNode);
				result = true;
			}
		}
		
		return result;
	}
	
	@Override
	protected void solveDefectCategory() {
		logger.info("Start solving coreference resolution issues - ambiguous coreference found");
		
		INode sourceContextEntity = questionableContextEntities.get(0);		// contextEntity which refers to multiple contextEntities
		INode sourceContextEntityToken = null; 		// represents the token of the contextEntity
		
		// this part extracts the tokens of the observed contextEntity (anaphoraReferent -> contextEntity -> reference -> tokens)
		List<INode> contextTokens = new ArrayList<INode>();		// this tokens are relevant to define the beginning of the question
		List<INode> targetEntities = new ArrayList<INode>();
		for (IArc iArc : sourceContextEntity.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("reference")) {
				sourceContextEntityToken = iArc.getTargetNode();
			}
			if (iArc.getType().getName().equals("contextRelation")) { 
				if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
					targetEntities.add(iArc.getTargetNode());
					for (IArc iArcTargetContextEntity : iArc.getTargetNode().getOutgoingArcs()) {
						if (iArcTargetContextEntity.getType().getName().equals("reference")) {
							contextTokens.add(iArcTargetContextEntity.getTargetNode());
						}
					}
				}
			}
		} 
		
		List<INode> textPart = getQuestionableTextPart(sourceContextEntity, contextTokens, sourceContextEntityToken);

		// detect, if the questionable personal pronoun occurs twice
		Map<INode, String> allContextEntities = new HashMap<INode, String>(); // contains all target contextEntities and their tokens as ONE String
		Map<INode, Integer> occurence = new HashMap<INode, Integer>(); // contains Strings of the contextEntities and the amount of occurence
		for (INode iNode : targetEntities) {
			allContextEntities.put(iNode, getTokensAsString(iNode));
			occurence.put(iNode, 1);
		}
		boolean sourceContextEntityMoreThanOnce = false;
		for (INode iNode : allContextEntities.keySet()) {
			if (getTokensAsString(sourceContextEntity).equals(allContextEntities.get(iNode))) {
				sourceContextEntityMoreThanOnce = true;
			}
		}

		// phrase open question
		String question = "In the following part, what does ";
		if (sourceContextEntityMoreThanOnce) {
			question = question + "the last mentioned ";
		} else {
			question = question + "the word ";			
		}
		List<INode> tokensEntityToAskForList = new ArrayList<INode>();
		detectContextEntityTokens(tokensEntityToAskForList, sourceContextEntity);	// explanation see comments of this method
		
		// log the source and target entities with confidence
		logger.info("Source entity " + sourceContextEntity);
		for (INode iNode : tokensEntityToAskForList) {
			logger.info("Possible target entity " + iNode);
			for (IArc iArc : iNode.getIncomingArcs()) {
				if (iArc.getType().getName().equals("contextRelation")) { 
					if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
						if (iArc.getSourceNode().equals(sourceContextEntity)) {
							logger.info("AnaphoraReferent confidence of this entity " + iArc.getAttributeValue("confidence"));
						}
					}
				}
			}
		}

		// go on phrasing the question
		if (tokensEntityToAskForList.size() > 1) {
			question = question + "phrase ";
			for (INode iNodeTokenToAskFor : tokensEntityToAskForList) {
				question = question + iNodeTokenToAskFor.getAttributeValue("value") + "  ";
			}
		} else if (tokensEntityToAskForList.size() == 1) {
			question = question + tokensEntityToAskForList.get(0).getAttributeValue("value") + "  ";
		} 
		question = question + "refer to?  ";
		for (int i = 0; i < textPart.size(); i++) {
			question = question + textPart.get(textPart.size() - i - 1).getAttributeValue("value") + "  ";
		}

		// log the question
		for (INode iNode : textPart) {
			logger.info("Coref question " + iNode);
		}

		// ask the user and get the answer as a graph
		Synthesizer.enunciateQuestion(question);
		IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();
		
		// log the answer
		for (INode iNode : userAnswerGraph.getNodes()) {
			logger.info("Coref answer " + iNode);
		}
		
		// try to extract the correct targetEntity
		List<INode> matchedContextEntityTokens = checkMatch(targetEntities, userAnswerGraph);
		for (INode iNode : matchedContextEntityTokens) {
			logger.info("Identified target context entity is " + iNode);
		}	
		
		boolean isNotSolved = true;
		boolean secondQuestionAsked = false;
		while (isNotSolved) {		// this loop is necessary if the question is not correctly answered yet
			if (matchedContextEntityTokens.isEmpty() || matchedContextEntityTokens.size() > 1) {
				if (!secondQuestionAsked) { 	// consider the !
					// ask again with alternatives
					question = "I did not understand you. Look, you mentioned the following " + targetEntities.size() + " entities. ";
					for (INode iNode : allContextEntities.keySet()) {
						question = question + allContextEntities.get(iNode) + ". ";
					}
					question = question + " Now, please tell me what does ";
					if (sourceContextEntityMoreThanOnce) {
						question = question + "the last mentioned ";
					} else {
						question = question + "the word ";						
					}
					question = question + getTokensAsString(sourceContextEntity) + "  refer to ";
					question = question + "in the following part.  ";
					for (int u = 0; u < textPart.size(); u++) {
						question = question + textPart.get(textPart.size() - u - 1).getAttributeValue("value") + " ";
					}

					// ask the user and get the answer as a graph
					Synthesizer.enunciateQuestion(question);
					userAnswerGraph = GainUserAnswer.getUserAnswer();
					matchedContextEntityTokens = checkMatch(targetEntities, userAnswerGraph);
					if (!matchedContextEntityTokens.isEmpty() && !(matchedContextEntityTokens.size() > 1)) {
						logger.info("Identified target context entity is " + matchedContextEntityTokens.get(0));
					}
					secondQuestionAsked = true;
				} else {	// if second question did not help
					// now drill down on every target contextEntity
					int counter = 0;
					for (int i = 0; i < targetEntities.size(); i++) {
						// phrase yes/no question to ask for one entity
						question = "You said: ";
						for (int u = 0; u < textPart.size(); u++) {
							question = question + textPart.get(textPart.size() - u - 1).getAttributeValue("value") + " ";
						}
						question = question + "Does " + getTokensAsString(sourceContextEntity) + " refer to " + getTokensAsString(targetEntities.get(i));

						// ask the user and get the answer as a graph
						Synthesizer.enunciateQuestion(question);
						userAnswerGraph = GainUserAnswer.getUserAnswer();
						
						// determine if the answer is yes or no
						if (userAnswerGraph.getNodes().size() == 0) {
							question = "This is a yes or no question. Please answer the following question just with yes or no.";
							Synthesizer.enunciateQuestion(question);
							i--;
						} else {
							List<INode> answer = new ArrayList<INode>();
							for (INode node : userAnswerGraph.getNodes()) {
								if (node.getType().getName().equals("token")) {
									answer.add(node);
								}
							}
							if (answer.size() == 1 && answer.get(0).getAttributeValue("value").toString().equals("yes")) {
								matchedContextEntityTokens.add(targetEntities.get(i));
								logger.info("Identified target context entity is " + targetEntities.get(i));
								i = targetEntities.size(); 	
								// target entity found update the graph and stop the loop
								updateAnaphoraReferent(sourceContextEntity, matchedContextEntityTokens.get(matchedContextEntityTokens.size()-1));
								isNotSolved = false;
							} else if (answer.size() == 1 && answer.get(0).getAttributeValue("value").toString().equals("no")) {
								// go on with the next target contextEntity
								counter++;
							} else {
								question = "This is a yes or no question. Please answer the following question just with yes or no.";
								Synthesizer.enunciateQuestion(question);
								i--;
							}
						}
						if (counter == allContextEntities.keySet().size()) {
							// nothing fits set all considered anaphoraReferent arcs 0.0
							setAnaphoraReferentArcsZero(sourceContextEntity);
							logger.info("None of the above is a target context entity. All considered anaphoraReferent arc confidences are set 0.0.");
							isNotSolved = false;
						}					
					}
				}
			} else {
				// include the user answer into the graph
				INode matchedContextEntity = matchedContextEntityTokens.get(0);
				updateAnaphoraReferent(sourceContextEntity, matchedContextEntity);
				isNotSolved = false;
			}
		}
		logger.info("Coreference solved");
		logCoreferenceContext(graph);
	}
	
	// detect the target entities in the answer graph and returns them as a list
	protected List<INode> checkMatch(List<INode> targetEntities, IGraph answerGraph) { 
		List<INode> matchedContextEntities = new ArrayList<INode>();
		for (int i = 0; i < targetEntities.size(); i++) {
			List<INode> tokens = new ArrayList<INode>();
			detectContextEntityTokens(tokens, targetEntities.get(i));
			if (tokens.size() == 1) { // if the referenced contextEntity comprise one word
				for (INode iNode : answerGraph.getNodes()) {
					if (iNode.getAttributeValue("value").equals(tokens.get(0).getAttributeValue("value"))) {
						matchedContextEntities.add(targetEntities.get(i));
					}
				}
			} else {
				// if all words of the viewed contextEntity match the answerGraph
				int counter = 0;
				for (INode token : tokens) {
					for (INode iNode : answerGraph.getNodes()) {
						if (iNode.getAttributeValue("value").equals(token.getAttributeValue("value"))) {
							counter++;
						}
					}
				}
				if (counter == tokens.size()) {
					matchedContextEntities.add(targetEntities.get(i));
				}
			}
			// if not all words match - this will be checked after all targetEntities are checked once 
			if (i == (targetEntities.size() - 1) && matchedContextEntities.isEmpty()) { // this just helps, if there is a noun NN or NNP
				for (int u = 0; u < targetEntities.size(); u++) {
					tokens = new ArrayList<INode>();
					detectContextEntityTokens(tokens, targetEntities.get(u));
					if (tokens.size() > 1) { 
						for (INode token : tokens) {
						    if (token.getAttributeValue("pos").equals("NN") || token.getAttributeValue("pos").equals("NNP")) {
								for (INode iNode : answerGraph.getNodes()) {
									if (iNode.getAttributeValue("value").equals(token.getAttributeValue("value"))) {
										if (!matchedContextEntities.contains(targetEntities.get(u))) { // prevents of adding the same contextEntity twice
											matchedContextEntities.add(targetEntities.get(u));												
										}			
									}
								}
						    }
						}
					} 
				}
			}
		}
		return matchedContextEntities;
	}
	
	// returns the part of the text which is included in the question
	protected List<INode> getQuestionableTextPart(INode iNode, List<INode> contextTokens, INode sourceContextEntityToken) {
		List<INode> textPart = new ArrayList<INode>(); // text part which is later used for the question
		INode startNode = sourceContextEntityToken;
		textPart.add(startNode);
		int counter = 0;
		// iterate in the graph backwards till first referenced context entity
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
			boolean relationArcFound = false; // checks if we are at the beginning of the graph
			for (IArc iArc : startNode.getIncomingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					startNode = iArc.getSourceNode();
					relationArcFound = true;
				}
 			}
			if (relationArcFound) {
				textPart.add(startNode);
			} else {
				break;
			}
		}
		
		// add next node after the source context entity, if there is any (to leave the NP of the target context entity)
		if (!sourceContextEntityToken.getOutgoingArcs().isEmpty()) {
			for (IArc iArc : sourceContextEntityToken.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					sourceContextEntityToken = iArc.getTargetNode();
					textPart.add(0, sourceContextEntityToken);
				}
			}
		}
	
		// iterate till the textPart ends with a noun phrase		
		while (!sourceContextEntityToken.getAttributeValue("chunkName").equals("NP")) {
			boolean relationArcFound = false; // checks if we are at the end of the graph
			for (IArc iArc : sourceContextEntityToken.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					sourceContextEntityToken = iArc.getTargetNode();
					relationArcFound = true;
				}
 			}
			if (relationArcFound) {				
				textPart.add(0, sourceContextEntityToken);
			} else {
				break;
			}
		}
		
		// complete the noun phrase
		while (sourceContextEntityToken.getAttributeValue("chunkName").equals("NP")) {
			boolean relationArcFound = false; // checks if we are at the end of the graph
			for (IArc iArc : sourceContextEntityToken.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					sourceContextEntityToken = iArc.getTargetNode();
					relationArcFound = true;
				}
 			}
			if (relationArcFound) {
				if (sourceContextEntityToken.getAttributeValue("chunkName").equals("NP")) {
					textPart.add(0, sourceContextEntityToken);					
				}
			} else {
				break;
			}
		}
		return textPart;
	}
	
	// add all tokens of a contextEntity to a list 
	// this method receives the "return"-list as input, because this method works recursive
	// this works because the list is a referenced data type like an array
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
	
	// update anaphoraReferent arcs 
	protected void updateAnaphoraReferent(INode originContextEntity, INode targetContextEntity) {
		for (IArc iArc : originContextEntity.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("contextRelation")) { 
				if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
					if (iArc.getTargetNode().equals(targetContextEntity)) {
						iArc.setAttributeValue("confidence", "1.0");
						iArc.setAttributeValue("confidenceVerified", true);
					} else {
						iArc.setAttributeValue("confidence", "0.0");	
						iArc.setAttributeValue("confidenceVerified", true);					
					}
				}				
			}			
		}
	}
	
	// set all considered anaphoraReferent arcs zero
	protected void setAnaphoraReferentArcsZero(INode originContextEntity) {
		for (IArc iArc : originContextEntity.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("contextRelation")) { 
				if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
					iArc.setAttributeValue("confidence", "0.0");	
					iArc.setAttributeValue("confidenceVerified", true);
				}				
			}			
		}
	}
	
	// returns all tokens of a contextEntity as a String (this is a recursive method)
	protected String getTokensAsString(INode contextEntity) {
		for (IArc iArc : contextEntity.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("reference"))
				return " " + iArc.getTargetNode().getAttributeValue("value").toString() + getTokensAsString(iArc.getTargetNode());
		}
		return "";
	}
		
	// returns all tokens of a contextEntity as a String - is used for logging (this is a recursive method)
	protected String detectContextEntityElements(INode contextEntity) {
		for (IArc iArc : contextEntity.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("reference"))
				return "" + iArc.getTargetNode() + "\n" + detectContextEntityElements(iArc.getTargetNode());
		}
		return "";
	}
	
	// toString method for readable arcs - is used for logging
	private String arcToString(IArc iArc) {
		String ts = "Arc(type: " + iArc.getType().getName() + " - ";
		for (final String attrName : iArc.getAttributeNames()) {
			ts = ts.concat(" " + attrName + ": " + iArc.getAttributeValue(attrName) + ",");
		}
		ts = ts.substring(0, ts.length() - 1).concat(")");
		return ts;
	}
	
	// logging the context of the coreference arcs
	private void logCoreferenceContext(IGraph graph) {
		for (IArc iArc : graph.getArcs()) {
			if (iArc.getType().getName().equals("contextRelation")) {
				if (iArc.getAttributeValue("name").equals("anaphoraReferent")) {
					INode anaphoraSourceNode = iArc.getSourceNode();
					logger.info("anaphora source token " + detectContextEntityElements(anaphoraSourceNode));
					logger.info(" anaphora source context entity " + anaphoraSourceNode);
					logger.info("  anaphora arc "+ arcToString(iArc));
					INode anaphoraTargetNode = iArc.getTargetNode();
					logger.info(" anaphora target context entity " + anaphoraTargetNode);
					logger.info("anaphora target token " + detectContextEntityElements(anaphoraTargetNode));
				}
			}
		}	
	}
}
