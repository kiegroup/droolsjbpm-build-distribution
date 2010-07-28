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

import bitronix.tm.internal.BitronixRuntimeException;

/**
 * This implementation spawns a new thread per request.
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public class SimpleAsyncExecutor implements Executor {

    public Object submit(Job job) {
        Thread t = new Thread(job);
        t.setDaemon(true);
        t.start();
        return t;
    }

    public void waitFor(Object future, long timeout) {
        Thread t = (Thread) future;
        try {
            t.join(timeout);
        } catch (InterruptedException ex) {
            throw new BitronixRuntimeException("job interrupted", ex);
        }
    }

    public boolean isDone(Object future) {
        Thread t = (Thread) future;
        return !t.isAlive();
    }

    public boolean isUsable() {
        return true;
    }

    public void shutdown() {
    }
}
