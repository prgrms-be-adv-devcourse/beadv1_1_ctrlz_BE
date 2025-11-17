package com.domainservice.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

@Configuration
public class WordNetConfiguration {

	@Bean
	public Dictionary wordNetDictionary() throws JWNLException {
		return Dictionary.getDefaultResourceInstance();

	}
}
