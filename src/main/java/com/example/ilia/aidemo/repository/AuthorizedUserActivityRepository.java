package com.example.ilia.aidemo.repository;

import com.example.ilia.aidemo.entity.AuthorizedUserActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuthorizedUserActivityRepository extends MongoRepository<AuthorizedUserActivity, String> {
    List<AuthorizedUserActivity> findAllByUserId(String userId);

}
