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
package org.milyn.expression;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.milyn.cdr.SmooksConfigurationException;
import org.mvel2.DataConversion;
import org.mvel2.MVEL;
import org.mvel2.integration.impl.MapVariableResolverFactory;

/**
 * <a href="http://mvel.codehaus.org/">MVEL</a> expression evaluator.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MVELExpressionEvaluator implements ExpressionEvaluator {

    private static final String MVEL_VARIABLES_VARIABLE_NAME = "VARS";

	private String expression;

    private Serializable compiled;

    private boolean containsVariablesVariable;
    
    private Class<?> toType;

    public ExpressionEvaluator setExpression(String expression) throws SmooksConfigurationException {
        this.expression = expression.trim();
        compiled = MVEL.compileExpression(this.expression);

        containsVariablesVariable = this.expression.contains(MVEL_VARIABLES_VARIABLE_NAME);

        return this;
    }

    public String getExpression() {
        return expression;
    }
    
	public void setToType(Class<?> toType) {
		this.toType = toType;
	}

	public boolean eval(Object contextObject) throws ExpressionEvaluationException {
        return (Boolean) exec(contextObject);
    }

    @SuppressWarnings("unchecked")
	public Object exec(final Object contextObject) throws ExpressionEvaluationException {
        try {

        	if(containsVariablesVariable && contextObject instanceof Map) {

        		// We use two variableResolverFactories so that variables created in MVEL Scripts are put in the empty HashMap
        		// of the second VariableResolverFactory and not in the contextObject Map.
        		MapVariableResolverFactory contextVariableResolverFactory = new MapVariableResolverFactory((Map) contextObject);

	        	MapVariableResolverFactory variableResolverFactory = new MapVariableResolverFactory(new HashMap<String, Object>());
	        	variableResolverFactory.setNextFactory(contextVariableResolverFactory);

	        	// The VARS variable contains the MVELVariables object which get access to toe variableResolverFactory to be able to
	        	// do look in the variables of the resolver factory
	        	variableResolverFactory.createVariable(MVEL_VARIABLES_VARIABLE_NAME, new MVELVariables(variableResolverFactory));

	        	if(toType != null) {
	        		return  DataConversion.convert(MVEL.executeExpression(compiled, variableResolverFactory), toType);
	        	} else {
	        		return  MVEL.executeExpression(compiled, variableResolverFactory);
	        	}
        	} else {
	        	if(toType != null) {	        		
	        		return DataConversion.convert(MVEL.executeExpression(compiled, contextObject, new MapVariableResolverFactory(new HashMap<String, Object>())), toType);
	        	} else {
	        		return MVEL.executeExpression(compiled, contextObject, new MapVariableResolverFactory(new HashMap<String, Object>()));
	        	}
        	}

        } catch(Exception e) {
        	String msg = "Error evaluating MVEL expression '" + expression + "' against object type '" + contextObject.getClass().getName() + "'. " +
            				"Common issues include:" +
            				"\n\t\t1. Referencing a variable that is not bound into the context." +
            				" In this case use the 'isdef' operator to check if the variable is bound in the context." +
            				"\n\t\t2. Invalid expression reference to a List/Array based variable token.  Example List/Array referencing expression token: 'order.orderItems[0].productId'.";

            throw new ExpressionEvaluationException(msg, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Deprecated
	public Object getValue(final Object contextObject) throws ExpressionEvaluationException {
    	return exec(contextObject);
    }
}
