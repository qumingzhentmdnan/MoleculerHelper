package com.fjut.moleculerhelper.entity;

import com.intellij.notification.NotificationType;

import javax.management.Notification;

public class NoticeEntity {
    private String title;
    private String content;
    private NotificationType type;

    public NoticeEntity(String title, String content, NotificationType type) {
        this.title = title;
        this.content = content;
        this.type = type;
    }

    public NoticeEntity() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
}
