package com.userservice.infrastructure.jpa.vo;

import com.userservice.infrastructure.jpa.converter.UserInformationConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Embeddable
public class EmbeddedAddress {
	@Convert(converter = UserInformationConverter.class)
	@Column(nullable = false)
	private String zipCode;

	@Convert(converter = UserInformationConverter.class)
	@Column(nullable = false)
	private String city;

	@Convert(converter = UserInformationConverter.class)
	@Column(nullable = false)
	private String street;

	@Convert(converter = UserInformationConverter.class)
	@Column(nullable = false)
	private String state;

	@Convert(converter = UserInformationConverter.class)
	@Column(nullable = false)
	private String details;

	@Builder
	public EmbeddedAddress(String zipCode, String city, String street, String state, String details) {
		this.zipCode = zipCode;
		this.city = city;
		this.street = street;
		this.state = state;
		this.details = details;
	}
}
