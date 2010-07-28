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

import javax.transaction.Synchronization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import bitronix.tm.BitronixTransaction;

/**
 * {@link Synchronization} used to release a {@link XAStatefulHolder} object after 2PC has executed.
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public class DeferredReleaseSynchronization implements Synchronization {

    private final static Logger log = LoggerFactory.getLogger(DeferredReleaseSynchronization.class);

    private XAStatefulHolder xaStatefulHolder;

    public DeferredReleaseSynchronization(XAStatefulHolder xaStatefulHolder) {
        this.xaStatefulHolder = xaStatefulHolder;
    }

    public XAStatefulHolder getXAStatefulHolder() {
        return xaStatefulHolder;
    }

    public void afterCompletion(int status) {
        if (log.isDebugEnabled()) log.debug("DeferredReleaseSynchronization requeuing " + xaStatefulHolder);

        // set this connection's state back to IN_POOL
        xaStatefulHolder.setState(XAResourceHolder.STATE_IN_POOL);

        if (log.isDebugEnabled()) log.debug("DeferredReleaseSynchronization requeued " + xaStatefulHolder);
    }

    public void beforeCompletion() {
    }

    public String toString() {
        return "a DeferredReleaseSynchronization of " + xaStatefulHolder;
    }
}