package com.bassboy.services;

import com.bassboy.common.ConfigProp;
import com.bassboy.models.SchemaEvolverUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@Service
public class FbGraphApiService {

    @Value("api.facebook.graphUri")
    private String graphUri;

    private final RestTemplate restTemplate;

    public FbGraphApiService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public SchemaEvolverUser getProfile(String id, String accessToken) throws IOException {
        try {
            //params
            String params = "fields=id,name,email,first_name,last_name&access_token=" + accessToken;

            //build url
            String url = graphUri + id + "?" + params;

            //create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // create request
            HttpEntity request = new HttpEntity(headers);

            //use rest template
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            //check for status code
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root =  new ObjectMapper().readTree(response.getBody());

                // return a user object
                SchemaEvolverUser user = new SchemaEvolverUser();
                user.setSocialId("facebook:"+root.path("id").asText());
                user.setUsername(root.path("name").asText());
                user.setEmail(root.path("email").asText());
                return user;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}