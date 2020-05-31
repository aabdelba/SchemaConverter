package com.bassboy.services;

import com.bassboy.models.SchemaEvolverUser;
import org.springframework.data.jpa.repository.JpaRepository;

// we do not need to worry about implementing this, JpaRepository will implement it for us
public interface SchemaEvolverUserRepository extends JpaRepository<SchemaEvolverUser,Long> {
    SchemaEvolverUser findByUsername(String username);//specifying this tells Spring to implement this method
                                         //along with unimplemented JpaRepository methods

    boolean existsUserBySocialId(String socialId);

    SchemaEvolverUser findBySocialId(String socialId);

    boolean existsUserByUsername(String username);
}
