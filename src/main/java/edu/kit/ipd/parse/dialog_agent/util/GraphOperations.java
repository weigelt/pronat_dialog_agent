package edu.kit.ipd.parse.dialog_agent.util;

import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;

public final class GraphOperations {

	// private constructor to prevent instantiation of an utility class
	private GraphOperations() {
	}

	// get previous verb node
	public static INode getPreviousVerbNode(INode iNode) {
		// take a previous node, if iNode is already a verb
		if (iNode.getAttributeValue("chunkName").equals("VP") && !iNode.getIncomingArcs().isEmpty()) {
			for (IArc iArc : iNode.getIncomingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					iNode = iArc.getSourceNode();
				}
			}
		}
		
		// iterate till the textPart begins with a verb phrase
		while (!iNode.getAttributeValue("chunkName").equals("VP")) {
			boolean relationArcFound = false;
			if (!iNode.getIncomingArcs().isEmpty()) {
				for (IArc iArc : iNode.getIncomingArcs()) {
					if (iArc.getType().getName().equals("relation")) {
						relationArcFound = true;
						iNode = iArc.getSourceNode();
					}
				}
			}
			else {
				break;
			}
			if (!relationArcFound) {
				break;
			}
		}
		return iNode;
	}
	
	// get last node of the following noun phrase
	public static INode getSubsequentNounNode(INode iNode) {
		// if iNode is already a noun phrase, leave this noun phrase
		while (iNode.getAttributeValue("chunkName").equals("NP")) {
			boolean relationArcFound = false; // checks if the end of the graph is reached
			for (IArc iArc : iNode.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					relationArcFound = true;
					iNode = iArc.getTargetNode();
				}
			}
			if (!relationArcFound) {
				break;
			}
		}
		
		// gather all nodes between iNode and the next noun phrase
		while (!iNode.getAttributeValue("chunkName").equals("NP")) {
			boolean relationArcFound = false; // checks if the end of the graph is reached
			for (IArc iArc : iNode.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					relationArcFound = true;
					iNode = iArc.getTargetNode();
				}
			}
			if (!relationArcFound) {
				break;
			}
		}
		
		// complete the second noun phrase
		boolean search = true;
		while (iNode.getAttributeValue("chunkName").equals("NP") && search) {
			boolean relationArcFound = false; // checks if the end of the graph is reached 
			for (IArc iArc : iNode.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					relationArcFound = true;
					if (!iArc.getTargetNode().getAttributeValue("chunkName").equals("NP") || iArc.getTargetNode().getAttributeValue("chunkIOB").equals("B-NP")) {
						// do not add this node, because this node is behind the next noun phrase
						search = false;
						break;
					} else {
						iNode = iArc.getTargetNode();
					}
				}
			}
			if (!relationArcFound) {
				break;
			}
		}	
		return iNode;
	}
}
