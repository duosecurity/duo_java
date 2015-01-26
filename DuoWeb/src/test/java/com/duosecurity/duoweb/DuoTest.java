package com.duosecurity.duoweb;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DuoTest {

	/* Dummy IKEY and SKEY values */
	private static final String IKEY = "DIXXXXXXXXXXXXXXXXXX";
	private static final String WRONG_IKEY = "DIXXXXXXXXXXXXXXXXXY";
	private static final String SKEY = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef";
	private static final String AKEY = "useacustomerprovidedapplicationsecretkey";

	/* Dummy username */
	private static final String USER = "testuser";

	/* Dummy response signatures */
	private static final String INVALID_RESPONSE = "AUTH|INVALID|SIG";
	private static final String EXPIRED_RESPONSE = "AUTH|dGVzdHVzZXJ8RElYWFhYWFhYWFhYWFhYWFhYWFh8MTMwMDE1Nzg3NA==|cb8f4d60ec7c261394cd5ee5a17e46ca7440d702";
	private static final String FUTURE_RESPONSE = "AUTH|dGVzdHVzZXJ8RElYWFhYWFhYWFhYWFhYWFhYWFh8MTYxNTcyNzI0Mw==|d20ad0d1e62d84b00a3e74ec201a5917e77b6aef";
	private static final String WRONG_PARAMS_RESPONSE = "AUTH|dGVzdHVzZXJ8RElYWFhYWFhYWFhYWFhYWFhYWFh8MTYxNTcyNzI0M3xpbnZhbGlkZXh0cmFkYXRh|6cdbec0fbfa0d3f335c76b0786a4a18eac6cdca7";
	private static final String WRONG_PARAMS_APP = "APP|dGVzdHVzZXJ8RElYWFhYWFhYWFhYWFhYWFhYWFh8MTYxNTcyNzI0M3xpbnZhbGlkZXh0cmFkYXRh|7c2065ea122d028b03ef0295a4b4c5521823b9b5";

	@Test public void testSignRequest() {
		String request_sig;

		request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER);
		assertNotNull(request_sig);

		request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, "");
		assertEquals(request_sig, DuoWeb.ERR_USER);

		request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, "in|valid");
		assertEquals(request_sig, DuoWeb.ERR_USER);

		request_sig = DuoWeb.signRequest("invalid", SKEY, AKEY, USER);
		assertEquals(request_sig, DuoWeb.ERR_IKEY);

		request_sig = DuoWeb.signRequest(IKEY, "invalid", AKEY, USER);
		assertEquals(request_sig, DuoWeb.ERR_SKEY);

		request_sig = DuoWeb.signRequest(IKEY, SKEY, "invalid", USER);
		assertEquals(request_sig, DuoWeb.ERR_AKEY);
	}

	@Test public void testVerifyResponse() {
		String[] sigs;
		String request_sig;
		String valid_app_sig, invalid_app_sig;
		String invalid_user, expired_user, future_user;

		request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER);
		sigs = request_sig.split(":");
		valid_app_sig = sigs[1];

		request_sig = DuoWeb.signRequest(IKEY, SKEY, "invalidinvalidinvalidinvalidinvalidinvalid", USER);
		sigs = request_sig.split(":");
		invalid_app_sig = sigs[1];

		try {
			invalid_user = DuoWeb.verifyResponse(IKEY, SKEY, AKEY, INVALID_RESPONSE + ":" + valid_app_sig);
			fail();
		} catch (Exception e) {
		}

		try {
			expired_user = DuoWeb.verifyResponse(IKEY, SKEY, AKEY, EXPIRED_RESPONSE + ":" + valid_app_sig);
			fail();
		} catch (Exception e) {

		}

		try {
			future_user = DuoWeb.verifyResponse(IKEY, SKEY, AKEY, FUTURE_RESPONSE + ":" + invalid_app_sig);
			fail();
		} catch (Exception e) {

		}

		try {
			future_user = DuoWeb.verifyResponse(IKEY, SKEY, AKEY, FUTURE_RESPONSE + ":" + valid_app_sig);
			assertEquals(future_user, USER);
		} catch (Exception e) {
			fail();
		}

		try {
			future_user = DuoWeb.verifyResponse(IKEY, SKEY, AKEY, WRONG_PARAMS_RESPONSE + ":" + valid_app_sig);
			fail();
		} catch (Exception e) {

		}

		try {
			future_user = DuoWeb.verifyResponse(IKEY, SKEY, AKEY, FUTURE_RESPONSE + ":" + WRONG_PARAMS_APP);
			fail();
		} catch (Exception e) {

		}
		try {
			future_user = DuoWeb.verifyResponse(WRONG_IKEY, SKEY, AKEY, FUTURE_RESPONSE + ":" + valid_app_sig);
			fail();
		} catch (Exception e) {

		}

	}
}
