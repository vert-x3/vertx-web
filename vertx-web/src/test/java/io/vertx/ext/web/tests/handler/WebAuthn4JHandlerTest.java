/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.tests.handler;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.webauthn4j.RelyingParty;
import io.vertx.ext.auth.webauthn4j.Authenticator;
import io.vertx.ext.auth.webauthn4j.CredentialStorage;
import io.vertx.ext.auth.webauthn4j.WebAuthn4J;
import io.vertx.ext.auth.webauthn4j.WebAuthn4JOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.WebAuthn4JHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.tests.WebTestBase;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import com.webauthn4j.converter.AttestedCredentialDataConverter;
import com.webauthn4j.converter.AuthenticationExtensionsClientOutputsConverter;
import com.webauthn4j.converter.AuthenticatorDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.AttestationConveyancePreference;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.AuthenticatorAssertionResponse;
import com.webauthn4j.data.AuthenticatorAttachment;
import com.webauthn4j.data.AuthenticatorAttestationResponse;
import com.webauthn4j.data.AuthenticatorSelectionCriteria;
import com.webauthn4j.data.PublicKeyCredential;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import com.webauthn4j.data.PublicKeyCredentialRpEntity;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.PublicKeyCredentialUserEntity;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.UserVerificationRequirement;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.data.extension.client.AuthenticationExtensionClientOutput;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientInputs;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientInput;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import com.webauthn4j.test.EmulatorUtil;
import com.webauthn4j.test.authenticator.webauthn.WebAuthnAuthenticatorAdaptor;
import com.webauthn4j.test.client.ClientPlatform;
import com.webauthn4j.util.Base64UrlUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WebAuthn4JHandlerTest extends WebTestBase {

	private final ObjectConverter objectConverter = new ObjectConverter();

	private final AuthenticationExtensionsClientOutputsConverter authenticationExtensionsClientOutputsConverter = new AuthenticationExtensionsClientOutputsConverter(objectConverter);

	String rpName = "ACME Corporation";
	String username = "fromage";
	String displayName = "Stephane Epardaud";
	Origin origin = new Origin("http://localhost");
	private Object credId;
	private Object publicKey;

	private TestStorage testStorage;

	public static class TestStorage implements CredentialStorage {
		private final List<Authenticator> database = new ArrayList<>();
		private ContextInternal ctx;

		TestStorage(Context ctx){
			this.ctx = (ContextInternal)ctx;
		}

		@Override
		public Future<List<Authenticator>> find(String userName, String credentialId) {
			return ctx.succeededFuture(
					database.stream()
					.filter(entry -> {
						if (userName != null) {
							return userName.equals(entry.getUsername());
						}
						if (credentialId != null) {
							return credentialId.equals(entry.getCredID());
						}
						// This is a bad query! both username and credID are null
						return false;
					})
					.collect(Collectors.toList())
					);
		}

		@Override
		public Future<Void> storeCredential(Authenticator authenticator) {
			long credentialIdFound = database.stream()
					.filter(entry -> authenticator.getCredID().equals(entry.getCredID()))
					.count();
			long userNameFound = database.stream()
					.filter(entry -> authenticator.getUsername().equals(entry.getUsername()))
					.count();

			if (credentialIdFound > 0) {
				return ctx.failedFuture("Duplicate authenticator for credential ID "+authenticator.getCredID());
			} else if (userNameFound > 0) {
				return ctx.failedFuture("Duplicate user "+authenticator.getUsername());
			} else {
				database.add(authenticator);
				return ctx.succeededFuture();
			}
		}

		@Override
		public Future<Void> updateCounter(Authenticator authenticator) {
			long updated = database.stream()
					.filter(entry -> authenticator.getCredID().equals(entry.getCredID()))
					.peek(entry -> {
						// update existing counter
						entry.setCounter(authenticator.getCounter());
					}).count();

			if (updated > 0) {
				return ctx.succeededFuture();
			} else {
				return ctx.failedFuture("Authenticator not found for credential ID "+authenticator.getCredID());
			}
		}

		public void clear() {
			database.clear();
		}

	}

	@Before
	public void setup() throws Exception {
		testStorage = new TestStorage(vertx.getOrCreateContext());
		WebAuthn4J webauthn = WebAuthn4J.create(vertx, new WebAuthn4JOptions()
				.setRelyingParty(new RelyingParty().setName(rpName))
				)
				.credentialStorage(testStorage);
		WebAuthn4JHandler handler = WebAuthn4JHandler.create(webauthn);
		handler.setOrigin(origin.toString());

		// parse the BODY
		router.post()
		.handler(BodyHandler.create());
		// add a session handler
		router.route()
		.handler(SessionHandler
				.create(LocalSessionStore.create(vertx)));

		handler.setupCredentialsCreateCallback(router.post("/webauthn/register"));
		handler.setupCredentialsGetCallback(router.post("/webauthn/login"));
		handler.setupCallback(router.post("/webauthn/callback"));

		router.route("/protected/*").handler(handler);
	}

	@Test
	public void testRegisterAndLogin() throws Exception {

		Handler<RoutingContext> handler = rc -> {
			assertNotNull(rc.user());
			assertEquals(username, rc.user().subject());
			rc.response().end("Welcome to the protected resource!");
		};


		router.route("/welcome").handler(rc -> rc.response().end("Welcome"));
		router.route("/protected/somepage").handler(handler);

		testRequest(HttpMethod.GET, "/welcome", null, resp -> {
		}, 200, "OK", "Welcome");

		testRequest(HttpMethod.GET, "/protected/somepage", null, resp -> {
		}, 401, "Unauthorized", null);

		WebAuthnAuthenticatorAdaptor webAuthnAuthenticatorAdaptor = new WebAuthnAuthenticatorAdaptor(EmulatorUtil.PACKED_AUTHENTICATOR);
		ClientPlatform clientPlatform = new ClientPlatform(origin, webAuthnAuthenticatorAdaptor);

		String session1Cookie = testRegistration(clientPlatform);

		// Now try again with credentials
		testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader(HttpHeaders.COOKIE, session1Cookie), 200, "OK", "Welcome to the protected resource!");

		// Let's drop this session and try logging in
		String session2Cookie = testAuthentication(clientPlatform);

		// Now try again with credentials
		testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader(HttpHeaders.COOKIE, session2Cookie), 200, "OK", "Welcome to the protected resource!");
	}

	private String testRegistration(ClientPlatform clientPlatform) throws Exception {

		String[] obtainedChallenge = new String[1];
		String[] obtainedCookie = new String[1];
		JsonObject registerRequest = new JsonObject()
				.put("name", username);
		testRequestBuffer(HttpMethod.POST, "/webauthn/register", req -> {
			req.send(registerRequest.encode());
		}, resp -> {
			String cookie = resp.getHeader(HttpHeaders.SET_COOKIE);
			obtainedCookie[0] = extractVertxSessionCookie(cookie);
		}, 200, "OK", buffer -> {
			JsonObject jsonObject = buffer.toJsonObject();
			obtainedChallenge[0] = jsonObject.getString("challenge");
		});

		DefaultChallenge challenge = new DefaultChallenge(obtainedChallenge[0]);
		RegistrationRequest registrationRequest = createRegistrationRequest(clientPlatform, origin.getHost(), challenge, username, displayName);
		// dummy request
		JsonObject request = new JsonObject()
				.put("id", credId)
				.put("rawId", credId)
				.put("type", "public-key")
				.put("response", new JsonObject()
						.put("attestationObject", Base64UrlUtil.encodeToString(registrationRequest.getAttestationObject()))
						.put("clientDataJSON", Base64UrlUtil.encodeToString(registrationRequest.getClientDataJSON())));

		testRequest(HttpMethod.POST, "/webauthn/callback", req -> {
			req.putHeader(HttpHeaders.COOKIE, obtainedCookie[0]);
			req.send(request.encode());
		}, resp -> {
			String cookie = resp.getHeader(HttpHeaders.SET_COOKIE);
			obtainedCookie[0] = extractVertxSessionCookie(cookie);
		}, 204, "No Content", null);

		testStorage.find(username, null)
		.onSuccess(authenticators -> {
			Assert.assertNotNull(authenticators);
			Assert.assertEquals(1, authenticators.size());
			Authenticator authenticator = authenticators.get(0);
			// Check username, credid, counter, publicKey
			Assert.assertEquals(username, authenticator.getUsername());
			Assert.assertEquals(credId, authenticator.getCredID());
			Assert.assertEquals(1, authenticator.getCounter());
			Assert.assertEquals(publicKey, authenticator.getPublicKey());
		})
		.onFailure(x -> Assert.fail("Well that did not work"));

		return obtainedCookie[0];
	}

	private String extractVertxSessionCookie(String cookie) {
		if(cookie != null && cookie.startsWith("vertx-web.session=")) {
			if(cookie.indexOf(";") != -1) {
				return cookie.substring(0, cookie.indexOf(";"));
			} else {
				return cookie;
			}
		}
		return null;
	}

	private RegistrationRequest createRegistrationRequest(ClientPlatform clientPlatform, String rpId, Challenge challenge, String username, String displayName){
		AuthenticatorSelectionCriteria authenticatorSelectionCriteria =
				new AuthenticatorSelectionCriteria(
						AuthenticatorAttachment.CROSS_PLATFORM,
						true,
						UserVerificationRequirement.REQUIRED);

		PublicKeyCredentialParameters publicKeyCredentialParameters = new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256);

		PublicKeyCredentialUserEntity publicKeyCredentialUserEntity = new PublicKeyCredentialUserEntity(new byte[32], username, displayName);

		AuthenticationExtensionsClientInputs<RegistrationExtensionClientInput> extensions = new AuthenticationExtensionsClientInputs<>();
		PublicKeyCredentialCreationOptions credentialCreationOptions
		= new PublicKeyCredentialCreationOptions(
				new PublicKeyCredentialRpEntity(rpId, "example.com"),
				publicKeyCredentialUserEntity,
				challenge,
				Collections.singletonList(publicKeyCredentialParameters),
				null,
				Collections.emptyList(),
				authenticatorSelectionCriteria,
				AttestationConveyancePreference.DIRECT,
				extensions
				);
		PublicKeyCredential<AuthenticatorAttestationResponse, RegistrationExtensionClientOutput> credential = clientPlatform.create(credentialCreationOptions);
		AuthenticatorAttestationResponse registrationRequest = credential.getResponse();

		// save cred id and public key to verify later
		AuthenticatorDataConverter authenticatorDataConverter = new AuthenticatorDataConverter(objectConverter);
		AttestedCredentialDataConverter attestedCredentialDataConverter = new AttestedCredentialDataConverter(objectConverter);
		byte[] attestedCredentialDataBytes = authenticatorDataConverter.extractAttestedCredentialData(registrationRequest.getAuthenticatorData(objectConverter));
		AttestedCredentialData attestedCredentialData = attestedCredentialDataConverter.convert(attestedCredentialDataBytes);
		this.credId = Base64UrlUtil.encodeToString(attestedCredentialData.getCredentialId());
		this.publicKey = Base64UrlUtil.encodeToString(objectConverter.getCborConverter().writeValueAsBytes(attestedCredentialData.getCOSEKey()));
		AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> clientExtensionResults = credential.getClientExtensionResults();
		Set<String> transports = Collections.emptySet();
		String clientExtensionJSON = authenticationExtensionsClientOutputsConverter.convertToString(clientExtensionResults);
		return new RegistrationRequest(
				registrationRequest.getAttestationObject(),
				registrationRequest.getClientDataJSON(),
				clientExtensionJSON,
				transports
				);
	}

	private String testAuthentication(ClientPlatform clientPlatform) throws Exception {
		String[] obtainedChallenge = new String[1];
		String[] obtainedCookie = new String[1];
		JsonObject registerRequest = new JsonObject()
				.put("name", username);
		testRequestBuffer(HttpMethod.POST, "/webauthn/login", req -> {
			req.send(registerRequest.encode());
		}, resp -> {
			String cookie = resp.getHeader(HttpHeaders.SET_COOKIE);
			obtainedCookie[0] = extractVertxSessionCookie(cookie);
		}, 200, "OK", buffer -> {
			JsonObject jsonObject = buffer.toJsonObject();
			obtainedChallenge[0] = jsonObject.getString("challenge");
		});

		DefaultChallenge challenge = new DefaultChallenge(obtainedChallenge[0]);
		AuthenticationRequest authenticationRequest = createAuthenticationRequest(clientPlatform, origin.getHost(), challenge, username, displayName);
		// dummy request
		JsonObject request = new JsonObject()
				.put("id", credId)
				.put("rawId", credId)
				.put("type", "public-key")
				.put("response", new JsonObject()
						.put("signature", Base64UrlUtil.encodeToString(authenticationRequest.getSignature()))
						.put("authenticatorData", Base64UrlUtil.encodeToString(authenticationRequest.getAuthenticatorData()))
						.put("clientDataJSON", Base64UrlUtil.encodeToString(authenticationRequest.getClientDataJSON())));

		testRequest(HttpMethod.POST, "/webauthn/callback", req -> {
			req.putHeader(HttpHeaders.COOKIE, obtainedCookie[0]);
			req.send(request.encode());
		}, resp -> {
			String cookie = resp.getHeader(HttpHeaders.SET_COOKIE);
			obtainedCookie[0] = extractVertxSessionCookie(cookie);
		}, 204, "No Content", null);

		testStorage.find(username, null)
		.onSuccess(authenticators -> {
			Assert.assertNotNull(authenticators);
			Assert.assertEquals(1, authenticators.size());
			Authenticator authenticator = authenticators.get(0);
			// Check username, credid, counter, publicKey
			Assert.assertEquals(username, authenticator.getUsername());
			Assert.assertEquals(credId, authenticator.getCredID());
			Assert.assertEquals(2, authenticator.getCounter());
			Assert.assertEquals(publicKey, authenticator.getPublicKey());
		})
		.onFailure(x -> Assert.fail("Well that did not work"));

		return obtainedCookie[0];
	}

	private AuthenticationRequest createAuthenticationRequest(ClientPlatform clientPlatform, String rpId, Challenge challenge, String username, String displayName) {
		// get
		PublicKeyCredentialRequestOptions credentialRequestOptions = new PublicKeyCredentialRequestOptions(
				challenge,
				0l,
				rpId,
				null,
				UserVerificationRequirement.REQUIRED,
				null
				);

		PublicKeyCredential<AuthenticatorAssertionResponse, AuthenticationExtensionClientOutput> credential = clientPlatform.get(credentialRequestOptions);
		AuthenticatorAssertionResponse authenticationRequest = credential.getResponse();
		AuthenticationExtensionsClientOutputs<AuthenticationExtensionClientOutput> clientExtensionResults = credential.getClientExtensionResults();
		String clientExtensionJSON = authenticationExtensionsClientOutputsConverter.convertToString(clientExtensionResults);

		return new AuthenticationRequest(
				credential.getRawId(),
				authenticationRequest.getAuthenticatorData(),
				authenticationRequest.getClientDataJSON(),
				clientExtensionJSON,
				authenticationRequest.getSignature()
				);

	}

	protected void testRequestBuffer(HttpMethod method, String path, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
			int statusCode, String statusMessage,
			Consumer<Buffer> responseBodyBufferAction) throws Exception {
		RequestOptions requestOptions = new RequestOptions().setMethod(method).setPort(8080).setURI(path).setHost("localhost");
		CountDownLatch latch = new CountDownLatch(1);
		client.request(requestOptions).onComplete(onSuccess(req -> {
			req.response().onComplete(onSuccess(resp -> {
				assertEquals(statusCode, resp.statusCode());
				assertEquals(statusMessage, resp.statusMessage());
				if (responseAction != null) {
					responseAction.accept(resp);
				}
				if (responseBodyBufferAction == null) {
					latch.countDown();
				} else {
					resp.bodyHandler(buff -> {
						responseBodyBufferAction.accept(buff);
						latch.countDown();
					});
				}
			}));
			if (requestAction != null) {
				requestAction.accept(req);
			}
			req.end();
		}));
		awaitLatch(latch);
	}
}
