package com.example.ilia.aidemo.repository;

import com.example.ilia.aidemo.entity.UnauthorizedUserActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UnauthorizedUserActivityRepository extends MongoRepository<UnauthorizedUserActivity, String> {
    int countByLocalDateTimeBetweenAndFingerprint(LocalDateTime from, LocalDateTime to, String fingerprint);


}
