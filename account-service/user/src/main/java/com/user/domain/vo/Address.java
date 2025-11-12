package com.user.domain.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
public class Address {

	private String zipCode;
	private String city;
	private String street;
	private String state;
	private String details;

	@Builder
	public Address(String zipCode, String city, String street, String state, String details) {
		this.zipCode = zipCode;
		this.city = city;
		this.street = street;
		this.state = state;
		this.details = details;
	}

	public void changeZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public void changeCity(String city) {
		this.city = city;
	}

	public void changeStreet(String street) {
		this.street = street;
	}

	public void changeState(String state) {
		this.state = state;
	}

	public void changeDetails(String details) {
		this.details = details;
	}
}
