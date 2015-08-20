/*
 * GROOVE: GRaphs for Object Oriented VErification Copyright 2003--2007
 * University of Twente
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * $Id: CTLModelChecker.java 5702 2015-04-03 08:17:56Z rensink $
 */
package groove.verify;

import groove.explore.ExploreResult;
import groove.explore.Generator;
import groove.explore.Generator.LTSLabelsHandler;
import groove.explore.util.LTSLabels;
import groove.explore.util.LTSLabels.Flag;
import groove.graph.Edge;
import groove.graph.Graph;
import groove.graph.Node;
import groove.lts.GTS;
import groove.lts.GraphState;
import groove.util.Groove;
import groove.util.cli.GrooveCmdLineParser;
import groove.util.cli.GrooveCmdLineTool;
import groove.util.parse.FormatException;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.FileOptionHandler;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * Command-line tool directing the model checking process.
 *
 * @author Harmen Kastenberg
 * @version $Revision: 5702 $ $Date: 2008-03-28 07:03:03 $
 */
public class CTLModelChecker extends GrooveCmdLineTool<Object> {
    /**
     * Constructor.
     * @param args the command-line arguments for the model checker
     */
    public CTLModelChecker(String... args) {
        super("ModelChecker", args);
    }

    @Override
    protected GrooveCmdLineParser createParser(String appName) {
        GrooveCmdLineParser result = new GrooveCmdLineParser(appName, this) {
            @Override
            public void printSingleLineUsage(Writer w, ResourceBundle rb) {
                int optionCount = getOptions().size();
                PrintWriter pw = new PrintWriter(w);
                for (int ix = 0; ix < optionCount - 1; ix++) {
                    printSingleLineOption(pw, getOptions().get(ix), rb, true);
                }
                pw.print(" [");
                pw.print(getOptions().get(optionCount - 1).getNameAndMeta(rb));
                pw.print(" | ");
                pw.print(getArguments().get(0).getNameAndMeta(rb));
                pw.print(']');
                pw.flush();
            }
        };
        // move -g to the final position
        @SuppressWarnings("rawtypes")
        List<OptionHandler> handlers = result.getOptions();
        OptionHandler<?> genHandler = null;
        for (OptionHandler<?> handler : handlers) {
            if (handler instanceof GeneratorHandler) {
                genHandler = handler;
            }
        }
        handlers.remove(genHandler);
        handlers.add(genHandler);
        return result;
    }

    /**
     * Method managing the actual work to be done.
     */
    @Override
    protected Object run() throws Exception {
        modelCheck(this.genArgs == null ? null : this.genArgs.get());
        return null;
    }

    private void modelCheck(String[] genArgs) throws Exception {
        long genStartTime = System.currentTimeMillis();
        Model model;
        if (genArgs != null) {
            try {
                model = new GTSModel(Generator.execute(genArgs));
            } catch (Exception e) {
                throw new Exception("Error while invoking Generator\n" + e.getMessage(), e);
            }
        } else {
            emit("Model: %s%n", this.modelGraph);
            model = new GraphModel(Groove.loadGraph(this.modelGraph), this.ltsLabels);
        }
        long mcStartTime = System.currentTimeMillis();
        int maxWidth = 0;
        Map<Formula,Boolean> outcome = new HashMap<Formula,Boolean>();
        for (Formula property : this.properties) {
            maxWidth = Math.max(maxWidth, property.getParseString().length());
            CTLMarker marker = new CTLMarker(property, model);
            outcome.put(property, marker.hasValue(true));
        }
        emit("%nModel checking outcome:%n");
        for (Formula property : this.properties) {
            emit("    %-" + maxWidth + "s : %s%n", property.getParseString(), outcome.get(property)
                ? "satisfied" : "violated");
        }
        long endTime = System.currentTimeMillis();

        emit("%n** Model Checking Time (ms):\t%d%n", endTime - mcStartTime);
        emit("** Total Running Time (ms):\t%d%n", endTime - genStartTime);
    }

    @Option(name = "-ef", metaVar = "flags", usage = "" + "Special GTS labels. Legal values are:\n" //
        + "  s - start state label (default: 'start')\n" //
        + "  f - final states label (default: 'final')\n" //
        + "  o - open states label (default: 'open')\n" //
        + "  r - result state label (default: 'result')" //
        + "Specify label to be used by appending flag with 'label' (single-quoted)",
        handler = LTSLabelsHandler.class)
    private LTSLabels ltsLabels;

    @Option(name = "-ctl", metaVar = "form", usage = "Check the formula <form> (multiple allowed)",
        handler = FormulaHandler.class, required = true)
    private List<Formula> properties;
    @Option(name = "-g", metaVar = "args",
        usage = "Invoke the generator using <args> as options + arguments",
        handler = GeneratorHandler.class)
    private GeneratorArgs genArgs;

    @Argument(metaVar = "graph", usage = "File name of graph to be checked",
        handler = FileOptionHandler.class)
    private File modelGraph;

    /**
     * Main method.
     * Always exits with {@link System#exit(int)}; see {@link #execute(String[])}
     * for programmatic use.
     * @param args the list of command-line arguments
     */
    public static void main(String args[]) {
        tryExecute(CTLModelChecker.class, args);
    }

    /**
     * Constructs and invokes a model checker instance.
     * @param args the list of command-line arguments
     */
    public static void execute(String args[]) throws Exception {
        new CTLModelChecker(args).start();
    }

    /** Option handler for CTL formulas. */
    public static class FormulaHandler extends OneArgumentOptionHandler<Formula> {
        /**
         * Required constructor.
         */
        public FormulaHandler(CmdLineParser parser, OptionDef option, Setter<? super Formula> setter) {
            super(parser, option, setter);
        }

        @Override
        protected Formula parse(String argument) throws CmdLineException {
            try {
                return Formula.parse(Logic.CTL, argument).toCtlFormula();
            } catch (FormatException e) {
                throw new CmdLineException(this.owner, e);
            }
        }
    }

    /** Option handler for the '-g' option. */
    public static class GeneratorHandler extends OptionHandler<GeneratorArgs> {
        /** Required constructor. */
        public GeneratorHandler(CmdLineParser parser, OptionDef option,
            Setter<? super GeneratorArgs> setter) {
            super(parser, option, setter);
        }

        @Override
        public int parseArguments(Parameters params) throws CmdLineException {
            ArrayList<String> genArgs = new ArrayList<String>();
            for (int ix = 0; ix < params.size(); ix++) {
                genArgs.add(params.getParameter(ix));
            }
            this.setter.addValue(new GeneratorArgs(params));
            return params.size();
        }

        @Override
        public String getDefaultMetaVariable() {
            return "generator-args";
        }
    }

    /**
     * Option value class collecting all remaining arguments.
     * Wrapped into a class to fool Args4J into understanding this is not a multiple value.
     */
    private static class GeneratorArgs {
        GeneratorArgs(Parameters params) throws CmdLineException {
            this.args = new ArrayList<String>();
            for (int ix = 0; ix < params.size(); ix++) {
                this.args.add(params.getParameter(ix));
            }
        }

        /** Returns the content of this argument, as a string array. */
        public String[] get() {
            return this.args.toArray(new String[0]);
        }

        private final List<String> args;
    }

    /** Creates a CTL-checkable model from an exploration result. */
    public static Model newModel(ExploreResult result) {
        return new GTSModel(result);
    }

    /** Creates a CTL-checkable model from a graph plus special labels mapping.
     * @throws FormatException if the graph is not compatible with the special labels.
     */
    public static Model newModel(Graph graph, LTSLabels ltsLabels) throws FormatException {
        return new GraphModel(graph, ltsLabels == null ? LTSLabels.DEFAULT : ltsLabels);
    }

    /** Facade for models, with the functionality required for CTL model checking. */
    public static interface Model {
        /** Returns the number of (real) nodes of the model. */
        int nodeCount();

        /** Returns the set of (real) nodes of the model. */
        Set<? extends Node> nodeSet();

        /** Returns the set of (real) outgoing edges of a node. */
        Set<? extends Edge> outEdgeSet(Node node);

        /** Tests if a given node satisfies the special property expressed by a given flag. */
        boolean isSpecial(Node node, Flag flag);

        // EZ says: change for SF bug #442. See below.
        /**
         * Return the proper index of the given node to be used in the arrays.
         * Usually the index is the same as the node number, but this can change
         * when the GTS has absent states.
         */
        int nodeIndex(Node node);

        /** Returns the special-label-flag corresponding to a given edge label, if any. */
        Flag getFlag(String label);
    }

    /*
     * EZ says: this is a hack to fix SF bug #442.
     * The new level of indirection introduced by having to check the node
     * index with the model obviously hurts performance a bit. But... this
     * change touched just a few parts of the code and mainly at the
     * initialization. So I'd say that this is not so bad...
     */
    /** Model built from an exploration result. */
    private static class GTSModel implements Model {
        /** Maps an exploration result into a model. */
        public GTSModel(ExploreResult result) {
            this.gts = result.getGTS();
            this.result = result;
            if (this.gts.hasAbsentStates() || this.gts.hasTransientStates()) {
                this.nodeIdxMap = new HashMap<GraphState,Integer>();
                int nr = 0;
                for (GraphState state : this.gts.getStates()) {
                    this.nodeIdxMap.put(state, nr);
                    nr++;
                }
            } else {
                this.nodeIdxMap = null;
            }
        }

        private final ExploreResult result;
        private final GTS gts;

        @Override
        public int nodeCount() {
            return this.nodeSet().size();
        }

        @Override
        public Set<? extends Node> nodeSet() {
            return this.gts.getStates();
        }

        @Override
        public Set<? extends Edge> outEdgeSet(Node node) {
            return ((GraphState) node).getTransitions();
        }

        @Override
        public boolean isSpecial(Node node, Flag flag) {
            GraphState state = (GraphState) node;
            boolean result = false;
            switch (flag) {
            case FINAL:
                result = state.isFinal();
                break;
            case OPEN:
                result = !state.isClosed();
                break;
            case START:
                result = state == this.gts.startState();
                break;
            case RESULT:
                result = this.result != null && this.result.containsState(state);
            }
            return result;
        }

        @Override
        public int nodeIndex(Node node) {
            if (this.nodeIdxMap == null) {
                return node.getNumber();
            } else {
                return this.nodeIdxMap.get(node);
            }
        }

        private final Map<GraphState,Integer> nodeIdxMap;

        @Override
        public Flag getFlag(String label) {
            return null;
        }
    }

    /** Model built from a graph and a special labels mapping. */
    private static class GraphModel implements Model {
        /** Wraps a graph and a special labels mapping into a model.
         * @throws FormatException if the graph is not compatible with the special labels.
         */
        public GraphModel(Graph graph, LTSLabels ltsLabels) throws FormatException {
            this.graph = graph;
            this.ltsLabels = ltsLabels == null ? LTSLabels.DEFAULT : ltsLabels;
            testFormat();
        }

        /** Tests if the model is consistent with the special state markers.
         * @throws FormatException if the model has special state markers that occur
         * on edge labels
         */
        private void testFormat() throws FormatException {
            boolean startFound = false;
            for (Node node : nodeSet()) {
                Set<? extends Edge> outEdges = outEdgeSet(node);
                for (Edge outEdge : outEdges) {
                    Flag flag = getFlag(outEdge.label().text());
                    if (flag == null) {
                        continue;
                    }
                    if (!outEdge.isLoop()) {
                        throw new FormatException(
                            "Special state marker '%s' occurs as edge label in model",
                            outEdge.label());
                    }
                    if (flag == Flag.START) {
                        if (startFound) {
                            throw new FormatException(
                                "Start state marker '%s' occurs more than once in model",
                                outEdge.label());
                        } else {
                            startFound = true;
                        }
                    }
                }
            }
            if (!startFound) {
                throw new FormatException("Start state marker '%s' does not occur in model",
                    this.ltsLabels.getLabel(Flag.START));
            }
        }

        @Override
        public int nodeCount() {
            return this.graph.nodeCount();
        }

        @Override
        public Set<? extends Node> nodeSet() {
            return this.graph.nodeSet();
        }

        @Override
        public Set<? extends Edge> outEdgeSet(Node node) {
            return this.graph.outEdgeSet(node);
        }

        @Override
        public boolean isSpecial(Node node, Flag flag) {
            return false;
        }

        private final Graph graph;

        @Override
        public int nodeIndex(Node node) {
            return node.getNumber();
        }

        @Override
        public Flag getFlag(String label) {
            Flag result = null;
            if (this.ltsLabels != null) {
                return this.ltsLabels.getFlag(label);
            }
            return result;
        }

        private final LTSLabels ltsLabels;
    }
}