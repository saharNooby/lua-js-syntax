package me.saharnooby.luajssyntax.util;

import lombok.NonNull;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author saharNooby
 * @since 22:14 23.08.2019
 */
public final class HashUtil {

	public static String md5(@NonNull String s) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes(StandardCharsets.UTF_8));
			return hashToString(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static String hashToString(byte[] hash) {
		StringBuilder builder = new StringBuilder(new BigInteger(1, hash).toString(16));

		while (builder.length() < 32) {
			builder.insert(0, '0');
		}

		return builder.toString();
	}

}
