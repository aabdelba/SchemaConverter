package com.bassboy.services;

import com.bassboy.models.SchemaEvolverUser;
import com.bassboy.models.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemaEvolverUserDetailsService implements UserDetailsService {

    // UserRepository is our own defined interface
    @Autowired
    private SchemaEvolverUserRepository repo;//we do not implement methods. JPA implements the ones we need when we extend JPA

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SchemaEvolverUser user = repo.findByUsername(username);
        if(user==null)
            throw new UsernameNotFoundException("Username not found");

        return new UserPrincipal(user);
    }
//
//    public List<SchemaEvolverUser> loadAllUsers() {
//    }
}
