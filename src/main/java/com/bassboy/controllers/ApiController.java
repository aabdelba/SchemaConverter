package com.bassboy.controllers;

import com.bassboy.models.RequestUserObject;
import com.bassboy.models.SchemaEvolverUser;
import com.bassboy.models.UserPrincipal;
import com.bassboy.services.SchemaEvolverUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Basic;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

//    @Autowired
//    SchemaEvolverUserDetailsService userDetailsService;
//
//    @GetMapping(value = "/user", produces = "application/json")
//    public SchemaEvolverUser getUser(@RequestParam String username) {
//        return userDetailsService.getUser(username);
//    }
//
//    @PostMapping(value = "/user", produces = "application/json")
//    public SchemaEvolverUser postUser(@RequestBody RequestUserObject user, Principal principal){
//        System.out.println(principal.toString());
//        return userDetailsService.createUser(new SchemaEvolverUser(user));
//    }



}
