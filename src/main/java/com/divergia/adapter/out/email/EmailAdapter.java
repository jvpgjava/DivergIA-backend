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
        contexto.setVariable("token", espacarCaracteres(tokenBruto));
        contexto.setVariable("validadeMinutos", validadeRecuperacaoMinutos);
        contexto.setVariable("logoContentId", LOGO_CONTENT_ID);
        enviar(destinatario, "Recuperação de senha — DivergIA", "email/recuperacao-senha", contexto);
    }

    private String espacarCaracteres(String texto) {
        // Espaço literal em vez de letter-spacing: vários clientes de e-mail não centralizam com letter-spacing.
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
            // Multipart é obrigatório para a logo embutida via addInline.
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
