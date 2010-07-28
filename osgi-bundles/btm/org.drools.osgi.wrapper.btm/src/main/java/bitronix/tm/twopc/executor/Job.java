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

package bitronix.tm.twopc.executor;

import bitronix.tm.internal.XAResourceHolderState;

import javax.transaction.xa.XAException;

/**
 * Abstract job definition executable by the 2PC thread pools.
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public abstract class Job implements Runnable {
    private Object future;
    private XAResourceHolderState resourceHolder;

    protected XAException xaException;
    protected RuntimeException runtimeException;

    public Job(XAResourceHolderState resourceHolder) {
        this.resourceHolder = resourceHolder;
    }

    public XAResourceHolderState getResource() {
        return resourceHolder;
    }

    public XAException getXAException() {
        return xaException;
    }

    public RuntimeException getRuntimeException() {
        return runtimeException;
    }

    public void setFuture(Object future) {
        this.future = future;
    }

    public Object getFuture() {
        return future;
    }
}
