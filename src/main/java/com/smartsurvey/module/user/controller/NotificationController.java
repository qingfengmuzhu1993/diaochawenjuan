package com.smartsurvey.module.user.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.user.entity.Notification;
import com.smartsurvey.module.user.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<Notification>> getAll(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(notificationService.getAll(userId, page, size));
    }

    @GetMapping("/unread")
    public ApiResponse<List<Notification>> getUnread(@CurrentUser Long userId) {
        return ApiResponse.ok(notificationService.getUnread(userId));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(@CurrentUser Long userId) {
        return ApiResponse.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@CurrentUser Long userId, @PathVariable Long id) {
        notificationService.markAsRead(id, userId);
        return ApiResponse.ok();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@CurrentUser Long userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.ok();
    }
}
