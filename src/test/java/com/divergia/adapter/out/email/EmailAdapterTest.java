package com.divergia.adapter.out.email;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailAdapter adapter;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        adapter = new EmailAdapter(mailSender, templateEngine, "no-reply@divergia.com", 30);
    }

    private MimeMessage novaMimeMessage() {
        return new MimeMessage(Session.getDefaultInstance(new Properties()));
    }

    /**
     * A logo embutida por Content-ID força a mensagem a ser multipart/related
     * — {@code getContent()} não devolve mais a {@code String} do HTML
     * diretamente, precisa procurar a parte {@code text/html} dentro do
     * multipart.
     */
    private String extrairHtml(MimeMessage mensagem) throws Exception {
        String html = procurarParte(mensagem, "text/html");
        assertThat(html).isNotNull();
        return html;
    }

    private boolean temLogoEmbutida(MimeMessage mensagem) throws Exception {
        return procurarParte(mensagem, "image/png") != null;
    }

    /**
     * A logo embutida por Content-ID faz o `MimeMessageHelper` montar uma
     * estrutura multipart aninhada (ex: multipart/mixed contendo um
     * multipart/related com o HTML + a imagem) — percorre recursivamente em
     * vez de assumir um nível fixo de aninhamento.
     */
    private String procurarParte(Part parte, String mimeType) throws Exception {
        if (parte.isMimeType(mimeType)) {
            Object conteudo = parte.getContent();
            return conteudo instanceof String texto ? texto : "";
        }
        if (parte.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) parte.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                String encontrado = procurarParte(multipart.getBodyPart(i), mimeType);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    @Test
    void enviarRecuperacaoSenhaDeveMontarOHtmlComOTokenEAValidade() throws Exception {
        MimeMessage mimeMessage = novaMimeMessage();
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        adapter.enviarRecuperacaoSenha("ana@example.com", "ABC123");
        // sem isso os content-types das partes internas do multipart ainda não
        // foram computados (o mock nunca chama send() de verdade, que é quem
        // normalmente dispara isso) — toda parte reporta "text/plain" até aqui.
        mimeMessage.saveChanges();

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo("Recuperação de senha — DivergIA");
        String html = extrairHtml(mimeMessage);
        // o e-mail espaça cada caractere do token ("A B C 1 2 3") pra
        // centralizar de forma confiável em qualquer cliente — ver
        // `EmailAdapter.espacarCaracteres`.
        assertThat(html).contains("A B C 1 2 3");
        assertThat(html).contains("30");
        assertThat(temLogoEmbutida(mimeMessage)).isTrue();
    }

    @Test
    void enviarBoasVindasDeveMontarOHtmlComONome() throws Exception {
        MimeMessage mimeMessage = novaMimeMessage();
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        adapter.enviarBoasVindas("ana@example.com", "Ana Clara");
        mimeMessage.saveChanges();

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo("Bem-vindo(a) ao DivergIA");
        String html = extrairHtml(mimeMessage);
        assertThat(html).contains("Ana Clara");
        assertThat(temLogoEmbutida(mimeMessage)).isTrue();
    }
}
