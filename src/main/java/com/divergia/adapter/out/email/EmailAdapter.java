package com.divergia.adapter.out.email;

import com.divergia.application.port.out.EmailPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailAdapter implements EmailPort {

    private static final String LOGO_CONTENT_ID = "logo-divergia";
    // Versão "dark" (texto branco) porque a logo fica sobre uma faixa escura
    // no cabeçalho do e-mail — não a versão de texto escuro, que é pensada
    // pra ir sobre fundo claro.
    private static final String LOGO_CLASSPATH = "templates/email/images/logo-divergia-dark.png";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String remetente;
    private final long validadeRecuperacaoMinutos;

    public EmailAdapter(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            @Value("${spring.mail.username:}") String remetente,
            @Value("${divergia.recuperacao-senha.validade-minutos:30}") long validadeRecuperacaoMinutos) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.remetente = remetente;
        this.validadeRecuperacaoMinutos = validadeRecuperacaoMinutos;
    }

    @Override
    public void enviarRecuperacaoSenha(String destinatario, String tokenBruto) {
        Context contexto = new Context();
        // Espaço de verdade entre cada caractere em vez de `letter-spacing` no
        // CSS — `letter-spacing` deixa um espaço sobrando depois do último
        // caractere que vários clientes de e-mail não compensam, então o
        // texto nunca centraliza direito por mais que se ajuste a margem.
        // Com espaço literal, `text-align:center` simplesmente funciona.
        contexto.setVariable("token", espacarCaracteres(tokenBruto));
        contexto.setVariable("validadeMinutos", validadeRecuperacaoMinutos);
        contexto.setVariable("logoContentId", LOGO_CONTENT_ID);
        enviar(destinatario, "Recuperação de senha — DivergIA", "email/recuperacao-senha", contexto);
    }

    private String espacarCaracteres(String texto) {
        return String.join(" ", texto.split(""));
    }

    @Override
    public void enviarBoasVindas(String destinatario, String nome) {
        Context contexto = new Context();
        contexto.setVariable("nome", nome);
        contexto.setVariable("logoContentId", LOGO_CONTENT_ID);
        enviar(destinatario, "Bem-vindo(a) ao DivergIA", "email/boas-vindas", contexto);
    }

    private void enviar(String destinatario, String assunto, String template, Context contexto) {
        String corpoHtml = templateEngine.process(template, contexto);
        MimeMessage mensagem = mailSender.createMimeMessage();
        try {
            // `true` (multipart) é obrigatório aqui — sem isso `addInline` falha,
            // já que a logo embutida por Content-ID exige uma mensagem
            // multipart/related, não uma simples text/html.
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            helper.addInline(LOGO_CONTENT_ID, new ClassPathResource(LOGO_CLASSPATH));
        } catch (MessagingException e) {
            throw new IllegalStateException("Não foi possível montar o e-mail: " + e.getMessage(), e);
        }
        mailSender.send(mensagem);
    }
}
