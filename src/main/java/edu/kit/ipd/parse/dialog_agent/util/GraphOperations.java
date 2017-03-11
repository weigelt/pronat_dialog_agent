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
		graph.replaceNode(oldNode, newNode, false);
	}

	// verifies a node with low confidence and removes his alternative nodes
	public static void verifyNode(IGraph graph, INode iNode) {
		iNode.setAttributeValue("asrConfidence", 1.0);
//		iNode.setAttributeValue("verifiedByDialogAgent", true);
		removeAlternativeNodes(graph, iNode);
	}

	// remove the alternative nodes of a node
	public static void removeAlternativeNodes(IGraph graph, INode iNode) {
		// select alternative nodes
		Set<? extends IArc> outgoingArcs = iNode.getOutgoingArcs();
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
}
