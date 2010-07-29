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
package org.milyn.delivery.sax;

import org.milyn.container.ExecutionContext;
import org.milyn.delivery.*;
import org.milyn.delivery.ordering.Sorter;
import org.milyn.cdr.SmooksConfigurationException;

import java.util.*;

/**
 * SAX specific {@link org.milyn.delivery.ContentDeliveryConfig} implementation.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXContentDeliveryConfig extends AbstractContentDeliveryConfig {
    
    private ContentHandlerConfigMapTable<SAXVisitBefore> visitBefores;
    private ContentHandlerConfigMapTable<SAXVisitChildren> childVisitors = new ContentHandlerConfigMapTable<SAXVisitChildren>();
    private ContentHandlerConfigMapTable<SAXVisitAfter> visitAfters;
    private ContentHandlerConfigMapTable<VisitLifecycleCleanable> visitCleanables;
    private ContentHandlerConfigMapTable<ExecutionLifecycleCleanable> execCleanables;
    private FilterBypass filterBypass;

    private Map<String, SAXElementVisitorMap> optimizedVisitorConfig = new HashMap<String, SAXElementVisitorMap>();

    public ContentHandlerConfigMapTable<SAXVisitBefore> getVisitBefores() {
        return visitBefores;
    }

    public void setVisitBefores(ContentHandlerConfigMapTable<SAXVisitBefore> visitBefores) {
        this.visitBefores = visitBefores;
    }

    public ContentHandlerConfigMapTable<SAXVisitChildren> getChildVisitors() {
        return childVisitors;
    }

    public ContentHandlerConfigMapTable<SAXVisitAfter> getVisitAfters() {
        return visitAfters;
    }

    public void setVisitAfters(ContentHandlerConfigMapTable<SAXVisitAfter> visitAfters) {
        this.visitAfters = visitAfters;
    }

    public ContentHandlerConfigMapTable<VisitLifecycleCleanable> getVisitCleanables() {
        return visitCleanables;
    }

    public void setVisitCleanables(ContentHandlerConfigMapTable<VisitLifecycleCleanable> visitCleanables) {
        this.visitCleanables = visitCleanables;
    }

    public ContentHandlerConfigMapTable<ExecutionLifecycleCleanable> getExecCleanables() {
        return execCleanables;
    }

    public void setExecCleanables(ContentHandlerConfigMapTable<ExecutionLifecycleCleanable> execCleanables) {
        this.execCleanables = execCleanables;
    }

    public Map<String, SAXElementVisitorMap> getOptimizedVisitorConfig() {
        return optimizedVisitorConfig;
    }
    
    public FilterBypass getFilterBypass() {
    	return filterBypass;
    }

    public Filter newFilter(ExecutionContext executionContext) {
        return new SmooksSAXFilter(executionContext);
    }

    public void sort() throws SmooksConfigurationException {
        visitBefores.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        childVisitors.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        visitAfters.sort(Sorter.SortOrder.CONSUMERS_FIRST);
    }

    public void optimizeConfig() {
        if(visitBefores == null || visitAfters == null) {
            throw new IllegalStateException("Illegal call to setChildVisitors() before setVisitBefores() and setVisitAfters() are called.");
        }

        extractChildVisitors();

        // Now extract the before, child and after visitors for all configured elements...
        Set<String> elementNames = new HashSet();
        elementNames.addAll(visitBefores.getTable().keySet());
        elementNames.addAll(visitAfters.getTable().keySet());

        for (String elementName : elementNames) {
            SAXElementVisitorMap entry = new SAXElementVisitorMap();

            entry.setVisitBefores(visitBefores.getTable().get(elementName));
            entry.setChildVisitors(childVisitors.getTable().get(elementName));
            entry.setVisitAfters(visitAfters.getTable().get(elementName));
            entry.setVisitCleanables(visitCleanables.getTable().get(elementName));
            optimizedVisitorConfig.put(elementName, entry);
        }
        
		filterBypass = getFilterBypass(visitBefores, visitAfters);
    }

	public SAXElementVisitorMap getCombinedOptimizedConfig(String[] elementNames) {
        SAXElementVisitorMap combinedConfig = new SAXElementVisitorMap();

        combinedConfig.setVisitBefores(new ArrayList<ContentHandlerConfigMap<SAXVisitBefore>>());
        combinedConfig.setChildVisitors(new ArrayList<ContentHandlerConfigMap<SAXVisitChildren>>());
        combinedConfig.setVisitAfters(new ArrayList<ContentHandlerConfigMap<SAXVisitAfter>>());
        combinedConfig.setVisitCleanables(new ArrayList<ContentHandlerConfigMap<VisitLifecycleCleanable>>());

        for(String elementName : elementNames) {
            SAXElementVisitorMap elementConfig = optimizedVisitorConfig.get(elementName.toLowerCase());

            if(elementConfig != null) {
                List<ContentHandlerConfigMap<SAXVisitBefore>> elementVisitBefores = elementConfig.getVisitBefores();
                List<ContentHandlerConfigMap<SAXVisitChildren>> elementChildVisitors = elementConfig.getChildVisitors();
                List<ContentHandlerConfigMap<SAXVisitAfter>> elementVisitAfteres = elementConfig.getVisitAfters();
                List<ContentHandlerConfigMap<VisitLifecycleCleanable>> elementVisitCleanables = elementConfig.getVisitCleanables();

                if(elementVisitBefores != null) {
                    combinedConfig.getVisitBefores().addAll(elementVisitBefores);
                }
                if(elementChildVisitors != null) {
                    combinedConfig.getChildVisitors().addAll(elementChildVisitors);
                }
                if(elementVisitAfteres != null) {
                    combinedConfig.getVisitAfters().addAll(elementVisitAfteres);
                }
                if(elementVisitCleanables != null) {
                    combinedConfig.getVisitCleanables().addAll(elementVisitCleanables);
                }
            }
        }

        if(combinedConfig.getVisitBefores().isEmpty()) {
            combinedConfig.setVisitBefores(null);
        }
        if(combinedConfig.getChildVisitors().isEmpty()) {
            combinedConfig.setChildVisitors(null);
        }
        if(combinedConfig.getVisitAfters().isEmpty()) {
            combinedConfig.setVisitAfters(null);
        }
        if(combinedConfig.getVisitCleanables().isEmpty()) {
            combinedConfig.setVisitCleanables(null);
        }

        if(combinedConfig.getVisitBefores() == null && combinedConfig.getChildVisitors() == null && combinedConfig.getVisitAfters() == null ) {
            return null;
        } else {
            return combinedConfig;
        }
    }

    private void extractChildVisitors() {
        // Need to extract the child visitor impls from the visitBefores and the visitAfters.  Need to make sure that we don't add
        // the same handler twice - handlers can impl both SAXVisitBefore and SAXVisitAfter. So, we don't add child handlers from the
        // visitBefores if they also impl SAXVisitAfter (avoiding adding where it impls both).  We add from the visitafters list
        // if it impls SAXVisitAfter without checking for SAXVisitBefore (catching the case where it impls both).

        Set<Map.Entry<String, List<ContentHandlerConfigMap<SAXVisitBefore>>>> beforeMappings = visitBefores.getTable().entrySet();
        for (Map.Entry<String, List<ContentHandlerConfigMap<SAXVisitBefore>>> beforeMapping : beforeMappings) {
            List<ContentHandlerConfigMap<SAXVisitBefore>> elementMappings = beforeMapping.getValue();
            for (ContentHandlerConfigMap<SAXVisitBefore> elementMapping : elementMappings) {
                String elementName = beforeMapping.getKey();
                SAXVisitBefore handler = elementMapping.getContentHandler();

                // Wanna make sure we don't add the same handler twice, so if it also impls SAXVisitAfter, leave
                // that until we process the SAXVisitAfter handlers...
                if(handler instanceof SAXVisitChildren && !(handler instanceof SAXVisitAfter)) {
                    childVisitors.addMapping(elementName, elementMapping.getResourceConfig(), (SAXVisitChildren) handler);
                }
            }
        }

        Set<Map.Entry<String, List<ContentHandlerConfigMap<SAXVisitAfter>>>> afterMappings = visitAfters.getTable().entrySet();
        for (Map.Entry<String,List<ContentHandlerConfigMap<SAXVisitAfter>>> afterMapping : afterMappings) {
            List<ContentHandlerConfigMap<SAXVisitAfter>> elementMappings = afterMapping.getValue();
            for (ContentHandlerConfigMap<SAXVisitAfter> elementMapping : elementMappings) {
                String elementName = afterMapping.getKey();
                SAXVisitAfter handler = elementMapping.getContentHandler();

                if(handler instanceof SAXVisitChildren) {
                    childVisitors.addMapping(elementName, elementMapping.getResourceConfig(), (SAXVisitChildren) handler);
                }
            }
        }
    }
}