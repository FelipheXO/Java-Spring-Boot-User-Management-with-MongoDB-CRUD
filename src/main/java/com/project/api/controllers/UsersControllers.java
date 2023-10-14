package com.project.api.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.api.models.ErrorResponse;
import com.project.api.models.Users;
import com.project.api.models.role.RoleUsers;
import com.project.api.security.PasswordSecurity;
import com.project.api.security.TokenService;
import com.project.api.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UsersControllers {

    @Autowired
    private UsersService usersService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private PasswordSecurity passwordSecurity;

    @PostMapping("create")
    public ResponseEntity<?> create(@RequestBody Users user) {

        String name = user.getName();
        String email = user.getEmail();
        String phone = user.getPhone();
        String password = user.getPassword();

        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("The 'name' field is required."));
        }
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("The 'email' field is required."));
        }

        Users existsByEmail = this.usersService.findByEmail(email);
        if (existsByEmail != null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("The 'email' field is already in use."));
        }

        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("The 'phone' field is required."));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("The 'password' field must have at least 6 characters."));
        }

        String hashPassword = this.passwordSecurity.hashPassword(password);

        Users newUser = this.usersService.create(new Users(name, email, phone, hashPassword));

        Map<String, Object> response = new HashMap<>();
        RoleUsers roleUsers = new RoleUsers();
        roleUsers.setId(newUser.getId());

        response.put("token", tokenService.generateToken(roleUsers));
        return ResponseEntity.ok(response);

    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("The 'email' field is required."));
        }

        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("The 'password' field must have at least 6 characters."));
        }

        Users user = this.usersService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid email or password."));
        }

        boolean validPassword = passwordSecurity.isPasswordValid(password, user.getPassword());
        if (!validPassword) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid email or password."));
        }

        Map<String, Object> response = new HashMap<>();
        RoleUsers roleUsers = new RoleUsers();
        roleUsers.setId(user.getId());

        response.put("token", tokenService.generateToken(roleUsers));
        return ResponseEntity.ok(response);

    }

    @GetMapping("get")
    public ResponseEntity<?> get(final HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("User not found."));
        }

        String id = tokenService.validateToken(token);
        Users user = this.usersService.findOneById(id);
        if (user == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("User not found."));
        }

        return ResponseEntity.ok(user);
    }

    @PutMapping("update")
    public ResponseEntity<?> update(final HttpServletRequest request, @RequestBody Users user) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("User not found."));
        }

        String id = tokenService.validateToken(token);
        Users updateUser = this.usersService.update(id, user);

        return ResponseEntity.ok(updateUser);
    }

}
