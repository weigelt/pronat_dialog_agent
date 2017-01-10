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

import edu.kit.ipd.parse.luna.ILuna;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.AbstractPipelineData;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.data.token.Token;
import edu.kit.ipd.parse.luna.event.AbortEvent;
import edu.kit.ipd.parse.luna.event.IEvent;
import edu.kit.ipd.parse.luna.event.UpdateEvent;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
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
		// TO-DO mechanism to decide for which node to ask
//		String question = phraseQuestion(lowConfidenceNodesAlternatives.get(0));
		// activate new pipeline
		INode iNodeProcessed = null;
		String question = "Do you mean juice?";
		askQuestion(question);
		PrePipelineData ppdUserAnswer = getUserAnswer();
		if (examineYesNoAnswer(ppdUserAnswer).equals("YES")) {
			verifyNode(iNodeProcessed);
		} else if  (examineYesNoAnswer(ppdUserAnswer).equals("NO")) {
			// the user meant something else, we have to ask again
		} else {
			// the user did not answer properly lets ask again with advice
			String adviceQuestion = "Please answer the following question just with yes or no. " + question;
			askQuestion(adviceQuestion);
			ppdUserAnswer = getUserAnswer();
			if (examineYesNoAnswer(ppdUserAnswer).equals("YES")) {
				verifyNode(iNodeProcessed);
			} else if  (examineYesNoAnswer(ppdUserAnswer).equals("NO")) {
				// the user meant something else, we have to ask again
			} else {
				// break the user did not answered properly again we stop here
			}
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
		
		// this is investigates if the answer is just one word
		int countMainNodes = 0; 
		for (int i = 0; i < array.length; i++) {
			INode iNode = (INode) array[i];
			if (iNode.getType().getName().toString().equals("token")) {
				countMainNodes++;
			}
		}
		
		if (countMainNodes == 1) {
			INode iNode = (INode) array[0];
			if (iNode.getAttributeValue("value").equals("yes")) {
				return "YES";
			} else if (iNode.getAttributeValue("value").equals("no")) {
				return "NO";		
			} else {
				return "WORD_NOT_UNDERSTOOD";
				// advice this is a yes no question
//				askQuestionAdviceYesNo(question);
			}
		} else {
			return "TO_MANY_WORDS";
			// answer not understood
//			askQuestionAdviceYesNo(question);
		}
	}
	
	protected void verifyNode(INode iNode) {
//		iNode.setAttributeValue("asrConfidence", 1.0);
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
				ParseNode pNode = (ParseNode) array[i];
				if (pNode.getType().getName().toString().equals("token") && ((Double) pNode.getAttributeValue("asrConfidence") < reliableConfidenceThreshold)) {
					lowConfMainNodes.add(pNode);
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
	protected void askQuestion(String question) {
		WatsonTTS watsonTTS = new WatsonTTS();
		watsonTTS.synthesizeQuestion(question);
	}
}
