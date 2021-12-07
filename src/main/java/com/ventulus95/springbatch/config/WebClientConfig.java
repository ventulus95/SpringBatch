package com.ventulus95.springbatch.config;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webclient(){
        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().secure(t->{
                            try {
                                t.sslContext(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build());
                            } catch (SSLException e) {
                                e.printStackTrace();
                            }
                        })
                ))
                .baseUrl("https://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
