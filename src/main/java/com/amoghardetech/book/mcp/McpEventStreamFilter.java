package com.amoghardetech.book.mcp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Claude opens {@code GET /mcp} to start an event stream before it loads tools.
 * The stateless MCP transport answers that GET with 405, and Claude treats the
 * 405 as a failed tool load. This filter keeps a quiet event stream open instead.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class McpEventStreamFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (!isEventStreamGet(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
		var async = request.startAsync();
		async.setTimeout(0);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/event-stream");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setHeader("Cache-Control", "no-cache, no-transform");
		response.setHeader("X-Accel-Buffering", "no");
		async.addListener(new AsyncListener() {
			@Override
			public void onComplete(AsyncEvent event) {
			}

			@Override
			public void onTimeout(AsyncEvent event) {
				event.getAsyncContext().complete();
			}

			@Override
			public void onError(AsyncEvent event) {
				event.getAsyncContext().complete();
			}

			@Override
			public void onStartAsync(AsyncEvent event) {
			}
		});
		var writer = response.getWriter();
		writer.write(": connected\n\n");
		writer.flush();
		response.flushBuffer();
	}

	private static boolean isEventStreamGet(HttpServletRequest request) {
		if (!"GET".equalsIgnoreCase(request.getMethod())) {
			return false;
		}
		String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
		String uri = request.getRequestURI();
		String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
		if (!"/mcp".equals(path)) {
			return false;
		}
		String accept = request.getHeader("Accept");
		return accept != null && accept.contains("text/event-stream");
	}

}
