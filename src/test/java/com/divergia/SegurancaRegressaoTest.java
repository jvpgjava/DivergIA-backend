package com.divergia;

import com.divergia.adapter.out.storage.FotoPerfilDiscoAdapter;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.TokenPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.application.usecase.AutenticarUsuarioService;
import com.divergia.application.usecase.CredenciaisInvalidasException;
import com.divergia.config.LimiteDeCorpoFilter;
import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.service.GuardaDeDerivas;
import com.divergia.domain.service.TipoDeImagem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SegurancaRegressaoTest {

    @Test
    void tipoDeImagemVemDosBytesENaoDoNome() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0};
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] webp = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

        assertThat(TipoDeImagem.detectar(png)).contains(TipoDeImagem.PNG);
        assertThat(TipoDeImagem.detectar(jpeg)).contains(TipoDeImagem.JPEG);
        assertThat(TipoDeImagem.detectar(webp)).contains(TipoDeImagem.WEBP);
        assertThat(TipoDeImagem.detectar("<html><script>alert(1)</script>".getBytes())).isEmpty();
        assertThat(TipoDeImagem.detectar("<svg onload=alert(1)>".getBytes())).isEmpty();
    }

    @Test
    void fotoNuncaSaiDaPastaDeUploadsNemAceitaExtensaoForaDaLista(@TempDir Path pasta) throws Exception {
        FotoPerfilDiscoAdapter adapter = new FotoPerfilDiscoAdapter(pasta.toString(), "http://localhost/uploads");

        assertThatThrownBy(() -> adapter.salvar(UUID.randomUUID(), new byte[] {1}, "/../../pwn"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> adapter.salvar(UUID.randomUUID(), new byte[] {1}, "html"))
                .isInstanceOf(IllegalArgumentException.class);

        String url = adapter.salvar(UUID.randomUUID(), new byte[] {1}, "png");
        assertThat(url).endsWith(".png");
        try (var arquivos = Files.list(pasta)) {
            assertThat(arquivos).hasSize(1);
        }
    }

    @Test
    void loginComEmailInexistenteTambemPassaPeloBcrypt() {
        UsuarioRepositoryPort repositorio = mock(UsuarioRepositoryPort.class);
        PasswordEncoderPort encoder = mock(PasswordEncoderPort.class);
        given(encoder.codificar(anyString())).willReturn("hash-ficticio");
        given(repositorio.buscarPorEmail("ninguem@example.com")).willReturn(Optional.empty());
        AutenticarUsuarioService service = new AutenticarUsuarioService(repositorio, encoder, mock(TokenPort.class));

        assertThatThrownBy(() -> service.autenticar("ninguem@example.com", "qualquer123"))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verify(encoder).confere(eq("qualquer123"), eq("hash-ficticio"));
    }

    @Test
    void corpoJsonGrandeDemaisERecusadoComStatus413() throws Exception {
        LimiteDeCorpoFilter filtro = new LimiteDeCorpoFilter(1024);
        MockHttpServletRequest grande = new MockHttpServletRequest("POST", "/api/auth/login");
        grande.setContentType("application/json");
        grande.setContent(new byte[4096]);
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(grande, resposta, new MockFilterChain());

        assertThat(resposta.getStatus()).isEqualTo(413);
        assertThat(resposta.getContentAsString(StandardCharsets.UTF_8)).contains("grande demais");
    }

    @Test
    void uploadMultipartNaoPassaPeloLimiteDeJson() throws Exception {
        LimiteDeCorpoFilter filtro = new LimiteDeCorpoFilter(1024);
        MockHttpServletRequest upload = new MockHttpServletRequest("POST", "/api/analises");
        upload.setContentType("multipart/form-data; boundary=x");
        upload.setContent(new byte[4096]);
        MockFilterChain cadeia = new MockFilterChain();

        filtro.doFilter(upload, new MockHttpServletResponse(), cadeia);

        assertThat(cadeia.getRequest()).isNotNull();
    }

    @Test
    void explicacaoQueCitaTextoDeOutraPessoaETrocada() {
        String original = "Gosto de corridas leves ao ar livre.";
        String editado = "Gosto de corridas moderadas ao ar livre.";
        AvaliacaoDeDeriva deriva = new AvaliacaoDeDeriva(TipoDesvio.INTENSIDADE, "corridas leves", "corridas moderadas",
                "Como em \"meu chefe me demitiu ontem à noite\", a força da frase mudou.", 0.4);

        var resultado = new GuardaDeDerivas().filtrar(original, editado, List.of(deriva));

        assertThat(resultado.explicacoesTrocadas()).isEqualTo(1);
        assertThat(resultado.aprovadas().get(0).explicacao()).doesNotContain("chefe");
    }

    @Test
    void explicacaoQueCitaOProprioTextoDoUsuarioEMantida() {
        String original = "Gosto de corridas leves ao ar livre.";
        String editado = "Gosto de corridas moderadas ao ar livre.";
        AvaliacaoDeDeriva deriva = new AvaliacaoDeDeriva(TipoDesvio.INTENSIDADE, "corridas leves", "corridas moderadas",
                "O trecho \"gosto de corridas leves\" ficou mais intenso na versão editada.", 0.4);

        assertThat(new GuardaDeDerivas().filtrar(original, editado, List.of(deriva)).explicacoesTrocadas()).isZero();
    }
}
