package com.divergia.adapter.out.email;

import com.divergia.application.port.out.EmailPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailAdapter implements EmailPort {

    private static final Logger log = LoggerFactory.getLogger(EmailAdapter.class);

    private static final String LOGO_CONTENT_ID = "logo-divergia";
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

    // Assíncrono: o tempo de resposta de recuperar-senha não pode revelar se o e-mail existe.
    @Async
    @Override
    public void enviarRecuperacaoSenha(String destinatario, String tokenBruto) {
        Context contexto = new Context();
        contexto.setVariable("token", espacarCaracteres(tokenBruto));
        contexto.setVariable("validadeMinutos", validadeRecuperacaoMinutos);
        contexto.setVariable("logoContentId", LOGO_CONTENT_ID);
        enviarComSeguranca(destinatario, "Recuperação de senha — DivergIA", "email/recuperacao-senha", contexto);
    }

    private String espacarCaracteres(String texto) {
        // Espaço literal em vez de letter-spacing: vários clientes de e-mail não centralizam com letter-spacing.
        return String.join(" ", texto.split(""));
    }

    @Async
    @Override
    public void enviarBoasVindas(String destinatario, String nome) {
        Context contexto = new Context();
        contexto.setVariable("nome", nome);
        contexto.setVariable("logoContentId", LOGO_CONTENT_ID);
        enviarComSeguranca(destinatario, "Bem-vindo(a) ao DivergIA", "email/boas-vindas", contexto);
    }

    private void enviarComSeguranca(String destinatario, String assunto, String template, Context contexto) {
        try {
            enviar(destinatario, assunto, template, contexto);
        } catch (RuntimeException e) {
            log.warn("Falha ao enviar e-mail '{}': {}", assunto, e.getClass().getSimpleName());
        }
    }

    private void enviar(String destinatario, String assunto, String template, Context contexto) {
        String corpoHtml = templateEngine.process(template, contexto);
        MimeMessage mensagem = mailSender.createMimeMessage();
        try {
            // Multipart é obrigatório para a logo embutida via addInline.
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            helper.addInline(LOGO_CONTENT_ID, new ClassPathResource(LOGO_CLASSPATH));
        } catch (MessagingException e) {
            throw new IllegalStateException("Não foi possível montar o e-mail", e);
        }
        mailSender.send(mensagem);
    }
}
