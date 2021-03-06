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
 * $Id: TestDeleteUseWithAttributes.java 5480 2014-07-19 22:15:15Z rensink $
 */
package groove.test.criticalpair;

import static org.junit.Assert.assertTrue;
import groove.grammar.Rule;
import groove.grammar.model.GrammarModel;
import groove.transform.criticalpair.CriticalPair;
import groove.util.parse.FormatException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;

/**
 * @author Ruud Welling
 */
@SuppressWarnings("javadoc")
public class TestDeleteUseWithAttributes {
    @Test
    public void testAttributes() {
        String grammar = "junit/criticalpair/attributes.gps/";
        File grammarFile = new File(grammar);
        GrammarModel view = null;
        try {
            view = GrammarModel.newInstance(grammarFile, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Rule addOneToNumber = getSimpleRule("addOneToNumber", view);
        Rule addOneToNumber_2 = getSimpleRule("addOneToNumber_2", view);
        Rule deleteNumber = getSimpleRule("deleteNumber", view);
        Rule deleteNumberOne = getSimpleRule("deleteNumberOne", view);

        Set<CriticalPair> pairs =
            CriticalPair.computeCriticalPairs(addOneToNumber, addOneToNumber_2);
        assertTrue(pairs.size() == 1);
        pairs = CriticalPair.computeCriticalPairs(addOneToNumber, deleteNumber);
        assertTrue(pairs.size() == 1);
        pairs = CriticalPair.computeCriticalPairs(addOneToNumber, deleteNumberOne);
        assertTrue(pairs.size() == 1);

        pairs = CriticalPair.computeCriticalPairs(addOneToNumber_2, deleteNumber);
        assertTrue(pairs.size() == 1);
        pairs = CriticalPair.computeCriticalPairs(addOneToNumber_2, deleteNumberOne);
        assertTrue(pairs.size() == 1);
        pairs = CriticalPair.computeCriticalPairs(deleteNumber, deleteNumberOne);
        assertTrue(pairs.size() == 1);

    }

    @Test
    public void testAttributes_SameRule() {
        String grammar = "junit/criticalpair/attributes.gps/";
        File grammarFile = new File(grammar);
        GrammarModel view = null;
        try {
            view = GrammarModel.newInstance(grammarFile, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Rule addOneToNumber = getSimpleRule("addOneToNumber", view);
        Rule addOneToNumber_2 = getSimpleRule("addOneToNumber_2", view);
        Rule deleteNumber = getSimpleRule("deleteNumber", view);
        Rule deleteNumberOne = getSimpleRule("deleteNumberOne", view);

        Set<CriticalPair> pairs = CriticalPair.computeCriticalPairs(addOneToNumber, addOneToNumber);
        //Since constants and targets of operations are not included in the search for critical pairs
        //The only critical pair which we will find has the same match for both rules
        //therefore this critical pair is not considered a critical pair
        assertTrue(pairs.isEmpty());
        pairs = CriticalPair.computeCriticalPairs(addOneToNumber_2, addOneToNumber_2);
        //Similar to the previous
        assertTrue(pairs.isEmpty());
        pairs = CriticalPair.computeCriticalPairs(deleteNumber, deleteNumber);
        //There is one way to overlap the rule deleteNumber with itself such that
        //a conflicting parallel pair is formed, however, in this case the matches are equal
        //therefore the conflict is not a critical pair
        assertTrue(pairs.isEmpty());
        //Again, similarly to the previous same rules with the same match is not a critical pair
        //therefore there are no critical pairs for the following rule
        pairs = CriticalPair.computeCriticalPairs(deleteNumberOne, deleteNumberOne);
        assertTrue(pairs.isEmpty());

    }

    //    @Test
    //    public void testAttributes_Point() {
    //        String grammar = "junit/criticalpair/attributes.gps/";
    //        File grammarFile = new File(grammar);
    //        GrammarModel view = null;
    //        try {
    //            view = GrammarModel.newInstance(grammarFile, false);
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //        view.getProperties().setAlgebraFamily(AlgebraFamily.POINT);
    //        Rule deleteNumberTwo = getSimpleRule("deleteNumberTwo", view);
    //        Rule deleteNumberOne = getSimpleRule("deleteNumberOne", view);
    //        //Both left-hand
    //        Set<CriticalPair> pairs =
    //            CriticalPair.computeCriticalPairs(deleteNumberOne, deleteNumberTwo);
    //        assertTrue(pairs.size() == 1);
    //
    //    }

    private Rule getSimpleRule(String name, GrammarModel view) {
        Rule result = null;
        try {
            result = view.getRuleModel(name).toResource();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return result;
    }

}
