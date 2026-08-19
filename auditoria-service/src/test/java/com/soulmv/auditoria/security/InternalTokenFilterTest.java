package com.soulmv.auditoria.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class InternalTokenFilterTest {

    private static final String TOKEN = "segredo-compartilhado-123";
    private static final String PATH = "/api/auditoria/eventos";

    private final InternalTokenFilter filtro = new InternalTokenFilter(TOKEN);

    @Test
    void devePermitir_quandoTokenCorreto() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", PATH);
        request.addHeader("X-Internal-Token", TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filtro.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isNotEqualTo(403);
    }

    @Test
    void deveBloquear_quandoTokenAusente() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filtro.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void deveBloquear_quandoTokenErrado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", PATH);
        request.addHeader("X-Internal-Token", "token-invasor");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filtro.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void naoDeveExigirToken_emOutroCaminho() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auditoria");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filtro.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isNotEqualTo(403);
    }
}
