package com.escapehub.escapehub.config;

import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DevelopmentCurrentUserFilterTest {

    private final DevelopmentCurrentUserFilter filter =
            new DevelopmentCurrentUserFilter();

    @Test
    void validUserIdIsAddedToRequest() throws Exception {

        UUID userId = UUID.randomUUID();

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                DevelopmentCurrentUserFilter.HEADER_NAME,
                userId.toString()
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                new MockFilterChain()
        );

        assertEquals(
                userId,
                request.getAttribute(
                        DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE
                )
        );

        assertEquals(200, response.getStatus());
    }

    @Test
    void missingUserIdDoesNotPreventRequest() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                new MockFilterChain()
        );

        assertNull(
                request.getAttribute(
                        DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE
                )
        );

        assertEquals(200, response.getStatus());
    }

    @Test
    void invalidUserIdReturnsBadRequest() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                DevelopmentCurrentUserFilter.HEADER_NAME,
                "not-a-valid-uuid"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(400, response.getStatus());

        assertNull(chain.getRequest());
    }
}
