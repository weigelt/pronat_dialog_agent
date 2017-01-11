package edu.kit.ipd.parse.dialog_agent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseArc;
import edu.kit.ipd.parse.luna.graph.ParseNode;
import edu.kit.ipd.parse.dialog_agent.main.BuildGraph;
import edu.kit.ipd.parse.dialog_agent.stt.VoiceRecorder;
import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;
import edu.kit.ipd.parse.dialog_agent.tts.WatsonTTS;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;

public class DialogAgent extends AbstractAgent {
	
	private static final Logger logger = LoggerFactory.getLogger(DialogAgent.class); // DialogAgent.class is equals to getClass()

	Path path;
	PrePipelineData ppd;
	private Properties props;
	double reliableConfidenceThreshold;
	double minimumConfidenceThreshold;
	
	// constructor
	public DialogAgent(Path path) {
		this.path = path;
	}
	
	@Override
	public void init() {
		props = ConfigManager.getConfiguration(getClass());
		reliableConfidenceThreshold = Double.parseDouble(props.getProperty("RELIABLE_CONFIDENCE_THRESHOLD"));
		minimumConfidenceThreshold = Double.parseDouble(props.getProperty("MINIMUM_CONFIDENCE_THRESHOLD"));
	}
	
	@Override
	public void exec() {
		ppd = buildGraph(path);
		List<INode> lowConfidenceMainNodes = getMainNodesWithLowConfidence(ppd);
		List<List<INode>> lowConfidenceNodesAlternatives = new ArrayList<List<INode>>(); 
		for (int i = 0; i < lowConfidenceMainNodes.size(); i++) {
			lowConfidenceNodesAlternatives.add(determineAlternatives(lowConfidenceMainNodes.get(i)));
		}
		
		// take the main node with the lowest confidence and verify him
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
		
		askYesNoQuestion(iNodeProcessed);
		
		// the following just prints the final graph
		Object[] array = null;
		try {
			array = ppd.getGraph().getNodes().toArray();			
		} catch (MissingDataException mde) {
			mde.printStackTrace();
		}
		for (int i = 0; i < array.length; i++) {
			INode iNode = (INode) array[i];
			System.out.println(iNode.toString()); 
		}
	}
	
	// asks open questions
	protected void askOpenQuestion(INode iNode) {
		String openQuestion = "What did you mean?";
		enunciateQuestion(openQuestion);
		PrePipelineData ppdUserAnswer = getUserAnswer();
		
		if (examineOpenAnswer(ppdUserAnswer).equals("REPLACE")) {
//			replaceNode(iNode);
		} else if (examineOpenAnswer(ppdUserAnswer).equals("WORD_NOT_UNDERSTOOD")) {
			String adviceQuestion = "Sorry, I did not understand you. Please say it again.";
			enunciateQuestion(adviceQuestion);
			ppdUserAnswer = getUserAnswer();
			if (examineOpenAnswer(ppdUserAnswer).equals("REPLACE")) {
//				replaceNode(iNode);
			} else {
				// break the user did not answered properly again we stop here
			}
		} else if (examineOpenAnswer(ppdUserAnswer).equals("TO_MANY_WORDS")) {
			String adviceQuestion = "Please answer again but just with one word.";
			enunciateQuestion(adviceQuestion);
			ppdUserAnswer = getUserAnswer();
			if (examineOpenAnswer(ppdUserAnswer).equals("REPLACE")) {
//				replaceNode(iNode);
			}  else {
				// break the user did not answered properly again we stop here
			}
		}
	}
	
	// replaces a node
	protected void replaceNode(INode iNode, PrePipelineData ppd) {
		Object[] array = null;
		try {
			array = ppd.getGraph().getNodes().toArray();			
		} catch (MissingDataException mde) {
			mde.printStackTrace();
		}

//		System.out.println(" wr wer  " + iNode.toString());
//		iNode = (INode) array[0];
//		System.out.println(" wr wer  " + iNode.toString());
		
		
//		try {
//			ppd.getGraph().getNodes().add(iNode); // wann verwendet ihr denn add node??
//			System.out.println("Final print");
//			System.out.println(ppd.getGraph().showGraph());
//		} catch (final MissingDataException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	// asks a yes or no question BUT can also initiate open questions
	protected void askYesNoQuestion(INode iNode) {
		System.out.println("low confidence " + iNode.toString());
		// replace with a question pattern which uses the other nodes of this sentence
		String question = "Do you mean " + iNode.getAttributeValue("value") + "?";
		enunciateQuestion(question);
		PrePipelineData ppdUserAnswer = getUserAnswer();
		
		if (examineYesNoAnswer(ppdUserAnswer).equals("YES")) {
			verifyNode(iNode);
		} else if (examineYesNoAnswer(ppdUserAnswer).equals("NO")) {
			askOpenQuestion(iNode);
		} else {
			// the user did not answer properly lets ask again with advice
			String adviceQuestion = "Please answer the following question just with yes or no. " + question;
			enunciateQuestion(adviceQuestion);
			ppdUserAnswer = getUserAnswer();
			if (examineYesNoAnswer(ppdUserAnswer).equals("YES")) {
				verifyNode(iNode);
			} else if  (examineYesNoAnswer(ppdUserAnswer).equals("NO")) {
				askOpenQuestion(iNode);
			} else {
				// break the user did not answered properly again we stop here
			}
		}
	}
	
	// examine if an open question consists of one word
	protected String examineOpenAnswer(PrePipelineData ppd) {
		Object[] array = null;
		try {
			array = ppd.getGraph().getNodes().toArray();			
		} catch (MissingDataException mde) {
			mde.printStackTrace();
		}
		
		if (countMainNodes(array) == 1) { // if the answer contains one word
			INode iNode = (INode) array[0];
			if (Double.parseDouble(iNode.getAttributeValue("asrConfidence").toString()) > reliableConfidenceThreshold) {
				return "REPLACE";
			} else {
				return "WORD_NOT_UNDERSTOOD";
			}
		} else {
			return "TO_MANY_WORDS";
		}
	}
	
	// examine if the answer was yes or no
	protected String examineYesNoAnswer(PrePipelineData ppd) {
		Object[] array = null;
		try {
			array = ppd.getGraph().getNodes().toArray();			
		} catch (MissingDataException mde) {
			mde.printStackTrace();
		}
		
		if (countMainNodes(array) == 1) { // if the answer contains one word
			INode iNode = (INode) array[0];
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
	
	// count the main nodes of an array containing nodes
	protected int countMainNodes(Object[] array) {
		int counter = 0;
		for (int i = 0; i < array.length; i++) {
			INode iNode = (INode) array[i];
			if (iNode.getType().getName().toString().equals("token")) {
				counter++;
			}
		}
		return counter;
	}
	
	// verifies a node with low confidence (and removes his alternative nodes)
	protected void verifyNode(INode iNode) {
		iNode.setAttributeValue("asrConfidence", 1.0);
		System.out.println(iNode.toString());
//		Set <? extends IArc> outgoingArcs = iNode.getOutgoingArcs();
//		for (IArc outgoingArc : outgoingArcs) {
//			INode targetNode = outgoingArc.getTargetNode();
//			// remove methods for nodes and arcs are missing
//		}
//		System.out.println("YES");		
//		System.out.println(iNode.getOutgoingArcs());	
	}
	
	// returns all nodes with confidence between given confidence thresholds
	protected List<INode> getMainNodesWithLowConfidence(PrePipelineData ppd) {
		List<INode> lowConfMainNodes = new ArrayList<INode>();
		Object[] array;
		try {
			array = ppd.getGraph().getNodes().toArray();
			for (int i = 0; i < array.length; i++) {
				INode iNode = (INode) array[i];
				if (iNode.getType().getName().toString().equals("token") && ((Double) iNode.getAttributeValue("asrConfidence") < reliableConfidenceThreshold)) {
					lowConfMainNodes.add(iNode);
				}
			}
		} catch (MissingDataException mde) {
			mde.printStackTrace();
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
	
	// not used yet
	// this method formulates the question which should be later asked
	protected PrePipelineData phraseQuestion(List<INode> iNodes) {
		PrePipelineData resultPpd = null;
		System.out.println("method: askUser");
		for (INode iNode : iNodes) 
			System.out.println(iNode);

		return resultPpd;
	}
	
	// not used yet
	// returns only one word from the user answer, if there are more words another method will be used (or yes and no questions)
	protected INode getOneWordAnswer(PrePipelineData ppd) {
		Object[] allAnswerGraphNodes = null;
		INode iNode = null;
		int counter = 0; 
		try {
			allAnswerGraphNodes = ppd.getGraph().getNodes().toArray();
			for (int i = 0; i < allAnswerGraphNodes.length; i++) {
				INode in = (INode) allAnswerGraphNodes[i];			
				if (in.getType().getName().toString().equals("token")) {
					iNode = in;
					counter++;
					if (counter >= 2) {
						return null; // a return value that indicates to many tokens would be better
					}
				}
			}
		} catch (MissingDataException mde) {
			mde.printStackTrace();
		}
		return iNode;
	}
		
	// activates the voice recorder to receive the user answer as a file path and then uses buildgraph() to return PrePipeLineData
	protected PrePipelineData getUserAnswer() {
		VoiceRecorder voiceRecorder = new VoiceRecorder();
		Path pathAnswer = voiceRecorder.getAnswer();
		return buildGraph(pathAnswer);
	}
	
	// creates a graph out of a flac file
	protected PrePipelineData buildGraph(Path path) {
		BuildGraph bg = new BuildGraph(path);
		bg.buildGraph();
		PrePipelineData ppd = bg.getGraph();
		return ppd;
	}
	
	// invokes WatsonTTS to ask the user a question
	protected void enunciateQuestion(String question) {
		WatsonTTS watsonTTS = new WatsonTTS();
		watsonTTS.synthesizeQuestion(question);
	}
}
