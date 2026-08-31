package com.divergia.application.usecase;

import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CadastrarUsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private EmailPort emailPort;

    private CadastrarUsuarioService service;

    @BeforeEach
    void setUp() {
        service = new CadastrarUsuarioService(usuarioRepository, passwordEncoder, emailPort);
    }

    @Test
    void deveCadastrarUsuarioComSenhaCodificada() {
        given(usuarioRepository.existeComEmail("ana@example.com")).willReturn(false);
        given(passwordEncoder.codificar("senha12345")).willReturn("hash-codificado");
        given(usuarioRepository.salvar(any(Usuario.class))).willAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = service.cadastrar("Ana", "ana@example.com", "senha12345");

        assertThat(resultado.nome()).isEqualTo("Ana");
        assertThat(resultado.email()).isEqualTo("ana@example.com");
        assertThat(resultado.senhaHash()).isEqualTo("hash-codificado");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).salvar(captor.capture());
        assertThat(captor.getValue().senhaHash()).isEqualTo("hash-codificado");

        verify(emailPort).enviarBoasVindas("ana@example.com", "Ana");
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        given(usuarioRepository.existeComEmail("ana@example.com")).willReturn(true);

        assertThatThrownBy(() -> service.cadastrar("Ana", "ana@example.com", "senha12345"))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(usuarioRepository, never()).salvar(any());
        verify(passwordEncoder, never()).codificar(anyString());
        verify(emailPort, never()).enviarBoasVindas(anyString(), anyString());
    }
}
