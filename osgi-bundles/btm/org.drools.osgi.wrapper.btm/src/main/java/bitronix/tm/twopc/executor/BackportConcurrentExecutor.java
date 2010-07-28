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

/**
 * Abstraction of the <code>java.util.concurrent</code>
 * <a href="http://www.dcl.mathcs.emory.edu/util/backport-util-concurrent/">backport</a> implementation.
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public class BackportConcurrentExecutor extends ConcurrentExecutor {

    private final static String[] implementations = {
        "edu.emory.mathcs.backport.java.util.concurrent.Executors",
        "edu.emory.mathcs.backport.java.util.concurrent.ExecutorService",
        "edu.emory.mathcs.backport.java.util.concurrent.Future",
        "edu.emory.mathcs.backport.java.util.concurrent.TimeUnit"
    };

    public BackportConcurrentExecutor() {
        super(implementations);
    }
}
