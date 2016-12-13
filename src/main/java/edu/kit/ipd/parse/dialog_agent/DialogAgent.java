package edu.kit.ipd.parse.dialog_agent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.ILuna;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
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
import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;

public class DialogAgent extends AbstractAgent {
	
	private static final Logger logger = LoggerFactory.getLogger(DialogAgent.class); // DialogAgent.class is equals to getClass()

	PrePipelineData ppd;
	private Properties props;
	double reliableConfidenceThreshold;
	double minimumConfidenceThreshold;
	
	// constructor
	public DialogAgent(PrePipelineData ppd) {
		this.ppd = ppd;
	}
	
	@Override
	public void init() {
		props = ConfigManager.getConfiguration(getClass());
		reliableConfidenceThreshold = Double.parseDouble(props.getProperty("RELIABLE_CONFIDENCE_THRESHOLD"));
		minimumConfidenceThreshold = Double.parseDouble(props.getProperty("MINIMUM_CONFIDENCE_THRESHOLD"));
	}
	
	@Override
	public void exec() {
		List<INode> lowConfidenceMainNodes = getMainNodesWithLowConfidence(ppd);
		List<List<INode>> lowConfidenceNodesAlternatives = new ArrayList<List<INode>>(); 
		for (int i = 0; i < lowConfidenceMainNodes.size(); i++) {
			lowConfidenceNodesAlternatives.add(determineAlternatives(lowConfidenceMainNodes.get(i)));
		}
		// TO-DO mechanism to decide for which node to ask
		askUser(lowConfidenceNodesAlternatives.get(0));
		// activate new pipeline
	}

	
	
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
	
	protected PrePipelineData askUser(List<INode> iNodes) {
		PrePipelineData resultPpd = null;
		System.out.println("method: askUser");
		for (INode iNode : iNodes) 
			System.out.println(iNode);

		return resultPpd;
	}
	
	// just for one word-alternative answers (or yes and no questions)
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
}
