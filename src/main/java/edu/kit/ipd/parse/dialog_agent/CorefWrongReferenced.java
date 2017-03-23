package edu.kit.ipd.parse.dialog_agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;
import edu.kit.ipd.parse.dialog_agent.util.GainUserAnswer;
import edu.kit.ipd.parse.dialog_agent.util.Synthesizer;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class CorefWrongReferenced extends AbstractDefectCategory {

	private static final Logger logger = LoggerFactory.getLogger(CorefWrongReferenced.class);
	
	private IGraph graph;
	private Properties props;
	private double anaphoraReferentCorrectThreshold;
	private double anaphoraReferentMostProbablyCorrectThreshold;
	
	List<INode> questionableContextEntities = new ArrayList<INode>();
	
	@Override
	protected boolean analyseGraph(IGraph graph) {
		props = ConfigManager.getConfiguration(getClass(), "DialogAgent");
		anaphoraReferentCorrectThreshold = Double.parseDouble(props.getProperty("ANAPHORA_REFERENT_CORRECT_THRESHOLD"));
		anaphoraReferentMostProbablyCorrectThreshold = Double.parseDouble(props.getProperty("ANAPHORA_REFERENT_MOST_PROBABLY_CORRECT_THRESHOLD"));
		
		this.graph = graph;
		boolean result = false;
		
//		System.out.println(graph.showGraph());
//		for (INode iNode : graph.getNodes()) {
//			System.out.println(iNode);
//		}
////		
		System.out.println("");
		System.out.println("");
		for (IArc iArc : graph.getArcs()) {
			if (iArc.getType().getName().equals("contextRelation")) {
				if (iArc.getAttributeValue("name").equals("anaphoraReferent")) {
					INode anaphoraSourceNode = iArc.getSourceNode();
					System.out.println(detectContextEntityElements(anaphoraSourceNode));
//					for (IArc sourceOutgoingArc : anaphoraSourceNode.getOutgoingArcs()) {
//						if (sourceOutgoingArc.getType().getName().equals("reference"))
//							System.out.println("anaphora source token " + sourceOutgoingArc.getTargetNode());
//					}
					System.out.println(" anaphora source node " + anaphoraSourceNode);
					System.out.println("  " + arcToString(iArc));
					INode anaphoraTargetNode = iArc.getTargetNode();
					System.out.println(" anaphora target node " + anaphoraTargetNode);
					System.out.println(detectContextEntityElements(anaphoraTargetNode));
//					for (IArc targetOutgoingArc : anaphoraTargetNode.getOutgoingArcs()) {
////						System.out.println(arcToString(targetOutgoingArc));
//						if (targetOutgoingArc.getType().getName().equals("reference"))
//							System.out.println("anaphora target token " + targetOutgoingArc.getTargetNode());
////							for (IArc targetOutgoingArc2 : targetOutgoingArc.getTargetNode().getOutgoingArcs()) {
//////								System.out.println(arcToString(targetOutgoingArc2));
////								if (targetOutgoingArc2.getType().getName().equals("reference"))
////									System.out.println("anaphora target token " + targetOutgoingArc2.getTargetNode());
////							}
//					}
					System.out.println("");
				}
			}
		}
		
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
			
//			System.out.println(iNode);
//			System.out.println("size " + confidenceCorrectList.size());
//			System.out.println("size " + confidenceHighList.size());
//			System.out.println("size " + confidenceLowList.size());
		}
		
//		for (int i = 0; i < questionableContextEntities.size(); i++) {
//			System.out.println(questionableContextEntities.get(i));	
//		}
		return result;
	}
	
	@Override
	protected void solveDefectCategory() {
		logger.info("Start solving coreference resolution issues - ambiguous coreference found");
		
		INode contextEntityNode = questionableContextEntities.get(0);
//		int countAnaphoraArcs = 0;
//		for (IArc iArc : contextEntityNode.getOutgoingArcs()) {
//			if (iArc.getType().getName().equals("contextRelation")) { 
//				if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
//					countAnaphoraArcs++;
//				}
//			}
//		}
		INode endNode = null; // is the token of the contextEntity
		
		// this part extracts the tokens where the observed contextEntity = iNode refers to (all anaphoraReferent -> contextEntity -> reference -> tokens)
		List<INode> contextTokens = new ArrayList<INode>();
		List<INode> targetEntities = new ArrayList<INode>();
		List<Double> contextTokensConfidence = new ArrayList<Double>();
		for (IArc iArc : contextEntityNode.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("reference")) {
				endNode = iArc.getTargetNode();
			}
			if (iArc.getType().getName().equals("contextRelation")) { 
				if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
					targetEntities.add(iArc.getTargetNode());
					for (IArc iArcTargetContextEntity : iArc.getTargetNode().getOutgoingArcs()) {
						if (iArcTargetContextEntity.getType().getName().equals("reference")) {
							contextTokens.add(iArcTargetContextEntity.getTargetNode());
						}
					}
					contextTokensConfidence.add(Double.parseDouble(iArc.getAttributeValue("confidence").toString()));
				}
			}
		} 
		System.out.println("here1");
		System.out.println(contextEntityNode);
		for (IArc iArc : contextEntityNode.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("reference")) {
				System.out.println(iArc.getTargetNode());
			}
		}
		
		for (INode iNode : contextTokens) {
			System.out.println(iNode);
		}
		System.out.println("endNote " + endNode);
		
		List<INode> textPart = getQuestionableTextPart(contextEntityNode, contextTokens, endNode);
		int size = textPart.size();
		for (int i = 0; i < size; i++) { // removes <eps> because watson asr can not handle it as input
			if (textPart.get(i).getAttributeValue("value").equals("<eps>")) {
				textPart.remove(i);
				size--;
			}
		}

		// spezialfall zwei oder mehr it als antwortmoeglichkeit!!!________________________________________________
		Map<INode, String> allContextEntities = new HashMap<INode, String>(); // contains all target contextEntities and their tokens as one String
		Map<INode, Integer> occurence = new HashMap<INode, Integer>(); // contain Strings of the contextEntities and the amount of occurence
//		allContextEntities.put(contextEntityNode, getTokensAsString(contextEntityNode));
//		occurence.put(contextEntityNode, 1);
		for (INode iNode : targetEntities) {
			allContextEntities.put(iNode, getTokensAsString(iNode));
			occurence.put(iNode, 1);
		}
		
//		System.out.println("MAP");
		boolean sourceContextEntityMoreThanOnce = false;
		for (INode iNode : allContextEntities.keySet()) {
//			System.out.println(iNode);
//			System.out.println(allContextEntities.get(iNode));
			if (getTokensAsString(contextEntityNode).equals(allContextEntities.get(iNode))) {
				sourceContextEntityMoreThanOnce = true;
			}
		}
		
//		System.out.println(sourceContextEntityMoreThanOnce);
		
//		// check if there are duplicates in the target contextEntities e. g. it two times
//		for (INode firstLoopNode : allContextEntities.keySet()) {
//			for (INode secondLoopNode : allContextEntities.keySet()) {
//				if (!firstLoopNode.equals(secondLoopNode)) {
//					if (allContextEntities.get(firstLoopNode).equals(allContextEntities.get(secondLoopNode))) {
//						occurence.replace(firstLoopNode, occurence.get(firstLoopNode) + 1);
//					}
//				}
//			}
//		}
//		boolean duplicateTargetContextEntities = false;
//		for (INode iNode : occurence.keySet()) {
//			if (occurence.get(iNode) > 1) {
//				duplicateTargetContextEntities = true;
//			}
//		}
//		System.out.println(duplicateTargetContextEntities);
//		if (duplicateTargetContextEntities) {
//			// set all considered nodes notProcessable - that is future work
//		}		

		// phrase open question
		String question = "In the following part, where does the ";
		if (sourceContextEntityMoreThanOnce) {
			question = question + "last ";
		}
		List<INode> tokensEntityToAskForList = new ArrayList<INode>();
		detectContextEntityTokens(tokensEntityToAskForList, contextEntityNode);
//		System.out.println("entities to ask for");
		logger.debug("Source entity " + contextEntityNode);
		for (INode iNode : tokensEntityToAskForList) {
			logger.debug("Possible target entity " + iNode);
			for (IArc iArc : iNode.getIncomingArcs()) {
				if (iArc.getType().getName().equals("contextRelation")) { 
					if (iArc.getAttributeValue("name").equals("anaphoraReferent")) { 
						if (iArc.getSourceNode().equals(contextEntityNode)) {
							logger.debug("AnaphoraReferent confidence of this entity " + iArc.getAttributeValue("confidence"));
						}
					}
				}
			}
//			System.out.println(iNode);
		}

		if (tokensEntityToAskForList.size() > 1) {
			question = question + "phrase  ";
			for (INode iNodeTokenToAskFor : tokensEntityToAskForList) {
				question = question + iNodeTokenToAskFor.getAttributeValue("value") + "  ";
			}
		} else if (tokensEntityToAskForList.size() == 1) {
//			question = question + "word  ";
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
//		System.out.println(userAnswerGraph.showGraph());
		

////		 include answer into graph___________________________________________________________________________
//		for (INode node : contextTokens)
//			System.out.println(node);
		
		List<INode> matchedContextEntityTokens = checkMatch(targetEntities, userAnswerGraph);
		if (!matchedContextEntityTokens.isEmpty()) {
			logger.debug("Identified target context entity is " + matchedContextEntityTokens.get(0));
		}
		
		// 
		boolean isNotSolved = true;
		boolean secondQuestionAsked = false;
		while (isNotSolved) {
			if (matchedContextEntityTokens.isEmpty()) {
				if (!secondQuestionAsked) { // consider the !
					// ask again with alternatives
					question = "I did not understand you. Look, you mentioned the following " + targetEntities.size() + " entities. ";
					for (INode iNode : allContextEntities.keySet()) {
						question = question + allContextEntities.get(iNode) + ". ";
					}
					question = question + " Now, please tell me where ";
					if (sourceContextEntityMoreThanOnce) {
						question = question + "the last ";
					}
					question = question + getTokensAsString(contextEntityNode) + "  refers to ";
					question = question + "in the following part.  ";
					for (int u = 0; u < textPart.size(); u++) {
						question = question + textPart.get(textPart.size() - u - 1).getAttributeValue("value") + " ";
					}
					
					Synthesizer.enunciateQuestion(question);
					userAnswerGraph = GainUserAnswer.getUserAnswer();
//					System.out.println(userAnswerGraph.showGraph());
					matchedContextEntityTokens = checkMatch(targetEntities, userAnswerGraph);
					if (!matchedContextEntityTokens.isEmpty()) {
						logger.debug("Identified target context entity is " + matchedContextEntityTokens.get(0));
					}
					secondQuestionAsked = true;
				} else {
					// now drill down on every target contextEntity
					int counter = 0;
					for (int i = 0; i < targetEntities.size(); i++) {
						question = "You said: ";
						for (int u = 0; u < textPart.size(); u++) {
							question = question + textPart.get(textPart.size() - u - 1).getAttributeValue("value") + " ";
						}
						question = question + "Does " + getTokensAsString(contextEntityNode) + " refers to " + getTokensAsString(targetEntities.get(i));
						
						Synthesizer.enunciateQuestion(question);
						userAnswerGraph = GainUserAnswer.getUserAnswer();
//						System.out.println(userAnswerGraph.showGraph());
						
						// determine if the answer is yes or no
						if (userAnswerGraph.getNodes().size() == 0) {
							question = "This is a yes or no question. Please answer the following question with yes or no.";
							Synthesizer.enunciateQuestion(question);
							i--;
						} else {
							for (INode node : userAnswerGraph.getNodes()) {
								if (node.getAttributeValue("value").toString().equals("yes")) {
									matchedContextEntityTokens.add(targetEntities.get(i));
									if (!matchedContextEntityTokens.isEmpty()) {
										logger.debug("Identified target context entity is " + matchedContextEntityTokens.get(0));
									}
									break;							
								} else if (node.getAttributeValue("value").toString().equals("no")) {
									// go on with the next target contextEntity
									counter++;
									break;
								} else {
									question = "This is a yes or no question. Please answer the following question with yes or no.";
									Synthesizer.enunciateQuestion(question);
									i--;
									break;
								}
							}
						}
						if (counter == allContextEntities.keySet().size()) {
							// nothing fits set all considered anaphoraReferent arcs 0.0
							setAnaphoraReferentArcsZero(contextEntityNode);
							logger.debug("None of the above is a target context entity. All considered anaphoraReferent arc confidences are set 0.0.");
							isNotSolved = false;
						}					
					}
				}
			} else {
				// include the user answer into the graph
				INode matchedContextEntity = matchedContextEntityTokens.get(0);
//				System.out.println(matchedContextEntity);
//				System.out.println("outgoing");
//				for (IArc iArc : matchedContextEntity.getOutgoingArcs()) {
//					System.out.println(iArc);
//				}				
//				System.out.println("ingoing");
//				for (IArc iArc : matchedContextEntity.getIncomingArcs()) {
//					System.out.println(iArc);
//				}				
				updateAnaphoraReferent(contextEntityNode, matchedContextEntity);
				isNotSolved = false;
			}
		}
	}
	
	// returns the contextEntity if there is a match, otherwise an empty list
	// the contextEntity has to be returned (instead of the context tokens) to change the anaphoraReferent-arcs
	protected List<INode> checkMatch(List<INode> targetEntities, IGraph answerGraph) { // targetEntity is a contextEntity the viewed contextEntity refers to
		List<INode> matchedContextEntityTokens = new ArrayList<INode>();
		for (int i = 0; i < targetEntities.size(); i++) {
			List<INode> tokens = new ArrayList<INode>();
			detectContextEntityTokens(tokens, targetEntities.get(i));
			if (tokens.size() == 1) { // if the referenced contextEntity comprise one word
				for (INode iNode : answerGraph.getNodes()) {
					if (iNode.getAttributeValue("value").equals(tokens.get(0).getAttributeValue("value"))) {
						matchedContextEntityTokens.add(targetEntities.get(i));
						i = targetEntities.size();
						break;
					}
				}
			} else {
				// if all words of the contextEntity match the answerGraph
				int counter = 0;
				for (INode token : tokens) {
					for (INode iNode : answerGraph.getNodes()) {
						if (iNode.getAttributeValue("value").equals(token.getAttributeValue("value"))) {
							counter++;
							break; // important, otherwise it could count words twice
						}
					}
				}
				if (counter == tokens.size()) {
					matchedContextEntityTokens.add(targetEntities.get(i));
					i = targetEntities.size();
				}
			}
//			System.out.println(i);
			// if not all words match - this will be checked after all targetEntities are checked once 
			if (i == (targetEntities.size() - 1) && matchedContextEntityTokens.isEmpty()) { // this just helps, if there is a noun (NN)
				for (int u = 0; u < targetEntities.size(); u++) {
					tokens = new ArrayList<INode>();
					detectContextEntityTokens(tokens, targetEntities.get(u));
					if (tokens.size() > 1) { 
						for (INode token : tokens) {
//					    	System.out.println("token " + token);
						    if (token.getAttributeValue("pos").equals("NN")) {
								for (INode iNode : answerGraph.getNodes()) {
//							    	System.out.println("answerGraph " + iNode);
									if (iNode.getAttributeValue("value").equals(token.getAttributeValue("value"))) {
//										System.out.println("match");
										if (matchedContextEntityTokens.size() < 1) { // prevents of matching two contextEntities
											matchedContextEntityTokens.add(targetEntities.get(u));											
										}
										u = targetEntities.size();
										break; 
									}
								}
						    }
						}
					} 
				}
			}
//			System.out.println("after " + i);
		}
		return matchedContextEntityTokens;
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
		System.out.println("startNode " + startNode);
		
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
		System.out.println("startNode verb" + startNode);
		
		// add next node if there is any (to leave the NP of the target context entity)
		if (!endNode.getOutgoingArcs().isEmpty()) {
			for (IArc iArc : endNode.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					endNode = iArc.getTargetNode();
				}
			}
			textPart.add(0, endNode);
		}
	
		// iterate till the textPart ends with a complete noun phrase		
		while (!endNode.getAttributeValue("chunkName").equals("NP")) {
			if (!endNode.getOutgoingArcs().isEmpty()) {
				for (IArc iArc : endNode.getOutgoingArcs()) {
					if (iArc.getType().getName().equals("relation")) {
						endNode = iArc.getTargetNode();
					}
 				}
				textPart.add(0, endNode);
			}
			else {
				break;
			}
		}
		
		// complete the noun phrase
		while (endNode.getAttributeValue("chunkName").equals("NP")) {
			if (!endNode.getOutgoingArcs().isEmpty()) {
				for (IArc iArc : endNode.getOutgoingArcs()) {
					if (iArc.getType().getName().equals("relation")) {
						endNode = iArc.getTargetNode();
					}
 				}
				if (endNode.getAttributeValue("chunkName").equals("NP")) {
					textPart.add(0, endNode);					
				}
			}
			else {
				break;
			}
		}
		
		for (INode node : graph.getNodes()) {
			System.out.println(node);
		}
		
		
//		boolean search = true;
//		while (search) {
//			int count = 0; // this stops the loop if the end of the text is reached
//			for (IArc outgoingArc : endNode.getOutgoingArcs()) {
//				if (outgoingArc.getType().getName().equals("relation")) {
//					count++;
//				}
//			}
//			if (count > 0 && search) {
//				boolean inNP = false;
//				if (!inNP) { // add nodes till a NP is reached
//					for (IArc iArc : endNode.getOutgoingArcs()) {
//						if (iArc.getType().getName().equals("relation")) {
//							endNode = iArc.getTargetNode();
//							break; 
//						}
//			 		}
//					textPart.add(0, endNode);
//					if (endNode.getAttributeValue("chunkName").equals("NP")) {
//						inNP = true;
//					}
//				} 
//				else {
//					for (IArc iArc : endNode.getOutgoingArcs()) {
//						if (iArc.getType().getName().equals("relation")) {
//							endNode = iArc.getTargetNode();
//						}
//			 		}
//					if (!endNode.getAttributeValue("chunkName").equals("NP")) {
//						search = false; // because the next NP of the contextEntity is completely included in the textPart
//					}
//					else {
//						textPart.add(0, endNode);
//					}
//					System.out.println("1b");
//				}
//			} 
//			else {
//				search = false; // because there are no tokens left in the graph
//			}
//		}
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
	
	// set all considered anaphoraReferent arcs 0
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
	
	// returns all tokens of a contextEntity as a String
	protected String getTokensAsString(INode contextEntity) {
		for (IArc iArc : contextEntity.getOutgoingArcs()) {
			if (iArc.getType().getName().equals("reference"))
				return " " + iArc.getTargetNode().getAttributeValue("value").toString() + getTokensAsString(iArc.getTargetNode());
		}
		return "";
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
