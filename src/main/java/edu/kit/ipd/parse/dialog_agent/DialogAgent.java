package edu.kit.ipd.parse.dialog_agent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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
	public void exec() {
		List<INode> lowConfidenceMainNodes = getMainNodesWithLowConfidence(ppd);
		List<List<String>> lowConfidenceNodesAlternatives = null; 
		for (int i = 0; i < lowConfidenceMainNodes.size(); i++) {
			lowConfidenceNodesAlternatives.add(determineAlternatives(lowConfidenceMainNodes.get(i)));
		}
	}

	@Override
	public void init() {
		props = ConfigManager.getConfiguration(getClass());
		reliableConfidenceThreshold = Double.parseDouble(props.getProperty("RELIABLE_CONFIDENCE_THRESHOLD"));
		minimumConfidenceThreshold = Double.parseDouble(props.getProperty("MINIMUM_CONFIDENCE_THRESHOLD"));
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
	
	protected List<String> determineAlternatives(INode iNode) {
		List<String> alternatives = new ArrayList<String>();
		System.out.println(iNode.getNumberOfOutgoingArcs()); // ########
		for (IArc iArc : iNode.getOutgoingArcs()) {
			double targetNodeConfidence = (Double) iArc.getTargetNode().getAttributeValue("asrConfidence");
			if (iArc.getTargetNode().getType().getName().toString().equals("alternative_token") && 
					minimumConfidenceThreshold < targetNodeConfidence &&
					targetNodeConfidence < reliableConfidenceThreshold) {
				alternatives.add(iArc.getTargetNode().getAttributeValue("value").toString());
				System.out.println(iArc.getTargetNode().toString()); // ########
			}
		}
		return alternatives;
	}
}
