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
import bitronix.tm.utils.CollectionUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of all services required by a {@link XAResourceHolder}. This class keeps a list of all
 * {@link XAResourceHolderState}s of the {@link XAResourceHolder} plus the currently active one. There is
 * one per transaction in which this {@link XAResourceHolder} is enlisted plus all the suspended transactions in which
 * it is enlisted as well.
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public abstract class AbstractXAResourceHolder extends AbstractXAStatefulHolder implements XAResourceHolder {

    private final static Logger log = LoggerFactory.getLogger(AbstractXAResourceHolder.class);

    private final List xaResourceHolderStates = Collections.synchronizedList(new ArrayList());
    private XAResourceHolderState currentXaResourceHolderState;

    public XAResourceHolderState getXAResourceHolderState() {
        return currentXaResourceHolderState;
    }

    public void setXAResourceHolderState(XAResourceHolderState xaResourceHolderState) {
        synchronized (xaResourceHolderStates) {
            if (log.isDebugEnabled()) log.debug("setting default XAResourceHolderState [" + xaResourceHolderState + "] on " + this);
            if (xaResourceHolderState != null) {
                this.currentXaResourceHolderState = xaResourceHolderState;
                if (!CollectionUtils.containsByIdentity(xaResourceHolderStates, xaResourceHolderState)) {
                    if (log.isDebugEnabled()) log.debug("XAResourceHolderState previously unknown, adding it to the list");
                    this.xaResourceHolderStates.add(xaResourceHolderState);
                }
            }
            else {
                if (currentXaResourceHolderState != null) {
                    xaResourceHolderStates.remove(currentXaResourceHolderState);
                    this.currentXaResourceHolderState = null;
                }
                else
                    log.warn("currentXaResourceHolderState is already null! Bug ?");
            }
        }
    }

    public boolean removeXAResourceHolderState(XAResourceHolderState xaResourceHolderState) {
        boolean removed = xaResourceHolderStates.remove(xaResourceHolderState);
        if (removed && log.isDebugEnabled()) log.debug("removed " + xaResourceHolderState);
        return removed;
    }

    public boolean hasStateForXAResource(XAResourceHolder xaResourceHolder) {
        synchronized (xaResourceHolderStates) {
            for (int i = 0; i < xaResourceHolderStates.size(); i++) {
                XAResourceHolderState otherXaResourceHolderState = (XAResourceHolderState) xaResourceHolderStates.get(i);
                if (otherXaResourceHolderState.getXAResource() == xaResourceHolder.getXAResource()) {
                    if (log.isDebugEnabled()) log.debug("resource " + xaResourceHolder + " is enlisted in another transaction with " + otherXaResourceHolderState.getXid().toString());
                    return true;
                }
            }

            if (log.isDebugEnabled()) log.debug("resource not enlisted in any transaction: " + xaResourceHolder);
            return false;
        }
    }

    /**
     * If this method returns false, then local transaction calls like Connection.commit() can be made.
     * @return true if start() has been successfully called but not end() yet <i>and</i> the transaction is not suspended.
     */
    public boolean isParticipatingInActiveGlobalTransaction() {
        XAResourceHolderState xaResourceHolderState = getXAResourceHolderState();
        return xaResourceHolderState != null &&
                xaResourceHolderState.isStarted() &&
                !xaResourceHolderState.isSuspended() &&
                !xaResourceHolderState.isEnded();
    }

}
