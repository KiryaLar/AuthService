package ru.larkin.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.larkin.authservice.dto.response.SuccessResponse;
import ru.larkin.authservice.dto.response.UserResponse;
import ru.larkin.authservice.service.UserService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<SuccessResponse> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body(SuccessResponse.builder()
                .message("User with id " + id + " deleted")
                .timestamp(new Date().toString())
                .build());
    }
}
