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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAResource;
import java.util.List;
import java.util.Date;

/**
 * {@link XAResourceHolder} created by an {@link bitronix.tm.resource.common.XAResourceProducer} that is
 * used to perform recovery. Objects of this class cannot be used outside recovery scope.
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public class RecoveryXAResourceHolder extends AbstractXAResourceHolder {

    private final static Logger log = LoggerFactory.getLogger(RecoveryXAResourceHolder.class);

    private XAResourceHolder xaResourceHolder;

    public RecoveryXAResourceHolder(XAResourceHolder xaResourceHolder) {
        this.xaResourceHolder = xaResourceHolder;
    }

    public void close() throws Exception {
        if (log.isDebugEnabled()) log.debug("recovery xa resource is being closed: " + xaResourceHolder);
        xaResourceHolder.setState(STATE_IN_POOL);
    }

    public Date getLastReleaseDate() {
        return null;
    }

    public XAResource getXAResource() {
        return xaResourceHolder.getXAResource();
    }

    public List getXAResourceHolders() {
        return null;
    }

    public Object getConnectionHandle() throws Exception {
        throw new UnsupportedOperationException("illegal connection creation attempt out of " + this);
    }
}
