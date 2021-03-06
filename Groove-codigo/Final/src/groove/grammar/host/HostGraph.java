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
 * $Id: HostGraph.java 5480 2014-07-19 22:15:15Z rensink $
 */
package groove.grammar.host;

import groove.algebra.AlgebraFamily;
import groove.grammar.type.TypeGraph;
import groove.graph.GGraph;
import groove.graph.GraphInfo;
import groove.transform.DeltaTarget;
import groove.util.parse.FormatErrorSet;
import groove.util.parse.FormatException;

/**
 * Graph type used for graphs under transformation.
 * Host graphs consist of {@link HostNode}s and {@link HostEdge}s.
 * @author Arend Rensink
 * @version $Revision $
 */
public interface HostGraph extends GGraph<HostNode,HostEdge>, DeltaTarget {
    @Override
    HostGraph newGraph(String name);

    @Override
    HostGraph clone();

    /** Clones this host graph, while optionally changing the algebras. */
    HostGraph clone(AlgebraFamily family);

    @Override
    HostFactory getFactory();

    /** Returns the type graph for this host graph, if any. */
    public TypeGraph getTypeGraph();

    /**
     * Returns a copy of this graph, typed against a given type graph.
     * @throws FormatException if there are typing errors in the graph
     */
    public HostGraph retype(TypeGraph typeGraph) throws FormatException;

    /**
     * Checks the graph for type constraints that cannot be
     * prevented statically: in particular, multiplicity and containment
     * violations. Any errors found are collected and returned.
     * @see GraphInfo#getErrors
     */
    public FormatErrorSet checkTypeConstraints();
}
