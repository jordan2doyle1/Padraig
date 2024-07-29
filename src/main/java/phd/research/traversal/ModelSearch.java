package phd.research.traversal;

import io.appium.java_client.android.AndroidElement;
import org.jgrapht.graph.DefaultEdge;
import org.openqa.selenium.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.appium.AppiumManager;
import phd.research.core.DroidGraph;
import phd.research.ui.BackElement;
import phd.research.ui.InputHistory;
import phd.research.ui.InputRecord;
import phd.research.vertices.ControlVertex;
import phd.research.vertices.Vertex;

import java.util.*;

/**
 * @author Jordan Doyle
 */

public class ModelSearch extends Search {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelSearch.class);

    private final InputHistory inputHistory;
    private final DroidGraph graph;

    public ModelSearch() {
        super();
        this.inputHistory = new InputHistory();
        this.graph = new DroidGraph();
    }

    @Override
    protected List<AndroidElement> getAvailableControls() {
        return this.appiumManager.getElementsOnScreen();
    }

    @Override
    protected AndroidElement getNextControl(String activity, List<AndroidElement> controls) {
        Map<Integer, Integer> controlGains = new HashMap<>();
        for (int i = 0; i < controls.size(); i++) {
            AndroidElement control = controls.get(i);
            int gain = 0;
            String id = control.getAttribute("resource-id");
            if (id != null && !id.equals("null") && !id.isEmpty()) {
                id = id.split("/")[1];
                gain = getControlGain(id);
            }
            LOGGER.info("Coverage gain for control {} is {}", i, gain);
            controlGains.put(i, gain);
        }

        Map.Entry<Integer, Integer> maxEntry = Collections.max(controlGains.entrySet(), Map.Entry.comparingByValue());
        AndroidElement chosenControl = controls.get(maxEntry.getKey());
        controlGains.entrySet().removeIf(entry -> entry.getValue().compareTo(maxEntry.getValue()) < 0);
        if (controlGains.size() > 1) {
            LOGGER.warn("Multiple controls found with the same coverage gain. Choosing systematically.");
            List<AndroidElement> equalControls = new ArrayList<>();
            for (Integer index : controlGains.keySet()) {
                equalControls.add(controls.get(index));
            }
            chosenControl = getNextSystematically(activity, equalControls);
        }

        String id = chosenControl.getAttribute("resource-id");
        if (id != null && !id.equals("null") && !id.isEmpty()) {
            id = id.split("/")[1];
            visitTraceStartingAt(id);
        }

        return chosenControl;
    }

    protected AndroidElement getNextSystematically(String activity, List<AndroidElement> controls) {
        for (AndroidElement currentControl : controls) {
            if (!this.inputHistory.containsInput(activity, currentControl)) {
                this.inputHistory.addInput(activity, currentControl);
                return currentControl;
            }
        }

        AndroidElement nextControl = controls.get(0);
        InputRecord nextRecord = this.inputHistory.getInputRecord(activity, nextControl);
        for (AndroidElement currentControl : controls) {
            InputRecord currentRecord = this.inputHistory.getInputRecord(activity, currentControl);
            if (currentRecord.isLeaf()) {
                continue;
            }

            if (currentRecord.getInputCount() < nextRecord.getInputCount()) {
                nextControl = currentControl;
                nextRecord = currentRecord;
            }
        }

        return nextControl;
    }

    @Override
    public void applyInput(String activity, AndroidElement control) {
        String activityBeforeInput = this.appiumManager.getCurrentActivity();
        this.inputHistory.incrementInputCount(activity, control);
        String cmd = control instanceof BackElement ? Search.backCommand() : Search.tapCommand(control.getCenter());
        this.inputTracker.logInput(cmd, AppiumManager.getElementDescription(control));
        Point centre = control.getCenter();
        control.click();

        AppiumManager.waitForUi(3f);
        if (activityBeforeInput.equals(this.appiumManager.getCurrentActivity())) {
            LOGGER.info("Setting last input as a leaf.");
            this.inputHistory.setAsLeaf(activity, centre);
        }
    }

    private int getControlGain(String id) {
        if (id.equals("back")) {
            return 0;
        }

        Vertex startingVertex = getControlVertexWithId(id);
        if (startingVertex == null) {
            //LOGGER.error("Failed to find control with ID (" + id + ") in the graph");
            return 0;
        }

        int gain = countVertex(startingVertex);
        this.resetLocalVisit();
        return gain;
    }

    public void resetLocalVisit() {
        for (Vertex v : this.graph.getControlFlowGraph().vertexSet()) {
            v.localVisitReset();
        }
    }

    private int countVertex(Vertex vertex) {
        int count = vertex.hasVisit() ? 0 : 1;
        vertex.localVisit();

        Set<DefaultEdge> edges = this.graph.getControlFlowGraph().outgoingEdgesOf(vertex);
        for (DefaultEdge edge : edges) {
            Vertex targetVertex = this.graph.getControlFlowGraph().getEdgeTarget(edge);
            if (!targetVertex.hasLocalVisit()) {
                count = count + countVertex(targetVertex);
            }
        }

        return count;
    }

    private void visitTraceStartingAt(String id) {
        if (id.equals("back")) {
            return;
        }

        Vertex startingVertex = getControlVertexWithId(id);
        if (startingVertex == null) {
            LOGGER.error("Failed to find control with ID ({}) in the graph", id);
            return;
        }

        visitVertex(startingVertex);
    }

    private void visitVertex(Vertex vertex) {
        vertex.visit();
        vertex.localVisit();

        Set<DefaultEdge> edges = this.graph.getControlFlowGraph().outgoingEdgesOf(vertex);
        for (DefaultEdge edge : edges) {
            Vertex targetVertex = this.graph.getControlFlowGraph().getEdgeTarget(edge);
            if (!targetVertex.hasLocalVisit()) {
                visitVertex(targetVertex);
            }
        }
    }

    private Vertex getControlVertexWithId(String id) {
        for (Vertex vertex : this.graph.getControlFlowGraph().vertexSet()) {
            if (vertex instanceof ControlVertex) {
                ControlVertex controlVertex = (ControlVertex) vertex;
                if (controlVertex.getControl().getControlName().equals(id)) {
                    return controlVertex;
                }
            }
        }
        return null;
    }
}