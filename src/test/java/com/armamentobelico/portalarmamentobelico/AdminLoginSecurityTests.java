package com.armamentobelico.portalarmamentobelico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AdminLoginSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicPageShowsRestrictedAccessButtonAndLoginModalWhenAnonymous() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Acesso Restrito")))
                .andExpect(content().string(containsString("id=\"admin-login-dialog\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("id=\"admin-username\"")))
                .andExpect(content().string(containsString("id=\"admin-password\"")))
                .andExpect(content().string(containsString("Entrar no Painel")))
                .andExpect(content().string(containsString("/js/script.js")))
                .andExpect(content().string(containsString("</html>")))
                .andExpect(content().string(not(containsString("Inferno Aberto"))))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNotNull();
    }

    @Test
    void staticAssetRequestDoesNotCreateSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    void unauthenticatedAdminRequestRedirectsToHomeWithLoginPrompt() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?login=required"));
    }

    @Test
    void validLoginRedirectsToAdminPanel() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void invalidLoginRedirectsToHomeWithLoginError() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?loginError=true"));
    }

    @Test
    void authenticatedUserAccessesAdminAndSeesInfernoAbertoInFooter() throws Exception {
        mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Painel Administrativo")))
                .andExpect(content().string(containsString("Visão Geral")))
                .andExpect(content().string(containsString("/css/admin.css")))
                .andExpect(content().string(containsString("/js/admin.js")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("/admin/edicoes")))
                .andExpect(content().string(containsString("/admin/noticias")))
                .andExpect(content().string(containsString("/admin/eventos")))
                .andExpect(content().string(containsString("/admin/indicacoes")))
                .andExpect(content().string(containsString("/admin/aliados")));

        mockMvc.perform(get("/").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Inferno Aberto")))
                .andExpect(content().string(containsString("href=\"/admin\"")))
                .andExpect(content().string(not(containsString("id=\"admin-login-dialog\""))));
    }

    @Test
    void adminStaticAssetsAreAccessibleWithoutCreatingSession() throws Exception {
        MvcResult cssResult = mockMvc.perform(get("/css/admin.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".admin-shell")))
                .andReturn();
        assertThat(cssResult.getRequest().getSession(false)).isNull();

        MvcResult jsResult = mockMvc.perform(get("/js/admin.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("abz-admin-sidebar-collapsed")))
                .andReturn();
        assertThat(jsResult.getRequest().getSession(false)).isNull();
    }
}
