package com.example.ilia.aidemo.repository;


import com.example.ilia.aidemo.entity.PasswordResetTokens;
import com.example.ilia.aidemo.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokensRepository extends MongoRepository<PasswordResetTokens, Integer> {

    @Query(value = "{ 'owner.id' : ?0 }", sort = "{ 'expiredAt' : -1 }")
    List<Optional<PasswordResetTokens>> findTopByUserIdOrderByExpiredAtDesc(String userId);



    @Transactional
    void deleteAllByOwner(User user);
}
