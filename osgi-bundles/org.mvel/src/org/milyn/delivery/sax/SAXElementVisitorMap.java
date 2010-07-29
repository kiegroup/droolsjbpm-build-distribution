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

import org.milyn.delivery.ContentHandlerConfigMap;
import org.milyn.delivery.VisitLifecycleCleanable;

import java.util.ArrayList;
import java.util.List;

/**
 * SAXElement visitor Map.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXElementVisitorMap {

    private List<ContentHandlerConfigMap<SAXVisitBefore>> visitBefores;
    private List<ContentHandlerConfigMap<SAXVisitChildren>> childVisitors;
    private List<ContentHandlerConfigMap<SAXVisitAfter>> visitAfters;
    private List<ContentHandlerConfigMap<VisitLifecycleCleanable>> visitCleanables;

    public List<ContentHandlerConfigMap<SAXVisitBefore>> getVisitBefores() {
        return visitBefores;
    }

    public void setVisitBefores(List<ContentHandlerConfigMap<SAXVisitBefore>> visitBefores) {
        this.visitBefores = visitBefores;
    }

    public List<ContentHandlerConfigMap<SAXVisitChildren>> getChildVisitors() {
        return childVisitors;
    }

    public void setChildVisitors(List<ContentHandlerConfigMap<SAXVisitChildren>> childVisitors) {
        this.childVisitors = childVisitors;
    }

    public List<ContentHandlerConfigMap<SAXVisitAfter>> getVisitAfters() {
        return visitAfters;
    }

    public void setVisitAfters(List<ContentHandlerConfigMap<SAXVisitAfter>> visitAfters) {
        this.visitAfters = visitAfters;
    }

    public List<ContentHandlerConfigMap<VisitLifecycleCleanable>> getVisitCleanables() {
        return visitCleanables;
    }

    public void setVisitCleanables(List<ContentHandlerConfigMap<VisitLifecycleCleanable>> visitCleanables) {
        this.visitCleanables = visitCleanables;
    }
    
    public SAXElementVisitorMap merge(SAXElementVisitorMap map) {
    	if(map == null) {
    		// No need to merge...
    		return this;
    	}    	
    	
    	SAXElementVisitorMap merge = new SAXElementVisitorMap();
    	
        merge.visitBefores = new ArrayList<ContentHandlerConfigMap<SAXVisitBefore>>();
        merge.childVisitors = new ArrayList<ContentHandlerConfigMap<SAXVisitChildren>>();
        merge.visitAfters = new ArrayList<ContentHandlerConfigMap<SAXVisitAfter>>();
        merge.visitCleanables = new ArrayList<ContentHandlerConfigMap<VisitLifecycleCleanable>>();
        
        merge.visitBefores.addAll(visitBefores);
        merge.visitBefores.addAll(map.visitBefores);
        merge.childVisitors.addAll(childVisitors);
        merge.childVisitors.addAll(map.childVisitors);
        merge.visitAfters.addAll(visitAfters);
        merge.visitAfters.addAll(map.visitAfters);
        merge.visitCleanables.addAll(visitCleanables);
        merge.visitCleanables.addAll(map.visitCleanables);
    	
    	return merge;
    }
}
