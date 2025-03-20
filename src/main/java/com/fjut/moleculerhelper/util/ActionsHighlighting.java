package com.fjut.moleculerhelper.util;

import com.fjut.moleculerhelper.status.MoleculerGlobalData;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionsHighlighting implements FileEditorManagerListener {
    public static final Pattern METHOD_CALL_PATTERN =
            Pattern.compile("(?:ctx\\.call|this\\.broker\\.call)\\('([^']+)'");

    // 文件打开时，渲染高亮
    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        highlightServiceCalls(source, file);
    }

    // 文件切换时，重新渲染高亮
    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile newFile = event.getNewFile();
        if (newFile != null) {
            FileEditorManager source = event.getManager();
            highlightServiceCalls(source, newFile);
        }
    }

    // 匹配文件中的服务调用，高亮显示
    private void highlightServiceCalls(FileEditorManager source, VirtualFile file) {
        // 如果文件不在服务文件列表中，不进行高亮
        if (!MoleculerGlobalData.pathToMoleculerEntity.containsKey(file.getPath())) {
            return;
        }

        FileEditor[] editors = source.getEditors(file);
        for (FileEditor fileEditor : editors) {
            if (fileEditor instanceof TextEditor) {
                Editor editor = ((TextEditor) fileEditor).getEditor();
                String text = editor.getDocument().getText();

                Matcher matcher = METHOD_CALL_PATTERN.matcher(text);
                while (matcher.find()) {
                    int start = matcher.start(1);
                    int end = matcher.end(1);
                    highlightText(editor, start, end);
                }
            }
        }
    }

    // 高亮标注文本
    private static void highlightText(Editor editor, int startOffset, int endOffset) {
        TextAttributes attributes = new TextAttributes(
                JBColor.BLUE,
                null,
                JBColor.BLUE,
                null,
                Font.PLAIN
        );

        MarkupModel markupModel = editor.getMarkupModel();
        markupModel.addRangeHighlighter(
                startOffset,
                endOffset,
                HighlighterLayer.SELECTION - 1,
                attributes,
                HighlighterTargetArea.EXACT_RANGE
        );
    }
}
