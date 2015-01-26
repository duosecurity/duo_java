package com.duosecurity.duoweb;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class DuoWeb {
	private static final String DUO_PREFIX = "TX";
	private static final String APP_PREFIX = "APP";
	private static final String AUTH_PREFIX = "AUTH";

	private static final int DUO_EXPIRE = 300;
	private static final int APP_EXPIRE = 3600;

	private static final int IKEY_LEN = 20;
	private static final int SKEY_LEN = 40;
	private static final int AKEY_LEN = 40;

	public static final String ERR_USER = "ERR|The username passed to sign_request() is invalid.";
	public static final String ERR_IKEY = "ERR|The Duo integration key passed to sign_request() is invalid.";
	public static final String ERR_SKEY = "ERR|The Duo secret key passed to sign_request() is invalid.";
	public static final String ERR_AKEY = "ERR|The application secret key passed to sign_request() must be at least " + AKEY_LEN + " characters.";
	public static final String ERR_UNKNOWN = "ERR|An unknown error has occurred.";

	public static String signRequest(String ikey, String skey, String akey, String username) {
		String duo_sig;
		String app_sig;

		if (username.equals("")) {
			return ERR_USER;
		}
		if (username.indexOf('|') != -1) {
			return ERR_USER;
		}
		if (ikey.equals("") || ikey.length() != IKEY_LEN) {
			return ERR_IKEY;
		}
		if (skey.equals("") || skey.length() != SKEY_LEN) {
			return ERR_SKEY;
		}
		if (akey.equals("") || akey.length() < AKEY_LEN) {
			return ERR_AKEY;
		}

		try {
			duo_sig = signVals(skey, username, ikey, DUO_PREFIX, DUO_EXPIRE);
			app_sig = signVals(akey, username, ikey, APP_PREFIX, APP_EXPIRE);
		} catch (Exception e) {
			return ERR_UNKNOWN;
		}

		return duo_sig + ":" + app_sig;
	}

	public static String verifyResponse(String ikey, String skey, String akey, String sig_response)
		throws DuoWebException, NoSuchAlgorithmException, InvalidKeyException, IOException {
		String auth_user = null;
		String app_user = null;

		String[] sigs = sig_response.split(":");
		String auth_sig = sigs[0];
		String app_sig = sigs[1];

		auth_user = parseVals(skey, auth_sig, AUTH_PREFIX, ikey);
		app_user = parseVals(akey, app_sig, APP_PREFIX, ikey);

		if (!auth_user.equals(app_user)) {
			throw new DuoWebException("Authentication failed.");
		}

		return auth_user;
	}

	private static String signVals(String key, String username, String ikey, String prefix, int expire) 
		throws InvalidKeyException, NoSuchAlgorithmException {
		long ts = System.currentTimeMillis() / 1000;
		long expire_ts = ts + expire;
		String exp = Long.toString(expire_ts);

		String val = username + "|" + ikey + "|" + exp;
		String cookie = prefix + "|" + Base64.encodeBytes(val.getBytes());
		String sig = Util.hmacSign(key, cookie);

		return cookie + "|" + sig;
	}

	private static String parseVals(String key, String val, String prefix, String ikey)
		throws InvalidKeyException, NoSuchAlgorithmException, IOException, DuoWebException {
		long ts = System.currentTimeMillis() / 1000;

		String[] parts = val.split("\\|");
		if (parts.length != 3) {
			throw new DuoWebException("Invalid response");
		}

		String u_prefix = parts[0];
		String u_b64 = parts[1];
		String u_sig = parts[2];

		String sig = Util.hmacSign(key, u_prefix + "|" + u_b64);
		if (!Util.hmacSign(key, sig).equals(Util.hmacSign(key, u_sig))) {
			throw new DuoWebException("Invalid response");
		}

		if (!u_prefix.equals(prefix)) {
			throw new DuoWebException("Invalid response");
		}

		byte[] decoded = Base64.decode(u_b64);
		String cookie = new String(decoded);

		String[] cookie_parts = cookie.split("\\|");
		if (cookie_parts.length != 3) {
			throw new DuoWebException("Invalid response");
		}
		String username = cookie_parts[0];
		String u_ikey = cookie_parts[1];
		String expire = cookie_parts[2];

		if (!u_ikey.equals(ikey)) {
			throw new DuoWebException("Invalid response");
		}

		long expire_ts = Long.parseLong(expire);
		if (ts >= expire_ts) {
			throw new DuoWebException("Transaction has expired. Please check that the system time is correct.");
		}

		return username;
	}
}
