package com.bassboy.secureapp;

import com.bassboy.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

// we do not need to worry about implementing this, JpaRepository will implement it for us
public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);//we will pick Jpa's implementation of findByUsername
}
