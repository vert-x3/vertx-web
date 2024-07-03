package io.vertx.ext.web.impl;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.HttpServerRequestInternal;
import io.vertx.ext.web.AllowForwardHeaders;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class HttpServerRequestWrapperTest {

	@Test
	public void test_authority_can_be_null() {
		HttpServerRequest request = mock(HttpServerRequestInternal.class);
		HttpServerRequestWrapper wrapper = new HttpServerRequestWrapper(request, AllowForwardHeaders.NONE);
		assertNull(request.authority());
		assertNull(wrapper.authority());
	}

	@Test
	public void test_scheme_can_be_null() {
		HttpServerRequest request = mock(HttpServerRequestInternal.class);
		HttpServerRequestWrapper wrapper = new HttpServerRequestWrapper(request, AllowForwardHeaders.NONE);
		assertNull(request.scheme());
		assertNull(wrapper.scheme());
	}

	@Test
	public void test_path_can_be_null() {
		HttpServerRequest request = mock(HttpServerRequestInternal.class);
		HttpServerRequestWrapper wrapper = new HttpServerRequestWrapper(request, AllowForwardHeaders.NONE);
		assertNull(request.path());
		assertNull(wrapper.path());
	}

	@Test
	public void test_query_can_be_null() {
		HttpServerRequest request = mock(HttpServerRequestInternal.class);
		HttpServerRequestWrapper wrapper = new HttpServerRequestWrapper(request, AllowForwardHeaders.NONE);
		assertNull(request.query());
		assertNull(wrapper.query());
	}
}

