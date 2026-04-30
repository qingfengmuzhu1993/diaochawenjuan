package com.smartsurvey.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsurvey.module.user.entity.Notification;
import com.smartsurvey.module.user.mapper.NotificationMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    public Notification create(Long userId, String type, String title, String content,
                                Long relatedId, String relatedType) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setRelatedType(relatedType);
        n.setIsRead(0);
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);
        return n;
    }

    public List<Notification> getUnread(Long userId) {
        return notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
            .eq(Notification::getUserId, userId)
            .eq(Notification::getIsRead, 0)
            .orderByDesc(Notification::getCreatedAt));
    }

    public List<Notification> getAll(Long userId, int page, int size) {
        return notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
            .eq(Notification::getUserId, userId)
            .orderByDesc(Notification::getCreatedAt)
            .last("LIMIT " + (page - 1) * size + "," + size));
    }

    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
            .eq(Notification::getUserId, userId)
            .eq(Notification::getIsRead, 0));
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n != null && n.getUserId().equals(userId)) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unread = getUnread(userId);
        for (Notification n : unread) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }
}
