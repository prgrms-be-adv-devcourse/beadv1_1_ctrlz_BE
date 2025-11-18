package com.domainservice.domain.search.service;

import org.springframework.stereotype.Service;

import net.sf.extjwnl.dictionary.Dictionary;

import com.domainservice.domain.search.model.dto.request.Prefix;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DictionaryService {

	private final Dictionary dictionary;

	public void existsInDictionary(Prefix prefix) {

	}
}
