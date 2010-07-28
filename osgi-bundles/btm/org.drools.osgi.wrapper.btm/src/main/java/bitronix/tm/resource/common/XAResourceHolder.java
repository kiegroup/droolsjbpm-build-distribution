/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bitronix.tm.resource.common;

import bitronix.tm.internal.XAResourceHolderState;

import javax.transaction.xa.XAResource;

/**
 * {@link XAResource} wrappers must implement this interface. It defines a way to get access to the transactional
 * state of this {@link XAResourceHolder}.
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @see XAResourceHolderState
 * @author lorban
 */
public interface XAResourceHolder extends XAStatefulHolder {

    /**
     * Get the vendor's {@link XAResource} implementation of the wrapped resource.
     * @return the vendor's XAResource implementation.
     */
    public XAResource getXAResource();

    /**
     * Get the {@link XAResourceHolderState} of this wrapped resource.
     * <p>Since a {@link XAResourceHolder} can participate in more than one transaction at a time (when suspending a
     * context for instance) the transaction manager guarantees that the {@link XAResourceHolderState} related to the
     * current transaction context will be returned.</p>
     * @return the {@link XAResourceHolderState}.
     */
    public XAResourceHolderState getXAResourceHolderState();

    /**
     * Set the {@link XAResourceHolderState} of this wrapped resource.
     * @param xaResourceHolderState the {@link XAResourceHolderState} to set.
     */
    public void setXAResourceHolderState(XAResourceHolderState xaResourceHolderState);


    /**
     * Remove the specified state from this wrapped resource.
     * @param xaResourceHolderState the {@link XAResourceHolderState} to remove.
     * @return true if the state actually existed before removal, false otherwise.
     */
    public boolean removeXAResourceHolderState(XAResourceHolderState xaResourceHolderState);

    /**
     * Check if this {@link XAResourceHolder} contains a state for a specific {@link XAResourceHolder}.
     * In other words: has the {@link XAResourceHolder}'s {@link XAResource} been enlisted in some transaction ?
     * @param xaResourceHolder the {@link XAResourceHolder} to look for.
     * @return true if the {@link XAResourceHolder} is enlisted in some transaction, false otherwise.
     */
    public boolean hasStateForXAResource(XAResourceHolder xaResourceHolder);

}
