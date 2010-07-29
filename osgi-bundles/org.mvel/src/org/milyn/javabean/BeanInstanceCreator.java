/*
	Milyn - Copyright (C) 2006

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.javabean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.SmooksException;
import org.milyn.util.CollectionsUtil;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.Parameter;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.Config;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.delivery.ordering.Producer;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.expression.MVELExpressionEvaluator;
import org.milyn.javabean.BeanRuntimeInfo.Classification;
import org.milyn.javabean.repository.BeanId;
import org.milyn.javabean.repository.BeanIdRegister;
import org.milyn.javabean.repository.BeanRepositoryManager;
import org.milyn.javabean.repository.BeanRepository;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bean instance creator visitor class.
 * <p/>
 * Targeted via {@link org.milyn.javabean.BeanPopulator} expansion configuration.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@VisitBeforeReport(summary = "Created <b>${resource.parameters.beanId!'undefined'}</b> bean instance.  Associated lifecycle if wired to another bean.",
        detailTemplate = "reporting/BeanInstanceCreatorReport_Before.html")
@VisitAfterReport(condition = "parameters.containsKey('setOn') || parameters.beanClass.value.endsWith('[]')",
        summary = "Ended bean lifecycle. Set bean on any targets.",
        detailTemplate = "reporting/BeanInstanceCreatorReport_After.html")
public class BeanInstanceCreator implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter, Producer {

    private static Log logger = LogFactory.getLog(BeanInstanceCreator.class);

	public static final String INIT_VAL_EXPRESSION = "initValExpression";

    private String id;

    @ConfigParam(name="beanId")
    private String beanIdName;

    @ConfigParam(name="beanClass")
    private String beanClassName;
    
    @Config
    private SmooksResourceConfiguration config;

    @AppContext
    private ApplicationContext appContext;

    private BeanRuntimeInfo beanRuntimeInfo;

    private BeanRepositoryManager beanRepositoryManager;

    private BeanId beanId;

    private MVELExpressionEvaluator initValsExpression;
    
    /**
     * Public default constructor.
     */
    public BeanInstanceCreator() {
    }

    /**
     * Public default constructor.
     * @param beanId The beanId under which the bean instance is registered in the bean context.
     * @param beanClass The bean runtime class.
     */
    public BeanInstanceCreator(String beanId, Class beanClass) {
        AssertArgument.isNotNull(beanId, "beanId");
        AssertArgument.isNotNull(beanClass, "beanClass");
        this.beanIdName = beanId;

        if(!beanClass.isArray()){
            this.beanClassName = beanClass.getName();
        } else {
            this.beanClassName = beanClass.getComponentType().getName() + "[]";
        }
    }

    /**
     * Get the beanId of this Bean configuration.
     *
     * @return The beanId of this Bean configuration.
     */
    public String getBeanId() {
        return beanIdName;
    }

    /**
     * Set the resource configuration on the bean populator.
     * @throws SmooksConfigurationException Incorrectly configured resource.
     */
    @Initialize
    public void initialize() throws SmooksConfigurationException {
    	buildId();

    	beanRepositoryManager = BeanRepositoryManager.getInstance(appContext);
    	BeanIdRegister beanIdRegister = beanRepositoryManager.getBeanIdRegister();
        beanId = beanIdRegister.register(beanIdName);

    	beanRuntimeInfo = BeanRuntimeInfo.getBeanRuntimeInfo(beanIdName, beanClassName, appContext);

        if(logger.isDebugEnabled()) {
        	logger.debug("BeanInstanceCreator created for [" + beanIdName + "]. BeanRuntimeInfo: " + beanRuntimeInfo);
        }
        
        List<Parameter> initValExpressions = config.getParameters(INIT_VAL_EXPRESSION);
        if(initValExpressions != null && !initValExpressions.isEmpty()) {
        	StringBuilder initValsExpressionString = new StringBuilder();
        	
        	for(Parameter initValExpression : initValExpressions) {
        		initValsExpressionString.append(initValExpression.getValue());
        		initValsExpressionString.append("\n");
        	}
        	
        	initValsExpression = new MVELExpressionEvaluator();
        	initValsExpression.setExpression(initValsExpressionString.toString());
        }
    }

    /**
     * Get the bean runtime information.
     * @return The bean runtime information.
     */
    public BeanRuntimeInfo getBeanRuntimeInfo() {
        return beanRuntimeInfo;
    }

    private void buildId() {
    	StringBuilder idBuilder = new StringBuilder();
    	idBuilder.append(BeanInstanceCreator.class.getName());
    	idBuilder.append("#");
    	idBuilder.append(beanIdName);


    	id = idBuilder.toString();
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        createAndSetBean(executionContext);
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        createAndSetBean(executionContext);
    }


	/* (non-Javadoc)
	 * @see org.milyn.delivery.dom.DOMVisitAfter#visitAfter(org.w3c.dom.Element, org.milyn.container.ExecutionContext)
	 */
	public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
		visitAfter(executionContext);
	}

	/* (non-Javadoc)
	 * @see org.milyn.delivery.sax.SAXVisitAfter#visitAfter(org.milyn.delivery.sax.SAXElement, org.milyn.container.ExecutionContext)
	 */
	public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
		visitAfter(executionContext);
	}

	public void visitAfter(ExecutionContext executionContext) {
        BeanRepository beanRepo = BeanRepositoryManager.getBeanRepository(executionContext);
        Classification thisBeanType = beanRuntimeInfo.getClassification();
        boolean isBeanTypeArray = (thisBeanType == Classification.ARRAY_COLLECTION);

        beanRepo.setBeanInContext(beanId, false);

        if(isBeanTypeArray) {
            Object bean  = beanRepo.getBean(beanId);

            if(logger.isDebugEnabled()) {
                logger.debug("Converting bean [" + beanIdName + "] to an array and rebinding to context.");
            }
            bean = convert(executionContext, bean);
        }
	}


    private Object convert(ExecutionContext executionContext, Object bean) {

        bean = BeanUtils.convertListToArray((List<?>)bean, beanRuntimeInfo.getArrayType());

        BeanRepositoryManager.getBeanRepository(executionContext).changeBean(beanId, bean);

    	return bean;
    }

	private void createAndSetBean(ExecutionContext executionContext) {
        Object bean;
        BeanRepository beanRepo = BeanRepositoryManager.getBeanRepository(executionContext);

        bean = createBeanInstance();
        
        if(initValsExpression != null) {
        	initValsExpression.exec(bean);
        }
        
        beanRepo.setBeanInContext(beanId, false);
        beanRepo.addBean(beanId, bean);
        beanRepo.setBeanInContext(beanId, true);

        if (logger.isDebugEnabled()) {
            logger.debug("Bean [" + beanIdName + "] instance created.");
        }
    }

    /**
     * Create a new bean instance, generating relevant configuration exceptions.
     *
     * @return A new bean instance.
     */
    private Object createBeanInstance() {
        Object bean;

        try {
            bean = beanRuntimeInfo.getPopulateType().newInstance();
        } catch (InstantiationException e) {
            throw new SmooksConfigurationException("Unable to create bean instance [" + beanIdName + ":" + beanRuntimeInfo.getPopulateType().getName() + "].", e);
        } catch (IllegalAccessException e) {
            throw new SmooksConfigurationException("Unable to create bean instance [" + beanIdName + ":" + beanRuntimeInfo.getPopulateType().getName() + "].", e);
        }

        return bean;
    }

    public Set<? extends Object> getProducts() {
        return CollectionsUtil.toSet(beanIdName);
    }

    private String getId() {
		return id;
	}

    @Override
    public String toString() {
    	return getId();
    }
}
