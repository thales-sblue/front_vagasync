package com.br.thalesdev.front_gestao_vagas.modules.candidate.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplyJobService {

    @Value("${host.api.gestao.vagas}")
    private String hostAPIGestaoVagas;

    public String execute(String token, UUID idJob) {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<UUID> request = new HttpEntity<>(idJob, headers);

        String url = hostAPIGestaoVagas.concat("/candidate/job/apply");

        var result = rt.postForObject(url, request, String.class);

        return result;
    }

}
