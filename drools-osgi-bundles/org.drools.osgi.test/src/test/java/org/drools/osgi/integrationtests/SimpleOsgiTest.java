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

package org.drools.osgi.integrationtests;

import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactoryService;
import org.drools.osgi.test.AbstractDroolsSpringDMTest;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.util.ServiceRegistry;
import org.junit.Ignore;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.test.Cheese;
import org.test.Person;
import org.test.decisiontable.Dummy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleOsgiTest extends AbstractDroolsSpringDMTest {

    protected void onSetUp() throws Exception {
        ServiceReference kbuilderRef = bundleContext.getServiceReference( KnowledgeBuilderFactoryService.class.getName() );
        Thread.currentThread().setContextClassLoader(  bundleContext.getService( kbuilderRef ).getClass().getClassLoader()  );
        System.out.println( "setup" + bundleContext.getService( kbuilderRef ).getClass().getClassLoader() );
    }

    protected void onTearDown() throws Exception {
        
    }

    @Test
    public void testOsgiPlatformStarts() throws Exception {
        System.out.println( bundleContext.getProperty( Constants.FRAMEWORK_VENDOR ) );
        System.out.println( bundleContext.getProperty( Constants.FRAMEWORK_VERSION ) );
        System.out.println( bundleContext.getProperty( Constants.FRAMEWORK_EXECUTIONENVIRONMENT ) );
    }

    @Test
    public void testOsgiEnvironment() throws Exception {
        Bundle[] bundles = bundleContext.getBundles();
        System.out.println( "bundles: " );
        for ( int i = 0; i < bundles.length; i++ ) {
            System.out.print( bundles[i].getSymbolicName() + ":" + bundles[i].getState() );
            System.out.print( ", " );
        }
        System.out.println();
    }

    @Test
    public void testCompiler() {
        ServiceReference serviceRef = bundleContext.getServiceReference( ServiceRegistry.class.getName() );
        ServiceRegistry registry = (ServiceRegistry) bundleContext.getService( serviceRef );

        KnowledgeBuilderFactoryService knowledgeBuilderFactoryService = registry.get( KnowledgeBuilderFactoryService.class );
        
        KnowledgeBaseFactoryService knowledgeBaseFactoryService = registry.get( KnowledgeBaseFactoryService.class );
        ResourceFactoryService resourceFactoryService = registry.get( ResourceFactoryService.class );

        String str = "";
        str += "package org.test\n";
        str += "import org.test.Person\n";
        str += "global java.util.List list\n";
        str += "rule rule1\n";
        str += "when\n";
        str += "    $p : Person( age > 30 )\n";
        str += "then\n";
        str += "    list.add($p);\n";
        str += "end\n";

        KnowledgeBuilderConfiguration kbConf = knowledgeBuilderFactoryService.newKnowledgeBuilderConfiguration( null,
                                                                                                                getClass().getClassLoader() );

        KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder( kbConf );
        ResourceFactoryService resource = resourceFactoryService;
        kbuilder.add( resource.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( kbuilder.getErrors() );
            throw new RuntimeException( kbuilder.getErrors().toString() );
        }

        KnowledgeBaseConfiguration kbaseConf = knowledgeBaseFactoryService.newKnowledgeBaseConfiguration( null,                                                                                                          
                                                                                                          getClass().getClassLoader() );

        KnowledgeBase kbase = knowledgeBaseFactoryService.newKnowledgeBase( kbaseConf );
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        List<?> list = new ArrayList<Object>();
        ksession.setGlobal( "list",
                            list );

        ksession.insert( new Person( "name",
                                     34 ) );
        
        ksession.fireAllRules();
        ksession.dispose();

        assertEquals( 1,
                      list.size() );
        assertEquals( new Person( "name",
                                  34 ),
                      list.get( 0 ) );

    }

    // TODO decisiontables disabled because poi-ooxml is not osgi enabled (and wrapping it would require wrapping a list of dependencies including xmlbeans too)
    // This known issue is fixed in 6.0
//    @Ignore() // Ignore does not work because Spring OSGi test framework ignores @Ignore.
//    @Test
//    public void testDecisionTable() {
//        ServiceReference serviceRef = bundleContext.getServiceReference( ServiceRegistry.class.getName() );
//        ServiceRegistry registry = (ServiceRegistry) bundleContext.getService( serviceRef );
//
//        KnowledgeBuilderFactoryService knowledgeBuilderFactoryService = registry.get( KnowledgeBuilderFactoryService.class );
//        KnowledgeBaseFactoryService knowledgeBaseFactoryService = registry.get( KnowledgeBaseFactoryService.class );
//        ResourceFactoryService resourceFactoryService = registry.get( ResourceFactoryService.class );
//
//        KnowledgeBaseConfiguration kbaseConf = knowledgeBaseFactoryService.newKnowledgeBaseConfiguration( null,
//                                                                                                          getClass().getClassLoader() );
//
//        System.out.println( "test dtables started" );
//
//        KnowledgeBuilderConfiguration kbConf = knowledgeBuilderFactoryService.newKnowledgeBuilderConfiguration( null,
//                                                                                                                getClass().getClassLoader() );
//        KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder( kbConf );
//        kbuilder.add( resourceFactoryService.newClassPathResource( "changeset1Test.xml",
//                                                                   Dummy.class ),
//                      ResourceType.CHANGE_SET );
//
//        kbaseConf = knowledgeBaseFactoryService.newKnowledgeBaseConfiguration( null,
//                                                                               getClass().getClassLoader() );
//        KnowledgeBase kbase = knowledgeBaseFactoryService.newKnowledgeBase( kbaseConf );
//        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
//
//        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
//        List<?> list = new ArrayList<Object>();
//        ksession.setGlobal( "list",
//                            list );
//
//        ksession.insert( new Cheese( "cheddar",
//                                     42 ) );
//        ksession.insert( new Person( "michael",
//                                     "stilton",
//                                     25 ) );
//
//        ksession.fireAllRules();
//        ksession.dispose();
//
//        assertEquals( 3,
//                      list.size() );
//
//        assertEquals( "Young man cheddar",
//                      list.get( 0 ) );
//
//        assertEquals( "rule1",
//                      list.get( 1 ) );
//        assertEquals( "rule2",
//                      list.get( 2 ) );
//
//        System.out.println( "test dtables ended" );
//    }

}
