package com.bassboy.services;

import com.bassboy.models.User;
import com.bassboy.secureapp.UserPrincipal;
import com.bassboy.secureapp.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    // UserRepository is our own defined interface
    @Autowired
    private UserRepository repo;//we do not implement methods. JPA implements the ones we need when we extend JPA

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUsername(username);
        if(user==null)
            throw new UsernameNotFoundException("Username not found");

        return new UserPrincipal(user);
    }
}
