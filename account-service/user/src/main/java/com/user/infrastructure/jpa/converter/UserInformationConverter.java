package com.user.infrastructure.jpa.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Converter
public class UserInformationConverter implements AttributeConverter<String, String> {

	private final com.user.infrastructure.jpa.converter.AESUtils aesUtils;

	@Override
	public String convertToDatabaseColumn(String attribute) {
		return aesUtils.encrypt(attribute);
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		return aesUtils.decrypt(dbData);
	}
}
