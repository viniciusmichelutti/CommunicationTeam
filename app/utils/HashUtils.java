package utils;

import org.apache.commons.codec.digest.DigestUtils;

public final class HashUtils {

	public static String generateHash(String txt) {
		return DigestUtils.md5Hex(txt);
	}
	
}
