package com.rework.joss.persistence.convention.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class DAONamespaceHandler extends NamespaceHandlerSupport {
    
    public void init() {
        registerBeanDefinitionParser("dao", new ConventionDAODefinitionParser());        
    }
}