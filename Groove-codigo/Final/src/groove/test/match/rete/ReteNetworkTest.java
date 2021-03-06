/* GROOVE: GRaphs for Object Oriented VErification
 * Copyright 2003--2007 University of Twente
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * $Id: ReteNetworkTest.java 5480 2014-07-19 22:15:15Z rensink $
 */
package groove.test.match.rete;

import groove.grammar.Grammar;
import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.match.rete.ConditionChecker;
import groove.match.rete.ProductionNode;
import groove.match.rete.ReteNetwork;
import groove.match.rete.ReteNetworkNode;
import groove.match.rete.ReteSearchEngine;
import groove.match.rete.ReteSimpleMatch;
import groove.util.parse.FormatException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Arash Jalali
 * @version $Revision $
 */
public class ReteNetworkTest extends TestCase {
    /*
     * Essa implementa��o de ReteNetwork faz parte do c�digo legado que n�o est� mais sendo mantido.
     */

    /** Location of the samples. */
    public static final String INPUT_DIR = "junit/samples";

    @Override
    public void setUp() {
        //Nothing in particular to set up.
    }

    /**
     * Tests the static structure of the RETE network for an empty grammar.
     */
    public void testStaticEmptyGrammar() {
        ReteSearchEngine g = new ReteSearchEngine(new Grammar("empty"));
        ReteNetwork network = g.getNetwork();
        assertEquals(0, network.getRoot().getSuccessors().size());
        assertEquals(0, network.getConditonCheckerNodes().size());
        assertEquals(0, network.getProductionNodes().size());
    }

    /**
     * RETE static structure test for the simple grammar.
     */
    public void testStaticSimple() {
        Grammar g = loadGrammar("simple.gps", "start");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        testNetworkStructure(network);
    }

    /**
     * RETE static structure test for the Petri Net grammar.
     */
    public void testStaticPetriNet() {
        Grammar g = loadGrammar("petrinet.gps", "start");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        testNetworkStructure(network);
    }

    /**
     * Testing the basic basic-regexp grammar for first step matches found - 1
     */
    public void testDynamicRegExp1() {
        Grammar g = loadGrammar("basic-regexp.gps", "g1");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        network.processGraph(g.getStartGraph());
        for (ProductionNode pn : network.getProductionNodes()) {
            Set<ReteSimpleMatch> rmList = pn.getConflictSet();
            if (pn.getProductionRule().getFullName().toString().equals("neg")) {
                assertEquals(pn.toString(), 0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("neg2")) {
                assertEquals(pn.toString(), 0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("rev")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("seq")) {
                assertEquals(pn.toString(), 0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("neq")) {
                assertEquals(pn.toString(), 2, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("star")) {
                assertEquals(pn.toString(), 0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("var")) {
                assertEquals(pn.toString(), 2, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("wildcard1")) {
                assertEquals(pn.toString(), 0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("wildcard2")) {
                assertEquals(pn.toString(), 0, rmList.size());
            }
        }
    }

    /**
     * Testing the basic basic-regexp grammar for first step matches found - 2
     */
    public void testDynamicRegExp2() {
        Grammar g = loadGrammar("basic-regexp.gps", "g2");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        network.processGraph(g.getStartGraph());
        for (ProductionNode pn : network.getProductionNodes()) {
            Set<ReteSimpleMatch> rmList = pn.getConflictSet();
            if (pn.getProductionRule().getFullName().toString().equals("neg")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("neg2")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("rev")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("seq")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("neq")) {
                assertEquals(pn.toString(), 6, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("star")) {
                assertEquals(pn.toString(), 0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("var")) {
                assertEquals(pn.toString(), 3, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("wildcard1")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("wildcard2")) {
                assertEquals(pn.toString(), 1, rmList.size());
            }

        }
    }

    /**
     * Testing the basic basic-regexp grammar for first step matches found - 3
     */
    public void testDynamicRegExp3() {
        Grammar g = loadGrammar("basic-regexp.gps", "g3");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        network.processGraph(g.getStartGraph());
        for (ProductionNode pn : network.getProductionNodes()) {
            Set<ReteSimpleMatch> rmList = pn.getConflictSet();
            if (pn.getProductionRule().getFullName().toString().equals("neg")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("neg2")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("rev")) {
                assertEquals(pn.toString(), 0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("seq")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("neq")) {
                assertEquals(pn.toString(), 12, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("star")) {
                assertEquals(pn.toString(), 0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("var")) {
                assertEquals(pn.toString(), 3, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("wildcard1")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("wildcard2")) {
                assertEquals(pn.toString(), 1, rmList.size());
            }
        }
    }

    /**
     * Testing the basic basic-regexp grammar for first step matches found - 4
     */
    public void testDynamicRegExp4() {
        Grammar g = loadGrammar("basic-regexp.gps", "g4");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        network.processGraph(g.getStartGraph());
        for (ProductionNode pn : network.getProductionNodes()) {
            Set<ReteSimpleMatch> rmList = pn.getConflictSet();
            if (pn.getProductionRule().getFullName().toString().equals("neg")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("neg2")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("rev")) {
                assertEquals(pn.toString(), 0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("seq")) {
                assertEquals(pn.toString(), 0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("neq")) {
                assertEquals(pn.toString(), 42, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("star")) {
                assertEquals(pn.toString(), 3, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("var")) {
                assertEquals(pn.toString(), 10, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("wildcard1")) {
                assertEquals(pn.toString(), 2, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("wildcard2")) {
                assertEquals(pn.toString(), 2, rmList.size());
            }
        }
    }

    /**
     * Testing closure's sufficient coverage
     */
    public void testDynamicRegExp5() {
        Grammar g = loadGrammar("basic-regexp.gps", "g5");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        network.processGraph(g.getStartGraph());
        for (ProductionNode pn : network.getProductionNodes()) {
            Set<ReteSimpleMatch> rmList = pn.getConflictSet();
            if (pn.getProductionRule().getFullName().toString().equals("closure")) {
                assertEquals(pn.toString(), 3, rmList.size());
            }
        }
    }

    private void testNetworkStructure(ReteNetwork network) {
        for (ConditionChecker cc : network.getConditonCheckerNodes()) {
            checkBilateralConnectivity(cc);
        }
    }

    private void checkBilateralConnectivity(ReteNetworkNode nnode) {
        Collection<ReteNetworkNode> ants = nnode.getAntecedents();
        for (ReteNetworkNode ant : ants) {
            assertTrue(ant.getSuccessors().contains(nnode));
            checkBilateralConnectivity(ant);
        }
        if (ants.isEmpty()) {
            if (nnode instanceof ConditionChecker) {
                assertTrue(((ConditionChecker) nnode).getCondition().getPattern().isEmpty());
            } else {
                assertEquals(nnode.getOwner().getRoot(), nnode);
            }
        }
    }

    /**
     * Dynamic behavior test of the RETE for a grammar with simple injective rules.
     */
    public void testDynamicSimpleInjectiveRule() {
        Grammar g = loadGrammar("simpleInjective.gps", "start");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        network.processGraph(g.getStartGraph());
        for (ProductionNode pn : network.getProductionNodes()) {
            Set<ReteSimpleMatch> rmList = pn.getConflictSet();
            if (pn.getProductionRule().getFullName().toString().equals("addA")) {
                assertEquals(4, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("delBNode")) {
                assertEquals(2, rmList.size());
            }
        }
    }

    /**
     * Dynamic behavior test of the RETE for the control grammar.
     */
    public void testDynamicControl() {
        Grammar g = loadGrammar("control.gps", "start");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        network.processGraph(g.getStartGraph());
        for (ProductionNode pn : network.getProductionNodes()) {
            Set<ReteSimpleMatch> rmList = pn.getConflictSet();
            if (pn.getProductionRule().getFullName().toString().equals("move")) {
                assertEquals(pn.toString(), 1, rmList.size());
            } else {
                assertEquals(pn.toString(), 0, rmList.size());
            }
        }
    }

    /**
     * Testing the leader-election grammar for first step matches found - 1
     */
    public void testDynamicLeaderElection() {
        Grammar g = loadGrammar("leader-election.gps", "start-2");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        network.processGraph(g.getStartGraph());
        for (ProductionNode pn : network.getProductionNodes()) {
            Set<ReteSimpleMatch> rmList = pn.getConflictSet();
            if (pn.getProductionRule().getFullName().toString().equals("pick-number")) {
                assertEquals(4, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("pass-message")) {
                assertEquals(1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("next-phase")) {
                assertEquals(1, rmList.size());
            } else {
                assertEquals(pn.getProductionRule().getFullName(), 0, rmList.size());
            }
        }
    }

    /**
     * Tests the first step of the counting quantifier example
     */
    public void testDynamicQuantifierCount1() {
        Grammar g = loadGrammar("quantifierCounter.gps", "start");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        network.processGraph(g.getStartGraph());
        for (ProductionNode pn : network.getProductionNodes()) {
            Set<ReteSimpleMatch> rmList = pn.getConflictSet();
            if (pn.getProductionRule().getFullName().toString().equals("sameCount")) {
                assertEquals(1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("noMoreThan3A")) {
                assertEquals(1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("noMoreThan3B")) {
                assertEquals(1, rmList.size());
            } else {
                assertEquals(pn.getProductionRule().getFullName(), 0, rmList.size());
            }
        }
    }

    /**
     * Tests the first step of the counting quantifier example
     */
    public void testDynamicQuantifierCount2() {
        Grammar g = loadGrammar("quantifierCounter.gps", "twoAOneB");
        ReteSearchEngine eng = new ReteSearchEngine(g);
        ReteNetwork network = eng.getNetwork();
        network.processGraph(g.getStartGraph());
        for (ProductionNode pn : network.getProductionNodes()) {
            Set<ReteSimpleMatch> rmList = pn.getConflictSet();
            if (pn.getProductionRule().getFullName().toString().equals("sameCount")) {
                assertEquals(0, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("noMoreThan3A")) {
                assertEquals(1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("noMoreThan3B")) {
                assertEquals(1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("countOfAequals2")) {
                assertEquals(1, rmList.size());
            } else if (pn.getProductionRule().getFullName().toString().equals("threeOfAnyKind")) {
                assertEquals(1, rmList.size());
            } else {
                assertEquals(pn.getProductionRule().getFullName(), 0, rmList.size());
            }
        }
    }

    private Grammar loadGrammar(String grammarName, String startGraphName) {
        Grammar result = null;
        try {
            GrammarModel model = GrammarModel.newInstance(new File(INPUT_DIR, grammarName), false);
            model.setLocalActiveNames(ResourceKind.HOST, startGraphName);
            result = model.toGrammar();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        } catch (FormatException ex) {
            Assert.fail("Could not load grammar. " + ex.getMessage());
        }
        return result;
    }
}
