package com.smartsurvey.module.user.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.marketplace.service.BadgeService;
import com.smartsurvey.module.user.dto.*;
import com.smartsurvey.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final BadgeService badgeService;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    public UserController(UserService userService, BadgeService badgeService) {
        this.userService = userService;
        this.badgeService = badgeService;
    }

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(@CurrentUser Long userId,
            @RequestParam("file") MultipartFile file) throws IOException {
        String ext = getExtension(file.getOriginalFilename());
        if (!Arrays.asList("jpg", "jpeg", "png", "gif", "webp").contains(ext.toLowerCase())) {
            return ApiResponse.fail(40000, "仅支持 jpg/png/gif/webp 格式");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            return ApiResponse.fail(40000, "文件大小不能超过2MB");
        }

        File dir = new File(uploadPath, "avatars");
        if (!dir.exists()) dir.mkdirs();

        String filename = "avatar_" + userId + "_" + System.currentTimeMillis() + "." + ext.toLowerCase();
        File dest = new File(dir, filename);
        file.transferTo(dest);

        String avatarUrl = "/uploads/avatars/" + filename;
        userService.updateAvatar(userId, avatarUrl);

        Map<String, String> result = new LinkedHashMap<>();
        result.put("avatarUrl", avatarUrl);
        return ApiResponse.ok(result);
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i + 1) : "";
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(@CurrentUser Long userId) {
        return ApiResponse.ok(userService.getProfile(userId));
    }

    @GetMapping("/profile/{targetUserId}")
    public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable Long targetUserId) {
        return ApiResponse.ok(userService.getProfileByUserId(targetUserId));
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateProfile(
            @CurrentUser Long userId, @Valid @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(userService.updateProfile(userId, req));
    }

    @GetMapping("/badges")
    public ApiResponse<List<Map<String, Object>>> getBadges(@CurrentUser Long userId) {
        return ApiResponse.ok(badgeService.getUserBadges(userId));
    }
}
