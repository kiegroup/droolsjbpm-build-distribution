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

package bitronix.tm.twopc;

import bitronix.tm.internal.XAResourceHolderState;
import bitronix.tm.utils.Decoder;

import javax.transaction.xa.XAException;
import java.util.List;
import java.util.ArrayList;

/**
 * Thrown when a phase exection has thrown one or more exception(s).  
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public class PhaseException extends Exception {

    private List exceptions = new ArrayList();
    private List resources = new ArrayList();

    public PhaseException(List exceptions, List resources) {
        this.exceptions = exceptions;
        this.resources = resources;
    }

    public String getMessage() {
        StringBuffer errorMessage = new StringBuffer();
        errorMessage.append("collected ");
        errorMessage.append(exceptions.size());
        errorMessage.append(" exception(s):");
        for (int i = 0; i < exceptions.size(); i++) {
            errorMessage.append(System.getProperty("line.separator"));
            Throwable throwable = (Throwable) exceptions.get(i);
            String message = throwable.getMessage();
            XAResourceHolderState holderState = (XAResourceHolderState) resources.get(i);

            if (holderState != null) {
                errorMessage.append(" [");
                errorMessage.append(holderState.getUniqueName());
                errorMessage.append(" - ");
            }
            errorMessage.append(throwable.getClass().getName());
            if (throwable instanceof XAException) {
                XAException xaEx = (XAException) throwable;
                errorMessage.append("(");
                errorMessage.append(Decoder.decodeXAExceptionErrorCode(xaEx));
                errorMessage.append(")");
            }
            errorMessage.append(" - ");
            errorMessage.append(message);
            errorMessage.append("]");
        }

        return errorMessage.toString();
    }

    /**
     * Get the list of exceptions that have been thrown during a phase execution.
     * @return the list of exceptions that have been thrown during a phase execution.
     */
    public List getExceptions() {
        return exceptions;
    }

    /**
     * Get the list of resource which threw an exception during a phase execution.
     * This list always contains exactly one resource per exception present in {@link #getExceptions} list.
     * Indices of both list always match a resource against the exception it threw.
     * @return the list of resource which threw an exception during a phase execution.
     */
    public List getResources() {
        return resources;
    }
}
