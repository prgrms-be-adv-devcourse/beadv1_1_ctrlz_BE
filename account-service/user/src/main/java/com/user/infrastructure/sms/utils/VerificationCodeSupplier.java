package com.user.infrastructure.sms.utils;

import java.util.Random;
import java.util.random.RandomGenerator;

public class VerificationCodeSupplier {

	private final static RandomGenerator RANDOM = new Random();

	public static String generateCode() {
		return String.valueOf(RANDOM.nextInt(111111,999999));
	}
}
