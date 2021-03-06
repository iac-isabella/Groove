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
 * $Id: LTLTest.java 5702 2015-04-03 08:17:56Z rensink $
 */

package groove.test.verify;

import static org.junit.Assert.assertEquals;
import groove.explore.AcceptorValue;
import groove.explore.Exploration;
import groove.explore.Generator;
import groove.explore.StrategyValue;
import groove.explore.encode.Serialized;
import groove.explore.encode.Template;
import groove.explore.strategy.GraphNodeSizeBoundary;
import groove.explore.strategy.Strategy;
import groove.lts.GTS;
import groove.util.parse.FormatException;
import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the CTLStarFormula class.
 * @author Harmen Kastenberg
 * @version $Revision: 5702 $
 */
public class LTLTest {
    private StrategyValue strategyValue;
    private Template<Strategy> strategyTemplate;
    /** Transition system used by this test. */
    private GTS gts;

    /**
     * Tests whether the circular buffer fulfils certain properties and whether
     * the number of counter examples is correct for other properties.
     */
    @Test
    public void testCircularBuffer() {
        prepare("circular-buffer");
    }

    /** Test on a specially designed transition system. */
    @Test
    public void testNormal() {
        prepare(StrategyValue.LTL);
        testMC();
    }

    /** Test on a specially designed transition system. */
    @Test
    public void testBounded() {
        prepare(StrategyValue.LTL_BOUNDED);
        testMC();
    }

    /** Test on a specially designed transition system. */
    @Test
    public void testPocket() {
        prepare(StrategyValue.LTL_POCKET);
        testMC();
    }

    /** Test on a specially designed transition system. */
    private void testMC() {
        prepare("mc");
        testFormula("p U r", false);
        testFormula("p W r", true);
        testFormula("GF q", true);
        testFormula("FG p", false);
        testFormula("!FG p", false);
        testFormula("q M (p|r)", true);
        testFormula("p R r", false);
        //        testFormula("r R (p|q)", true);
        testFormula("G p", false);
        testFormula("G(p|q)", true);
        testFormula("G(p|X(q|X q))", true);
        testFormula("X q", true);
    }

    /** Sets the LTL strategy. */
    private void prepare(StrategyValue ltlStrategy) {
        this.strategyValue = ltlStrategy;
        this.strategyTemplate = ltlStrategy.getTemplate();
    }

    /** Sets the GTS to a given grammar in the JUnit samples. */
    private void prepare(String grammarName) {
        try {
            Generator generator = new Generator("-v", "0", "junit/samples/" + grammarName);
            this.gts = generator.start().getGTS();
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    /** Tests the number of counterexamples in the current;y
     * set GTS for a given formula. */
    private void testFormula(String formula, boolean succeed) {
        Serialized strategy = null;
        switch (this.strategyValue) {
        case LTL:
            strategy = this.strategyTemplate.toSerialized(formula);
            break;
        case LTL_BOUNDED:
        case LTL_POCKET:
            strategy = this.strategyTemplate.toSerialized(formula, new GraphNodeSizeBoundary(0, 1));
        }
        Exploration exploration = new Exploration(strategy, AcceptorValue.CYCLE.toSerialized(), 1);
        try {
            exploration.play(this.gts, this.gts.startState());
        } catch (FormatException e) {
            Assert.fail();
        }
        assertEquals(succeed, exploration.getResult().isEmpty());
    }
}
