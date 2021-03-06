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
 * $Id: FormulaTest.java 5489 2014-07-25 20:16:18Z rensink $
 */

package groove.test.verify;

import static groove.verify.Formula.Always;
import static groove.verify.Formula.And;
import static groove.verify.Formula.Call;
import static groove.verify.Formula.Equiv;
import static groove.verify.Formula.Eventually;
import static groove.verify.Formula.Exists;
import static groove.verify.Formula.False;
import static groove.verify.Formula.Follows;
import static groove.verify.Formula.Forall;
import static groove.verify.Formula.Implies;
import static groove.verify.Formula.Next;
import static groove.verify.Formula.Not;
import static groove.verify.Formula.Or;
import static groove.verify.Formula.Prop;
import static groove.verify.Formula.Release;
import static groove.verify.Formula.SRelease;
import static groove.verify.Formula.True;
import static groove.verify.Formula.Until;
import static groove.verify.Formula.WUntil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import groove.util.parse.FormatException;
import groove.util.parse.Id;
import groove.verify.Formula;
import groove.verify.FormulaParser;

import org.junit.Test;

/**
 * Tests the Formula class.
 * @author Harmen Kastenberg and Arend Rensink
 * @version $Revision: 5489 $
 */
@SuppressWarnings("all")
public class FormulaTest {
    /** Tests {@link FormulaParser#parse(String)}. */
    @Test
    public void testParse() {
        Formula a = Prop("a");
        Formula b = Prop("b");
        Formula c = Prop("c");
        Formula dc = Prop("dc");
        Formula d = Prop("d");
        Formula e = Prop("e");
        testParse("a", a);
        testParse("'a'", a);
        testParse("\"a\"", a);
        testParse("'a(1)'", Prop("a(1)"));
        testParse("a(1,id,'value')", Call(Id.id("a"), "1", "id", "'value'"));
        testParse("true", True());
        testParse("false", False());
        // and/or/not
        testParse("!!a&!b", And(Not(Not(a)), Not(b)));
        testParse("a&b&c", And(a, And(b, c)));
        testParse("a&b|c", Or(And(a, b), c));
        testParse("a|b&c", Or(a, And(b, c)));
        testParse("(!a)|(b&c)", Or(Not(a), And(b, c)));
        testParse("!((a|b)&c)", Not(And(Or(a, b), c)));
        // implies/follows/equiv
        testParse("a->b", Implies(a, b));
        testParse("a|c<-(b&dc)", Follows(Or(a, c), And(b, dc)));
        testParse("a->b<->c", Equiv(Implies(a, b), c));
        testParse("a->(b<->c)", Implies(a, Equiv(b, c)));
        //
        testParse("(a U b) M c R (d M e)", SRelease(Until(a, b), Release(c, SRelease(d, e))));
        //
        testParse("AFG X true", Forall(Eventually(Always(Next(True())))));
        // errors
        testParseError("a=");
        testParseError("(a");
        testParseError("a(");
        testParseError("(a)A");
        testParseError("(a)U");
        testParseError("AX");
        testParseError("'a");
        testParseError("U true");
        testParseError("F!(final U)");
    }

    private void testParse(String text, Formula expected) {
        Formula result = FormulaParser.instance().parse(text);
        assertEquals(expected, result);
        if (result.hasErrors()) {
            fail(result.getErrors().toString());
        }
    }

    private void testParseError(String text) {
        Formula result = FormulaParser.instance().parse(text);
        assertTrue(result.hasErrors());
    }

    /** Tests the toString method of the Formula class. */
    @Test
    public void testFormulaToString() {
        Formula a = Prop("a");
        Formula b = Prop("b");
        Formula c = Prop("c");
        // atoms
        testEquals("a", a);
        testEquals("true", True());
        testEquals("false", False());
        // negation
        testEquals("!a", Not(a));
        testEquals("!(a|b)", Not(Or(a, b)));
        testEquals("!a|b", Or(Not(a), b));
        // and/or
        testEquals("a&b", And(a, b));
        testEquals("a&b|c", Or(And(a, b), c));
        testEquals("a&(b|c)", And(a, Or(b, c)));
        // implies/follows/equiv
        testEquals("a->b", Implies(a, b));
        testEquals("a<-b", Follows(a, b));
        testEquals("a<->b", Equiv(a, b));
        testEquals("a<->b->c", Equiv(a, Implies(b, c)));
        testEquals("(a<->b)->c", Implies(Equiv(a, b), c));
        // next/always/eventually
        testEquals("X X a", Next(Next(a)));
        testEquals("F G a", Eventually(Always(a)));
        // until/release
        testEquals("a R b", Release(a, b));
        testEquals("a M b", SRelease(a, b));
        testEquals("a U b", Until(a, b));
        testEquals("a W b", WUntil(a, b));
        testEquals("(a->b) U c", Until(Implies(a, b), c));
        testEquals("a->b U c", Implies(a, Until(b, c)));
        // forall/exists
        testEquals("A true", Forall(True()));
        testEquals("E F a", Exists(Eventually(a)));
        testEquals("A a U b", Forall(Until(a, b)));
        testEquals("(A a) U b", Until(Forall(a), b));
    }

    private void testEquals(String s, Formula f) {
        assertEquals(s, f.toLine().toFlatString());
    }

    /** Tests if a given formula is ripe for CTL verification. */
    @Test
    public void testIsCtlFormula() {
        Formula a = Prop("a");
        Formula b = Prop("b");
        Formula c = Prop("c");
        // Any simple propositional formula
        assertTrue(a.isCtlFormula());
        assertTrue(True().isCtlFormula());
        assertTrue(False().isCtlFormula());
        assertTrue(And(Or(Not(a), b), c).isCtlFormula());
        // implication-like operators
        assertTrue(Implies(a, b).isCtlFormula());
        assertTrue(Follows(a, b).isCtlFormula());
        assertTrue(Equiv(a, b).isCtlFormula());
        // No next without path quantifier
        assertFalse(Next(a).isCtlFormula());
        assertTrue(Exists(Next(a)).isCtlFormula());
        // No until without path quantifier
        assertFalse(Until(a, b).isCtlFormula());
        assertTrue(Forall(Until(a, b)).isCtlFormula());
        // no weak until or release
        assertFalse(Forall(WUntil(a, b)).isCtlFormula());
        assertFalse(Forall(Release(a, b)).isCtlFormula());
        assertFalse(Forall(SRelease(a, b)).isCtlFormula());
        assertTrue(Forall(Until(a, b)).isCtlFormula());
        // No isolated path quantifier
        assertFalse(Forall(a).isCtlFormula());
    }

    @Test
    public void testToCtlFormula() {
        Formula a = Prop("a");
        Formula b = Prop("b");
        Formula c = Prop("c");
        testToCtlFormula("a->b", Implies(a, b));
        testToCtlFormula("AX a", Forall(Next(a)));
        testToCtlFormula("E(true U a)", Exists(Eventually(a)));
        testToCtlFormula("!E(true U !a)", Forall(Always(a)));
    }

    private void testToCtlFormula(String expected, Formula f) {
        try {
            assertEquals(FormulaParser.instance().parse(expected), f.toCtlFormula());
        } catch (FormatException e) {
            fail(e.getMessage());
        }
    }
}
