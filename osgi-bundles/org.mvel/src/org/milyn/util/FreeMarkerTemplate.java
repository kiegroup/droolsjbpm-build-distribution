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
package org.milyn.util;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.milyn.SmooksException;
import org.milyn.assertion.AssertArgument;

import java.io.*;

/**
 *  FreeMarker template.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
*/
public class FreeMarkerTemplate {

    private String templateText;
    private Template template;

    public FreeMarkerTemplate(String templateText) {
        AssertArgument.isNotNullAndNotEmpty(templateText, "templateText");
        this.templateText = templateText;
        Reader templateReader = new StringReader(templateText);

        try {
            try {
                template = new Template("free-marker-template", templateReader, new Configuration());
            } finally {
                templateReader.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException.", e);
        }
    }

    public FreeMarkerTemplate(String templatePath, Class basePath) {
        AssertArgument.isNotNullAndNotEmpty(templatePath, "templatePath");
        this.templateText = templatePath;

        try {
            Configuration configuration = new Configuration();

            if(basePath != null) {
                configuration.setClassForTemplateLoading(basePath, "");
            }
            template = configuration.getTemplate(templatePath);
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException.", e);
        }
    }

    public String getTemplateText() {
        return templateText;
    }

    public String apply(Object contextObject) {
        StringWriter outputWriter = new StringWriter();
        try {
            template.process(contextObject, outputWriter);
        } catch (TemplateException e) {
            throw new SmooksException("Failed to apply template.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException.", e);
        }
        return outputWriter.toString();
    }
}