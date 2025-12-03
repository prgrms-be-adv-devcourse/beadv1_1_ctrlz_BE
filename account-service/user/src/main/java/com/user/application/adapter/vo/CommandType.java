package com.user.application.adapter.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommandType {
	CART_COMMAND("cart command userId : "),
	DEPOSIT_COMMAND("deposit command userId : ");

	private final String value;

	public String getContentWithUserId(String userId) {
		return this.value + userId;
	}
}
