package edu.kit.ipd.parse.dialog_agent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseNode;
import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;

public class DialogAgent extends AbstractAgent {
	
	private static final Logger logger = LoggerFactory.getLogger(DialogAgent.class); // DialogAgent.class is equals to getClass()

	PrePipelineData ppd;
	private Properties props;
	double confidenceThreshold;
	
	// constructor
	public DialogAgent(PrePipelineData ppd) {
		this.ppd = ppd;
	}
	
	@Override
	public void exec() {
		getMainNodesWithLowConfidence(ppd);
	}

	@Override
	public void init() {
		props = ConfigManager.getConfiguration(getClass());
		confidenceThreshold = Double.parseDouble(props.getProperty("RELIABLE_CONFIDENCE_THRESHOLD"));
	}
	
	protected List<INode> getMainNodesWithLowConfidence(PrePipelineData ppd) {
		List<INode> lowConfMainNodes = new ArrayList<INode>();
		Object[] array;
		try {
			array = ppd.getGraph().getNodes().toArray();
			for (int i = 0; i < array.length; i++) {
				ParseNode pNode = (ParseNode) array[i];
				if (pNode.getType().getName().toString().equals("token") && ((Double) pNode.getAttributeValue("asrConfidence") < confidenceThreshold)) {
					lowConfMainNodes.add(pNode);
				}
			}
		} catch (MissingDataException mde) {
			mde.printStackTrace();
		}
		return lowConfMainNodes;
	}
}
