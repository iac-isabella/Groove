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
 * $Id: IOTest.java 5480 2014-07-19 22:15:15Z rensink $
 */
package groove.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import groove.explore.Exploration;
import groove.grammar.Grammar;
import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.lts.GTS;
import groove.util.Groove;
import groove.util.parse.FormatException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Tom Staijen
 */
@SuppressWarnings("all")
public class IOTest {

    private static final String DIRECTORY = "junit/samples/control.gps";
    private static final String JAR_FILE = "junit/samples.jar";
    private static final String ZIP_FILE = "junit/samples.zip";
    private static final String PATH_IN_ARCHIVE = "/samples/control.gps";

    private static final String DEF_START = "start";
    private static final String ALT_START = "start2";
    private static final String DEF_CONTROL = "control";
    private static final String ALT_CONTROL = "control2";

    /** test loading a directory grammar directly */
    @Test
    public void testLoadDefault() {
        int nodecount = 11;
        int edgecount = 12;
        try {
            testControl(Groove.loadGrammar(DIRECTORY), DEF_START, DEF_CONTROL, nodecount, edgecount);
            testControl(Groove.loadGrammar(DIRECTORY), DEF_START, DEF_CONTROL, nodecount, edgecount);

            File file = new File(DIRECTORY);
            URL url = Groove.toURL(file);

            testControl(GrammarModel.newInstance(file, false), DEF_START, DEF_CONTROL, nodecount,
                edgecount);
            testControl(GrammarModel.newInstance(file, false), DEF_START, DEF_CONTROL, nodecount,
                edgecount);

            testControl(GrammarModel.newInstance(url), DEF_START, DEF_CONTROL, nodecount, edgecount);
            testControl(GrammarModel.newInstance(url), DEF_START, DEF_CONTROL, nodecount, edgecount);
            testControl(GrammarModel.newInstance(url), DEF_START, DEF_CONTROL, nodecount, edgecount);

        } catch (IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testLoadAltStart() {
        int nodecount = 12;
        int edgecount = 14;
        try {
            testControl(Groove.loadGrammar(DIRECTORY), ALT_START, DEF_CONTROL, nodecount, edgecount);
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testLoadAltBoth() {
        int nodecount = 13;
        int edgecount = 16;
        try {
            URL dir = Groove.toURL(new File(DIRECTORY));
            GrammarModel grammarView = GrammarModel.newInstance(dir);
            testControl(grammarView, ALT_START, ALT_CONTROL, nodecount, edgecount);
        } catch (IOException e) {
            Assert.fail(e.toString());
        }

    }

    protected void testControl(GrammarModel view, String startName, String controlName,
            int nodecount, int edgecount) {
        testExploration(view, "control", startName, controlName, 3, nodecount, edgecount);
    }

    /**
     * Tests exploration of a given grammar, saving the GTS if required.
     * @param view the graph grammar to be tested
     * @param nodeCount expected number of nodes; disregarded if < 0
     * @param edgeCount expected number of edges; disregarded if < 0
     * @return the explored GTS
     */
    protected GTS testExploration(GrammarModel view, String grammarName, String startName,
            String controlName, int rulecount, int nodeCount, int edgeCount) {
        try {
            // and also set the start graph directly
            view.setLocalActiveNames(ResourceKind.CONTROL, controlName);
            view.setLocalActiveNames(ResourceKind.HOST, startName);

            // now instantiate the grammar
            Grammar gg = view.toGrammar();

            assertEquals(grammarName, gg.getName());
            assertEquals(startName, view.getStartGraphModel().getFullName());
            //            assertEquals(controlName, gg.getCtrlAut().getName());
            assertEquals(rulecount, gg.getActions().size());

            GTS lts = new GTS(gg);
            Exploration exploration = new Exploration();
            exploration.play(lts, null);
            assertFalse(exploration.isInterrupted());

            if (nodeCount >= 0) {
                assertEquals(nodeCount, lts.nodeCount());
            }
            if (edgeCount >= 0) {
                assertEquals(edgeCount, lts.edgeCount());
            }
            return lts;
        } catch (FormatException exc) {
            assertTrue(false);
            return null;
        }
    }

}
