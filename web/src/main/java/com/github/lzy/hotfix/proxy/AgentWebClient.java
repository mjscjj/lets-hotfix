package com.github.lzy.hotfix.proxy;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.lzy.hotfix.model.JvmProcess;
import com.github.lzy.hotfix.model.Result;

import reactor.core.publisher.Mono;

/**
 * @author liuzhengyang
 */
@Service
public class AgentWebClient {

    public Mono<Result<List<JvmProcess>>> getJvmProcess(String proxyServer) {
        WebClient webClient = WebClient.create(proxyServer);
        return webClient.get()
                .uri("/processList")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<List<JvmProcess>>>(){});
    }
}
