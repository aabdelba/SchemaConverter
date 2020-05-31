package com.bassboy.services;

import com.bassboy.models.SchemaEvolverUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;


@Service
public class SocialConnectionSignup implements ConnectionSignUp {

    @Autowired
    private SchemaEvolverUserRepository repo;

    private RestTemplate restTemplate;

    @Autowired
    public void HelloController(RestTemplateBuilder builder){
        this.restTemplate = builder.build();
    }

    @Override
    public String execute(Connection<?> connection) {

        String socialId = connection.getKey().toString();
        SchemaEvolverUser user = null;

        if(!repo.existsUserBySocialId(socialId)) {
            if(socialId.contains("facebook")) {
                FbGraphApiService fbApi = new FbGraphApiService(new RestTemplateBuilder());
                try {
                    user = fbApi.getProfile("me", connection.createData().getAccessToken());//fb api allows use of "me" instead of the userID
                } catch (IOException e) {
                    e.printStackTrace();//problem getting uri property from config.properties
                }
            }
            this.repo.save(user);
        } else {
            user = repo.findBySocialId(socialId);
        }
        return user.getUsername();
    }
}