package com.amoghardetech.book.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class McpEventStreamFilterTest {

	private final McpEventStreamFilter filter = new McpEventStreamFilter();

	@Test
	void eventStreamGetStaysOpenInsteadOfFallingThrough() throws Exception {
		var request = new MockHttpServletRequest("GET", "/mcp");
		request.addHeader("Accept", "text/event-stream");
		request.setAsyncSupported(true);
		var response = new MockHttpServletResponse();

		filter.doFilter(request, response, (req, res) -> {
			throw new AssertionError("GET /mcp should not reach the stateless transport");
		});

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getContentType()).startsWith("text/event-stream");
		assertThat(response.getContentAsString()).contains(": connected");
		assertThat(request.isAsyncStarted()).isTrue();
	}

	@Test
	void otherRequestsContinueDownTheChain() throws Exception {
		var request = new MockHttpServletRequest("POST", "/mcp");
		request.addHeader("Accept", "application/json, text/event-stream");
		var response = new MockHttpServletResponse();
		var seen = new boolean[1];

		filter.doFilter(request, response, (req, res) -> seen[0] = true);

		assertThat(seen[0]).isTrue();
	}

}
