package com.fjut.moleculerhelper.listener;

import com.fjut.moleculerhelper.entity.NoticeEntity;
import com.fjut.moleculerhelper.status.MoleculerGlobalData;
import com.fjut.moleculerhelper.util.CommonUtils;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ListenClickToJump implements EditorMouseListener{
        public static final Pattern METHOD_CALL_PATTERN =
                Pattern.compile("(?:ctx\\.call|this\\.broker\\.call)\\('([^']+)'");

        @Override
        public void mouseClicked(EditorMouseEvent e) {
            if (!(e.getMouseEvent().isControlDown() || e.getMouseEvent().isMetaDown())) {
                return;
            }

            Editor editor = e.getEditor();
            Project project = editor.getProject();
            if (project == null) return;

            int offset = editor.getCaretModel().getOffset();
            String text = editor.getDocument().getText();

            int lineNumber = editor.getDocument().getLineNumber(offset);
            int lineStartOffset = editor.getDocument().getLineStartOffset(lineNumber);
            int lineEndOffset = editor.getDocument().getLineEndOffset(lineNumber);
            String line = text.substring(lineStartOffset, lineEndOffset);

            Matcher matcher = METHOD_CALL_PATTERN.matcher(line);
            if (matcher.find()) {

                // 如果光标不在匹配的方法字符串上中，不进行跳转
                int matchStart = lineStartOffset + matcher.start();
                int matchEnd = lineStartOffset + matcher.end();

                if (!(offset >= matchStart && offset <= matchEnd)) {
                    return;
                }


                String fullPath = matcher.group(1);
                int lastDotIndex = fullPath.lastIndexOf('.');

                if (lastDotIndex != -1) {
                    String servicePath = fullPath.substring(0, lastDotIndex);
                    String methodName = fullPath.substring(lastDotIndex + 1);

                    String filePath = MoleculerGlobalData.nameToMoleculerEntity.get(servicePath).getFilePath();
                    if (filePath != null) {
                        CommonUtils.navigateToMethod(project, filePath, methodName);
                    }else {
                        CommonUtils.showMethodNotFoundNotification(project, new NoticeEntity("方法未找到", "方法 '" + methodName + "' 在文件中未找到", NotificationType.ERROR));
                    }
                }
            }
        }
}
