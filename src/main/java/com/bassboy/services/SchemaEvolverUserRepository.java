package com.bassboy.services;

import com.bassboy.models.SchemaEvolverUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// we do not need to worry about implementing this, JpaRepository will implement it for us
public interface SchemaEvolverUserRepository extends JpaRepository<SchemaEvolverUser,Long> {
    SchemaEvolverUser findByUsername(String username);//specifying this tells Spring to implement this method
                                         //along with unimplemented JpaRepository methods
    SchemaEvolverUser findByEmail(String email);

    SchemaEvolverUser findBySocialId(String socialId);

    boolean existsUserByUsername(String username);

    boolean existsUserByEmail(String email);

    boolean existsUserBySocialId(String socialId);

}
