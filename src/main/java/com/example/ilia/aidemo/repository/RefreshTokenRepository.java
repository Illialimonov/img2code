package com.example.ilia.aidemo.repository;

import com.example.ilia.aidemo.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByOwnerId(String id);
}
