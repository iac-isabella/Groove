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
 * $Id: MatcherFactory.java 5666 2015-02-01 16:42:17Z zambon $
 */
package groove.match;

import groove.grammar.Condition;
import groove.grammar.rule.Anchor;
import groove.match.plan.PlanSearchEngine;

/**
 * A factory for matchers.
 * The factory keeps an inner {@link SearchEngine} factory for search strategies;
 * updating this inner factory will cause the match strategies to
 * refresh themselves to the corresponding new search strategy.
 * Currently this is a singleton class, but it would be quite possible to
 * have multiple instances, at the cost of linking a shared instance to
 * all the objects that may do matching.
 * @author Arend Rensink
 * @version $Revision $
 */
public class MatcherFactory {
    private MatcherFactory() {
        this.engine = defaultEngine;
        this.oracle = DefaultValueOracle.instance();
    }

    /**
     * Changes the search engine set in this factory.
     * This will cause all matchers created by this factory to refresh
     * their inner search strategies.
     */
    public void setEngine(SearchEngine engine) {
        this.engine = engine;
    }

    /** Returns the installed value oracle.
     * @return the value oracle; non-{@code null}
     */
    public ValueOracle getOracle() {
        return this.oracle;
    }

    /** Sets a new value oracle.
     * @param oracle the new oracle; non-{@code null}
     */
    public void setOracle(ValueOracle oracle) {
        assert oracle != null;
        this.oracle = oracle;
    }

    /** Sets the search engine to the default. */
    public void setDefaultEngine() {
        setEngine(defaultEngine);
    }

    /** Returns the currently set search engine. */
    public SearchEngine getEngine() {
        return this.engine;
    }

    /** Creates a matcher for a given condition and default seeds. */
    public Matcher createMatcher(Condition condition) {
        return createMatcher(condition, null);
    }

    /** Creates a matcher for a given condition and explicitly specified seeds. */
    public Matcher createMatcher(Condition condition, Anchor seed) {
        return new Matcher(this, condition, seed);
    }

    /** The currently set search engine. */
    private SearchEngine engine;

    /** Oracle for matching value nodes. */
    private ValueOracle oracle;

    /** Returns the singleton instance of the factory. */
    public static MatcherFactory instance() {
        if (instance == null) {
            instance = new MatcherFactory();
        }
        return instance;
    }

    private static MatcherFactory instance;
    private static SearchEngine defaultEngine = PlanSearchEngine.getInstance();
}
