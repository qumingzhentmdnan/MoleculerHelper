package com.fjut.moleculerhelper.listener;

import com.fjut.moleculerhelper.status.MoleculerGlobalData;
import com.fjut.moleculerhelper.util.ServiceFileScanner;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ListenClickToRescanFile extends AnAction {
    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        try {
            ServiceFileScanner.scanServiceFiles(project);
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("Moleculer Helper")
                    .createNotification("服务扫描", "扫描到 " + MoleculerGlobalData.nameToMoleculerEntity.size() + " 个服务", NotificationType.INFORMATION)
                    .notify(project);
        } catch (Exception ex) {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("Moleculer Helper")
                    .createNotification("Error", ex.getMessage(), NotificationType.ERROR)
                    .notify(project);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 确保在项目中启用该action
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
