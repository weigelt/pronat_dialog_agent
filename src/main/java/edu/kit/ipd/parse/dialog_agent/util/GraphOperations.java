package edu.kit.ipd.parse.dialog_agent.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public final class GraphOperations {

	// private constructor to prevent instantiation of an utility class
	private GraphOperations() {
	}

	// replaces a node and removes his alternative nodes
	public static void replaceNode(IGraph graph, INode oldNode, INode newNode) {
		removeAlternativeNodes(graph, oldNode);
		newNode.setAttributeValue("asrVerified", true);
		graph.replaceNode(oldNode, newNode, false);
	}

	// verifies a node with low confidence and removes his alternative nodes
	public static void asrVerifyNode(IGraph graph, INode iNode) {
		iNode.setAttributeValue("asrConfidence", 1.0);
		removeAlternativeNodes(graph, iNode);
	}

	// remove the alternative nodes of a node
	public static void removeAlternativeNodes(IGraph graph, INode iNode) {
		// select alternative nodes
		Set<? extends IArc> outgoingArcs = (Set<? extends IArc>) iNode.getOutgoingArcs();
		List<INode> alternativeNodes = new ArrayList<INode>();
		System.out.println(iNode.toString());
		for (IArc outgoingArc : outgoingArcs) {
			INode targetNode = outgoingArc.getTargetNode();
			if (targetNode.getType().getName().equals("alternative_token")) {
				alternativeNodes.add(targetNode);
			}
		}

		// delete alternative nodes (if you try to delete in the for-loop above
		// you will receive a ConcurrentModificationException because you will
		// have two concurrent threads - one for the iteration and one which
		// tries to delete)
		while (!alternativeNodes.isEmpty()) {
			graph.deleteNode(alternativeNodes.get(0));
			alternativeNodes.remove(0);
		}
	}

	// count specific nodes (e. g. "token") of an array containing nodes
	public static int countNodes(String nodeType, IGraph graph) {
		int counter = 0;
		for (Iterator<INode> iterator = graph.getNodes().iterator(); iterator.hasNext();) {
			INode iNode = iterator.next();
			if (iNode.getType().getName().toString().equals(nodeType)) {
				counter++;
			}
		}
		return counter;
	}
	
	// get previous verb node
	public static INode getPreviousVerbNode(INode iNode) {
		// take a previous node, if iNode is a already a verb
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
		// if iNode is already a noun phrase
		while (iNode.getAttributeValue("chunkName").equals("NP")) {
			boolean relationArcFound = false;
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
			boolean relationArcFound = false;
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
		
		// the second noun phrase
		boolean search = true;
		while (iNode.getAttributeValue("chunkName").equals("NP") && search) {
			boolean relationArcFound = false;
			for (IArc iArc : iNode.getOutgoingArcs()) {
				if (iArc.getType().getName().equals("relation")) {
					relationArcFound = true;
					if (!iArc.getTargetNode().getAttributeValue("chunkName").equals("NP")) {
						// do not the nodes it anymore, because this node is behind the next noun phrase
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
