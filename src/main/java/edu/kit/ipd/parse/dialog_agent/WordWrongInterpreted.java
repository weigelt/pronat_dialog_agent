package edu.kit.ipd.parse.dialog_agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;
import edu.kit.ipd.parse.dialog_agent.util.GainUserAnswer;
import edu.kit.ipd.parse.dialog_agent.util.GraphOperations;
import edu.kit.ipd.parse.dialog_agent.util.Synthesizer;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class WordWrongInterpreted extends AbstractDefectCategory {

	IGraph graph;
	List<INode> lowConfidenceMainNodes;
	private Properties props;
	protected double reliableConfidenceThreshold;
	protected double minimumConfidenceThreshold;
	
	protected boolean analyseGraph(IGraph graph) {
		props = ConfigManager.getConfiguration(getClass(), "DialogAgent");
		reliableConfidenceThreshold = Double.parseDouble(props.getProperty("RELIABLE_ASR_CONFIDENCE_THRESHOLD"));
		minimumConfidenceThreshold = Double.parseDouble(props.getProperty("MINIMUM_ASR_CONFIDENCE_THRESHOLD"));
		
		this.graph = graph;
//		return false;
		lowConfidenceMainNodes = getTokenNodesWithLowConfidence();	
		if (lowConfidenceMainNodes.isEmpty()) 
			return false; 
		return true;
	}
	
	protected void solveDefectCategory() {
		List<List<INode>> lowConfidenceNodesAlternatives = new ArrayList<List<INode>>(); 
		for (int i = 0; i < lowConfidenceMainNodes.size(); i++) {
			lowConfidenceNodesAlternatives.add(determineAlternatives(lowConfidenceMainNodes.get(i)));
		}
		
		// take the main node with the lowest confidence 
		INode iNodeProcessed = null;
		for (INode lowConfidenceMainNode : lowConfidenceMainNodes) {
			if (iNodeProcessed != null) {
				if (Double.parseDouble(iNodeProcessed.getAttributeValue("asrConfidence").toString()) > 
						Double.parseDouble(lowConfidenceMainNode.getAttributeValue("asrConfidence").toString())) {
					iNodeProcessed = lowConfidenceMainNode;			
				}
			} else { 
				iNodeProcessed = lowConfidenceMainNode;
			}
			System.out.println("low confidence " + lowConfidenceMainNode.toString());
		}
	
		// verify this node
		if (!(iNodeProcessed == null))
			askYesNoQuestion(iNodeProcessed);
		
//		// the following just prints the final graph
//		Object[] array = graph.getNodes().toArray();		
//		for (int i = 0; i < array.length; i++) {
//			INode iNode = (INode) array[i];
//			System.out.println(iNode.toString()); 
//		}
		
	}
	
	// returns all nodes with confidence between given confidence thresholds
		protected List<INode> getTokenNodesWithLowConfidence() {
			List<INode> lowConfMainNodes = new ArrayList<INode>();
			for (Iterator<INode> iterator = graph.getNodes().iterator(); iterator.hasNext();) {
				INode iNode = iterator.next();
				if (iNode.getType().getName().toString().equals("token") && ((Double) iNode.getAttributeValue("asrConfidence") < reliableConfidenceThreshold)) {
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
				if (iArc.getTargetNode().getType().getName().toString().equals("alternative_token") && 
						minimumConfidenceThreshold < targetNodeConfidence &&
						targetNodeConfidence < reliableConfidenceThreshold) {
					alternatives.add(iArc.getTargetNode());
					System.out.println(iArc.getTargetNode().toString()); // ########
				}
			}
			return alternatives;
		}
		
		// asks a yes or no question BUT can also initiate open questions
		protected void askYesNoQuestion(INode iNode) {
			System.out.println("low confidence " + iNode.toString());
			// replace with a question pattern which uses the other nodes of this sentence
			String question = "Do you mean " + iNode.getAttributeValue("value") + "?";
			Synthesizer.enunciateQuestion(question);
			IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();
			
			if (examineYesNoAnswer(userAnswerGraph).equals("YES")) {
				GraphOperations.verifyNode(graph, iNode);
			} else if (examineYesNoAnswer(userAnswerGraph).equals("NO")) {
				askOpenQuestion(iNode);
			} else {
				// the user did not answer properly lets ask again with advice
				String adviceQuestion = "Please answer the following question just with yes or no. " + question;
				Synthesizer.enunciateQuestion(adviceQuestion);
				userAnswerGraph = GainUserAnswer.getUserAnswer();
				if (examineYesNoAnswer(userAnswerGraph).equals("YES")) {
					GraphOperations.verifyNode(graph, iNode);
				} else if  (examineYesNoAnswer(userAnswerGraph).equals("NO")) {
					askOpenQuestion(iNode);
				} else {
					// break the user did not answered properly again we stop here
				}
			}
		}
		
		// examine if the answer was yes or no
		protected String examineYesNoAnswer(IGraph graph) {
			if (GraphOperations.countNodes("token", graph) == 1) { // if the answer contains one word
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
			if (GraphOperations.countNodes("token", graph) == 1) { // if the answer contains one word
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
			String openQuestion = "What did you mean?";
			Synthesizer.enunciateQuestion(openQuestion);
			IGraph userAnswerGraph = GainUserAnswer.getUserAnswer();
			
			if (examineOpenAnswer(userAnswerGraph).equals("REPLACE")) {
				INode answerNode = userAnswerGraph.getNodes().iterator().next();
				GraphOperations.replaceNode(graph, iNode, answerNode);
			} else if (examineOpenAnswer(userAnswerGraph).equals("WORD_NOT_UNDERSTOOD")) {
				String adviceQuestion = "Sorry, I did not understand you. Please say it again.";
				Synthesizer.enunciateQuestion(adviceQuestion);
				userAnswerGraph = GainUserAnswer.getUserAnswer();
				if (examineOpenAnswer(userAnswerGraph).equals("REPLACE")) {
					INode answerNode = userAnswerGraph.getNodes().iterator().next();
					GraphOperations.replaceNode(graph, iNode, answerNode);
				} else {
					// break the user did not answered properly again we stop here
				}
			} else if (examineOpenAnswer(userAnswerGraph).equals("TO_MANY_WORDS")) {
				String adviceQuestion = "Please answer again but just with one word.";
				Synthesizer.enunciateQuestion(adviceQuestion);
				userAnswerGraph = GainUserAnswer.getUserAnswer();
				if (examineOpenAnswer(userAnswerGraph).equals("REPLACE")) {
					INode answerNode = userAnswerGraph.getNodes().iterator().next();
					GraphOperations.replaceNode(graph, iNode, answerNode);
				}  else {
					// break the user did not answered properly again we stop here
				}
			}
		}
}
