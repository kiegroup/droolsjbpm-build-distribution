package org.drools.osgi.integrationtests;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.bpmn2.SimpleBPMNProcessTest;
import org.drools.bpmn2.xml.BPMN2SemanticModule;
import org.drools.bpmn2.xml.BPMNDISemanticModule;
import org.drools.bpmn2.xml.BPMNSemanticModule;
import org.drools.bpmn2.xml.XmlBPMNProcessDumper;
import org.drools.bpmn2.xpath.XPathDialectConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.io.ResourceFactory;
import org.drools.io.ResourceFactoryService;
import org.drools.osgi.test.AbstractDroolsSpringDMTest;
import org.drools.ruleflow.core.RuleFlowProcess;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.util.ServiceRegistry;
import org.drools.xml.XmlProcessReader;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.util.OsgiStringUtils;
import org.test.Cheese;
import org.test.Person;
import org.test.decisiontable.Dummy;

public class BPMN2OsgiTest extends AbstractDroolsSpringDMTest {

    protected void onSetUp() throws Exception {
        super.onSetUp();
    }

    protected void onTearDown() throws Exception {
        super.onTearDown();
    }

	public void testMinimalProcess() throws Exception {		
		KnowledgeBase kbase = createKnowledgeBase("BPMN2-MinimalProcess.xml");
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		ProcessInstance processInstance = ksession.startProcess("Minimal");
		assertTrue(processInstance.getState() == ProcessInstance.STATE_COMPLETED);
	}
	
	private KnowledgeBase createKnowledgeBase(String process) throws Exception {
		
        ServiceReference serviceRef = bundleContext.getServiceReference( ServiceRegistry.class.getName() );
        ServiceRegistry registry = (ServiceRegistry) bundleContext.getService( serviceRef );

        KnowledgeBuilderFactoryService knowledgeBuilderFactoryService = registry.get( KnowledgeBuilderFactoryService.class );
        KnowledgeBaseFactoryService knowledgeBaseFactoryService = registry.get( KnowledgeBaseFactoryService.class );
        ResourceFactoryService resourceFactoryService = registry.get( ResourceFactoryService.class );
        
		KnowledgeBuilderConfiguration conf = knowledgeBuilderFactoryService.newKnowledgeBuilderConfiguration();
		((PackageBuilderConfiguration) conf).initSemanticModules();
        ((PackageBuilderConfiguration) conf).addSemanticModule(new BPMNSemanticModule());
		((PackageBuilderConfiguration) conf).addSemanticModule(new BPMN2SemanticModule());
        ((PackageBuilderConfiguration) conf).addSemanticModule(new BPMNDISemanticModule());
        ((PackageBuilderConfiguration) conf).addDialect("XPath", new XPathDialectConfiguration());        
        
		XmlProcessReader processReader = new XmlProcessReader(
	        ((PackageBuilderConfiguration) conf).getSemanticModules());
		RuleFlowProcess p = (RuleFlowProcess)
		    processReader.read(BPMN2OsgiTest.class.getResourceAsStream(process));				
		
		KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder(conf);
		
		kbuilder.add(resourceFactoryService.newReaderResource(
            new StringReader(XmlBPMNProcessDumper.INSTANCE.dump(p))), ResourceType.DRF);
		if (!kbuilder.getErrors().isEmpty()) {
			for (KnowledgeBuilderError error: kbuilder.getErrors()) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Errors while parsing knowledge base");
		}
		KnowledgeBase kbase = knowledgeBaseFactoryService.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		return kbase;
	}	

}