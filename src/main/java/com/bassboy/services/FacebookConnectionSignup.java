package com.bassboy.services;

import com.bassboy.models.User;
import com.bassboy.secureapp.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;


@Service
public class FacebookConnectionSignup implements ConnectionSignUp {

    @Autowired
    private UserRepository repo;

    private RestTemplate restTemplate;

    @Autowired
    public void HelloController(RestTemplateBuilder builder){
        this.restTemplate = builder.build();
    }


    @Override
    public String execute(Connection<?> connection) {

        String socialId = connection.getKey().toString();
        User user;

        if(!repo.existsUserBySocialId(socialId)) {
            user = new User();
            user.setSocialId(socialId);
            user.setUsername(connection.getDisplayName());
            user.setPassword(randomAlphabetic(8));
            this.repo.save(user);
        } else {
            user = repo.findBySocialId(socialId);
        }
        return user.getUsername();
    }
}