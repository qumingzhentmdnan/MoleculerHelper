package com.fjut.moleculerhelper.config;

import com.fjut.moleculerhelper.listener.ListenClickToJump;
import com.fjut.moleculerhelper.util.ActionsHighlighting;
import com.fjut.moleculerhelper.util.ServiceFileScanner;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.Disposer;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// 项目启动时扫描services文件夹下的service文件，解析服务名、方法等信息
public class Init implements ProjectActivity {
    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        // 扫描服务文件，保存服务名、文件路径等信息
        ServiceFileScanner.scanServiceFiles(project);
        Disposable disposable = Disposer.newDisposable();

        // 订阅文件编辑器管理器，监听文件编辑器的打开、关闭等事件，当触发时高亮显示服务调用
        ActionsHighlighting editorListener = new ActionsHighlighting();
        project.getMessageBus()
                .connect(disposable)
                .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, editorListener);
        Disposer.register(project, disposable);

        // 注册鼠标监听事件
        ListenClickToJump listenMouseClickToJump = new ListenClickToJump();
        EditorFactory.getInstance().getEventMulticaster()
                .addEditorMouseListener(listenMouseClickToJump, disposable);

        return Unit.INSTANCE;
    }
}
