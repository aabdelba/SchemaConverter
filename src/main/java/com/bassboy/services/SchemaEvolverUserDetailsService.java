package com.bassboy.services;

import com.bassboy.models.SchemaEvolverUser;
import com.bassboy.models.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SchemaEvolverUserDetailsService implements UserDetailsService {

    // UserRepository is our own defined interface
    @Autowired
    private SchemaEvolverUserRepository repo;//we do not implement methods. JPA implements the ones we need when we extend JPA

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        SchemaEvolverUser user = repo.findByEmail(name);
        if(user==null)
            user = repo.findByUsername(name);
        if(user==null)
            throw new UsernameNotFoundException("Username not found");

        return new UserPrincipal(user);
    }

    public void createUser(String username, String email, String password){
        SchemaEvolverUser user = new SchemaEvolverUser();
        user.setUsername(username);
        user.setEmail(email);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(13);
        password = "{bcrypt}"+encoder.encode(password);
        user.setPassword(password);

        user.setCreatedTimestamp();
        user.setModifiedTimestamp();
        this.repo.save(user);
    }

    public void createSocialUserIfNotFound(String socialId, String displayName) throws UsernameNotFoundException {
        if (!repo.existsUserBySocialId(socialId)) {
            SchemaEvolverUser user = new SchemaEvolverUser();
            user.setSocialId(socialId);
            user.setDisplayName(displayName);
            user.setCreatedTimestamp();
            user.setModifiedTimestamp();
            this.repo.save(user);
        }
    }

    // THE FOLLOWING TWO METHODS WERE FOR API

    public SchemaEvolverUser getUser(String username) throws UsernameNotFoundException {
        SchemaEvolverUser user = repo.findByUsername(username);
        if(user==null)
            throw new UsernameNotFoundException("Username not found");

        return user;
    }

    public SchemaEvolverUser createUser(SchemaEvolverUser user) {
        String email = user.getEmail();
        String username = user.getUsername();
        if(repo.existsUserByEmail(email)) {
            user = repo.findByEmail(email);
        } else  if(repo.existsUserByUsername(username)) {
            user = repo.findByUsername(username);
        } else {
            this.repo.save(user);
            user = repo.findByEmail(email);
        }
        return user;
    }

    public boolean usernameExists(String username) {
        return repo.existsUserByUsername(username);
    }

    public boolean emailExists(String email) {
        return repo.existsUserByEmail(email);
    }
}
