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

package bitronix.tm.internal;

/**
 * <p>Simple crypto helper that uses symetric keys to crypt and decrypt resources passwords.</p>
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @deprecated superceded by {@link bitronix.tm.utils.CryptoEngine}.
 * @author lorban
 */
public class CryptoEngine {

    public static void main(String[] args) throws Exception {
        System.out.println("WARNING: bitronix.tm.internal.CryptoEngine has been replaced by bitronix.tm.utils.CryptoEngine.");
        bitronix.tm.utils.CryptoEngine.main(args);
    }

}
