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
package org.milyn.db;

import org.milyn.SmooksException;
import org.milyn.util.CollectionsUtil;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXElementVisitor;
import org.milyn.delivery.sax.SAXText;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.delivery.ExecutionLifecycleCleanable;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.ordering.Producer;
import org.w3c.dom.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

/**
 * DataSource management resource.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class AbstractDataSource implements SAXVisitBefore, DOMVisitBefore, Producer, VisitLifecycleCleanable, ExecutionLifecycleCleanable {

    private static final String DS_CONTEXT_KEY_PREFIX = AbstractDataSource.class.getName() + "#datasource:";
    private static final String CONNECTION_CONTEXT_KEY_PREFIX = AbstractDataSource.class.getName() + "#connection:";

    public final void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        bind(executionContext);
    }

    public final void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        bind(executionContext);
    }

    public final void executeVisitLifecycleCleanup(ExecutionContext executionContext) {
        unbind(executionContext);
    }

    public final void executeExecutionLifecycleCleanup(ExecutionContext executionContext) {
        // This guarantees Datasource resource cleanup (at the end of an ExecutionContext lifecycle) in
        // situations where the Smooks filter operation has terminated prematurely i.e. where the
        // executeVisitLifecycleCleanup event method was not called...
        unbind(executionContext);
    }

    private void bind(ExecutionContext executionContext) {
        executionContext.setAttribute(DS_CONTEXT_KEY_PREFIX + getName(), this);
    }

    protected void unbind(ExecutionContext executionContext) {
        try {
            Connection connection = (Connection) executionContext.getAttribute(CONNECTION_CONTEXT_KEY_PREFIX + getName());
            if(connection != null) {
                try {
                    if(!isAutoCommit()) {
                        // If there's no termination error on the context, commit, otherwise rollback...
                        if(executionContext.getTerminationError() == null) {
                            connection.commit();
                        } else {
                            connection.rollback();
                        }
                    }
                } finally {
                    executionContext.removeAttribute(CONNECTION_CONTEXT_KEY_PREFIX + getName());
                    connection.close();
                }
            }
        } catch (SQLException e) {
            throw new SmooksException("Unable to unbind DataSource '" + getName() + "'.");
        } finally {
            executionContext.removeAttribute(DS_CONTEXT_KEY_PREFIX + getName());
        }
    }

    public static Connection getConnection(String dataSourceName, ExecutionContext executionContext) throws SmooksException {
        Connection connection = (Connection) executionContext.getAttribute(CONNECTION_CONTEXT_KEY_PREFIX + dataSourceName);

        if(connection == null) {
            AbstractDataSource datasource = (AbstractDataSource) executionContext.getAttribute(DS_CONTEXT_KEY_PREFIX + dataSourceName);

            if(datasource == null) {
                throw new SmooksException("DataSource '" + dataSourceName + "' not bound to context.  Configure an '" + AbstractDataSource.class.getName() +  "' implementation and target it at '#document'.");
            }

            try {
                connection = datasource.getConnection();
                connection.setAutoCommit(datasource.isAutoCommit());
            } catch (SQLException e) {
                throw new SmooksException("Unable to open connection to dataSource '" + dataSourceName + "'.", e);
            }
            executionContext.setAttribute(CONNECTION_CONTEXT_KEY_PREFIX + dataSourceName, connection);
        }

        return connection;
    }

    public Set<String> getProducts() {
        return CollectionsUtil.toSet(getName());
    }

    public abstract String getName();

    public abstract Connection getConnection() throws SQLException;

    public abstract boolean isAutoCommit();

}
