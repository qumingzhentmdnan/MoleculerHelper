package com.fjut.moleculerhelper.util;

import com.fjut.moleculerhelper.entity.NoticeEntity;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

public class CommonUtils {
    // 跳转到指定文件的指定方法
    public static void navigateToMethod(Project project, String filePath, String methodName) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (virtualFile != null && virtualFile.exists()) {
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
            Editor newEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (newEditor != null) {
                String fileContent = newEditor.getDocument().getText();
                int methodIndex = fileContent.indexOf(methodName);
                if (methodIndex != -1) {
                    newEditor.getCaretModel().moveToOffset(methodIndex);
                    newEditor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                } else {
                    showMethodNotFoundNotification(project, new NoticeEntity("方法未找到", "方法 '" + methodName + "' 在文件中未找到", NotificationType.ERROR));
                }
            }
        }
    }

    public static void showMethodNotFoundNotification(Project project, NoticeEntity notice) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Moleculer Helper")
                .createNotification(
                        notice.getTitle(),
                       notice.getContent(),
                        notice.getType()
                )
                .setImportant(false)
                .notify(project);
    }
}
