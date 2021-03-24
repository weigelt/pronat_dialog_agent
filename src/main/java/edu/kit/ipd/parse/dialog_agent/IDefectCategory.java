package edu.kit.ipd.parse.dialog_agent;

import edu.kit.ipd.parse.luna.graph.IGraph;

public interface IDefectCategory {

	void processDefectCategory(IGraph graph);

	void nextDefectCategory(IDefectCategory nextDefectCategory);
}
