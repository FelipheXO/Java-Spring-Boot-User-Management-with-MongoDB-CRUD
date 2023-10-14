package com.project.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.api.models.Users;

public interface UsersRepository extends MongoRepository<Users, String> {
    Users findByEmail(String email);

    Users findOneById(String email);

}
