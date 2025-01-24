package org.example.expert.domain.user.controller;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.s3.service.S3Service;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserImageResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final S3Service s3Service;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/users")
    public void changePassword(@AuthenticationPrincipal AuthUser authUser,
        @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
    }

    @PostMapping("/users/profile")
    public ResponseEntity<UserImageResponse> saveProfileImage(@AuthenticationPrincipal AuthUser authUser,
        @RequestPart MultipartFile imageUrl) throws
        IOException {
        return ResponseEntity.ok(s3Service.upload(authUser, imageUrl, "user-profile/"));
    }

    @PutMapping("/users/profile")
    public ResponseEntity<UserImageResponse> changeProfileImage(@AuthenticationPrincipal AuthUser authUser,
        @RequestPart MultipartFile newImage, @RequestPart String oldImage) throws IOException {
        return ResponseEntity.ok(s3Service.updateFile(authUser, newImage, oldImage, "user-profile/"));
    }
}
