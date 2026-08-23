package com.divergia.adapter.out.extraction;

import com.divergia.application.port.out.ExtracaoDocumentoException;
import com.divergia.application.port.out.ExtracaoDocumentoPort;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Chama o microsserviço Python de extração de documento (FastAPI + Docling)
 * via HTTP. Único adapter autorizado a conhecer o contrato desse serviço
 * (rota, formato de request/response).
 */
@Component
@EnableConfigurationProperties(ExtracaoDocumentoProperties.class)
public class DoclingExtracaoDocumentoAdapter implements ExtracaoDocumentoPort {

    private final RestClient restClient;

    public DoclingExtracaoDocumentoAdapter(ExtracaoDocumentoProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(properties.timeoutSegundos());
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public String extrairTexto(byte[] conteudo, String nomeArquivo) {
        MultipartBodyBuilder corpo = new MultipartBodyBuilder();
        corpo.part("arquivo", new ByteArrayResource(conteudo)).filename(nomeArquivo);

        try {
            ExtracaoResponse resposta = restClient.post()
                    .uri("/extrair")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(corpo.build())
                    .retrieve()
                    .body(ExtracaoResponse.class);

            if (resposta == null || resposta.texto() == null) {
                throw new ExtracaoDocumentoException(
                        "Serviço de extração respondeu sem texto para o arquivo: " + nomeArquivo);
            }
            return resposta.texto();
        } catch (RestClientException e) {
            throw new ExtracaoDocumentoException(
                    "Falha ao extrair texto do arquivo '" + nomeArquivo + "' via serviço de extração", e);
        }
    }

    private record ExtracaoResponse(String texto) {
    }
}
