package com.yisi.stiku.web.util;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class HibernateAwareObjectMapper extends ObjectMapper {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3718562315601729729L;

	public HibernateAwareObjectMapper() {
        Hibernate4Module hibernate4Module = new Hibernate4Module();
        registerModule(hibernate4Module);

        SimpleModule myModule = new SimpleModule("MyModule", new Version(1, 0, 0, null, null, null));
        registerModule(myModule);

    }

    private static ObjectMapper objectMapper = new HibernateAwareObjectMapper();

    public static ObjectMapper getInstance() {
        return objectMapper;
    }
}