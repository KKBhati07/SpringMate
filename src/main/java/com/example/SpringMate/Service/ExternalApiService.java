package com.example.SpringMate.Service;

import com.example.SpringMate.models.ExternalApiResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


//method with @PostConstruct annotation, invokes automatically as soon as the Bean is created
@Component
public class ExternalApiService {

    private final String  key = "";
    @Value("${external.api.path}")
    private String api;

    private final RestTemplate restTemplate;

    @Autowired
    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ExternalApiResponse[] getData(){
        ResponseEntity<ExternalApiResponse[]> response= restTemplate.getForEntity(api, ExternalApiResponse[].class);
        return response.getBody();
    }

    public ExternalApiResponse[] getDataWithExchange(){
        ResponseEntity<ExternalApiResponse[]> response = restTemplate.exchange(api, HttpMethod.GET,null, ExternalApiResponse[].class);
        return response.getBody();
    }


}
