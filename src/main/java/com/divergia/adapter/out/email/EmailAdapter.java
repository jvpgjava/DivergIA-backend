package com.divergia.adapter.out.email;

import com.divergia.application.port.out.EmailPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailAdapter implements EmailPort {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailAdapter(JavaMailSender mailSender, @Value("${spring.mail.username:}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    @Override
    public void enviarRecuperacaoSenha(String destinatario, String tokenBruto) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Recuperação de senha — DivergIA");
        mensagem.setText(
                "Use o código abaixo para redefinir sua senha. Ele expira em pouco tempo.\n\n"
                        + tokenBruto
                        + "\n\nSe você não solicitou isso, ignore este e-mail.");
        mailSender.send(mensagem);
    }
}
