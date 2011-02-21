/*
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

package org.drools.osgi.test.utils;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.osgi.test.provisioning.ArtifactLocator;

/**
 * Locator for artifacts found in the local Eclipse plugins workspace and
 * target. Does <strong>not</strong> use Eclipse PDE, it rather
 * uses the Eclipse patterns and conventions to identify the artifacts.
 * 
 * It gives priority to workspace bundle over target bundles.
 * 
 * @author Frederic Conrotte
 * 
 */
public class EclipseWorkspaceArtifactLocator implements ArtifactLocator {

    private static final Logger log = Logger
            .getLogger(EclipseWorkspaceArtifactLocator.class);
    
    private EclipseArtifactFinder m_ArtifactFinder = new EclipseArtifactFinder();

    /**
     * Find an artifact in the list of bundles from current Eclipse Workspace.
     * 
     * @param groupId
     *            - not used
     * @param artifactId
     *            - the artifact id of the bundle (required)
     * @param version
     *            - the version of the bundle (can be null)
     * @return the String representing the URL location of this bundle
     */
    public Resource locateArtifact(String groupId, String artifactId,
            String version) {
        return locateArtifact(groupId, artifactId, version,
                DEFAULT_ARTIFACT_TYPE);
    }

    public Resource locateArtifact(String groupId, String artifactId,
            String version, String type) {
        return localEclipseWorkspaceArtifact(artifactId, version);
    }

    /**
     * Locate an artifact in an Eclipse Workspace
     * 
     * @param artifactId
     *            - the artifact id of the bundle (required)
     * @param version
     *            - the version of the bundle (can be null)
     * @return Resource corresponding to the located Eclipse bundle
     */
    private Resource localEclipseWorkspaceArtifact(String aArtifactId,
            String aVersion) {
        try {
            Resource res = m_ArtifactFinder.findArtifact(aArtifactId, aVersion);
            if (res != null && log.isDebugEnabled()) {
                log.debug("[" + aArtifactId + "|" + aVersion + "] resolved to "
                        + res.getDescription() + " as a Eclipse artifact");
            }
            return res;
        } catch (IOException ioEx) {
            throw (RuntimeException) new IllegalStateException("Artifact "
                    + aArtifactId + "-" + aVersion + " could not be found")
                    .initCause(ioEx);
        }

    }

}
