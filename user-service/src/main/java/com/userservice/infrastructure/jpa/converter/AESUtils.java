package com.userservice.infrastructure.jpa.converter;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AESUtils {

	@Value("${aes.key}")
	private String aesKey;

	public String encrypt(String value) {
		Key key = generateKey();
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] bytes = cipher.doFinal(value.getBytes());
			return Base64.getEncoder().encodeToString(bytes);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public String decrypt(String value) {
		Key key = generateKey();
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, key);
			byte[] decodedValue = Base64.getDecoder().decode(value);
			byte[] deccodedBytes = c.doFinal(decodedValue);
			return new String(deccodedBytes);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}

	private Key generateKey() {
		return new SecretKeySpec(aesKey.getBytes(), "AES");
	}
}
