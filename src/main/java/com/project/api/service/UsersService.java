package com.project.api.service;

import org.springframework.stereotype.Service;
import com.project.api.models.Users;
import com.project.api.models.role.RoleUsers;
import com.project.api.repository.UsersRepository;

@Service
public class UsersService {

    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Users create(Users user) {
        return this.usersRepository.save(user);
    }

    public Users findByEmail(String email) {
        return this.usersRepository.findByEmail(email);
    }

    public Users findOneById(String id) {
        return this.usersRepository.findOneById(id);
    }

    public Users update(String id, Users updatedUser) {
        Users existingUser = this.findOneById(id);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhone(updatedUser.getPhone());
            return this.usersRepository.save(existingUser);
        }
        return null;
    }

    public RoleUsers roleFindOneById(String id) {
        Users user = findOneById(id);
        RoleUsers roleUsers = new RoleUsers();
        if (user != null) {
            roleUsers.setId(id);
            return roleUsers;
        }
        return null;
    }

}
