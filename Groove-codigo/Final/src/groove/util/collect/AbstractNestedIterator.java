// GROOVE: GRaphs for Object Oriented VErification
// Copyright 2003--2007 University of Twente

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific
// language governing permissions and limitations under the License.
/*
 * $Id: AbstractNestedIterator.java 5479 2014-07-19 12:20:13Z rensink $
 */
package groove.util.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that is constructed from a top-level iteration that yields
 * elements which are themselves iterators. The top-level iteration works
 * through the abstract methods <tt>nextIterator()</tt> and
 * <tt>hasNextIterator()</tt>. The iterators returned by
 * <tt>nextIterator()</tt> are called <i>inner</i> iterators. The resulting
 * iterator supports removal of elements if the inner iterators do so.
 * @author Arend Rensink
 * @version $Revision: 5479 $
 */
public abstract class AbstractNestedIterator<T> implements Iterator<T> {
    @Override
    public void remove() {
        this.latestProductiveInnerIter.remove();
    }

    @Override
    public boolean hasNext() {
        while (!currentIterHasNext() && hasNextIterator()) {
            this.currentInnerIter = nextIterator();
        }
        return currentIterHasNext();
    }

    @Override
    public T next() {
        if (hasNext()) {
            this.latestProductiveInnerIter = this.currentInnerIter;
            return this.currentInnerIter.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the next (inner) iterator
     * @return the next (inner) iterator.
     * @ensure <tt>result != null</tt>
     * @throws NoSuchElementException if there is no next inner iterator (in
     *         which case {@link #hasNextIterator()} returns <tt>false</tt>.
     */
    protected abstract Iterator<? extends T> nextIterator();

    /**
     * Returns <tt>true</tt> if there is a next (inner) iterator. It
     * <tt>true</tt>, then {@link #nextIterator()} returns a valid result.
     * @return <tt>true</tt> if there is a next (inner) iterator
     */
    protected abstract boolean hasNextIterator();

    /**
     * Signals if {@link #currentInnerIter} has a next value.
     * @return <tt>currentInnerIter != null || currentInnerIter.hasNext()</tt>
     * 
     */
    private boolean currentIterHasNext() {
        return this.currentInnerIter != null && this.currentInnerIter.hasNext();
    }

    /**
     * The current inner iterator; i.e., the latest element returned by
     * <tt>nextIterator()</tt>
     */
    private Iterator<? extends T> currentInnerIter;
    /**
     * The inner iterator from which the latest call to <tt>next()</tt> has
     * obtained its value.
     */
    private Iterator<? extends T> latestProductiveInnerIter;
}