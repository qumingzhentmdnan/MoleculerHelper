package com.fjut.moleculerhelper.listener;

import com.fjut.moleculerhelper.entity.MoleculerServiceMethodCallEntity;
import com.fjut.moleculerhelper.entity.NoticeEntity;
import com.fjut.moleculerhelper.status.MoleculerGlobalData;
import com.fjut.moleculerhelper.util.CommonUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class ListenClickToSearchMethodCall extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        // 获取文件路径
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) return;
        String filePath = virtualFile.getPath();

        // 获取选中的文本
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) return;

        // 调用当前方法的方法列表
        String serviceName = MoleculerGlobalData
                .pathToMoleculerEntity
                .get(filePath)
                .getName();
        List<MoleculerServiceMethodCallEntity> methods = MoleculerGlobalData
                .nameToMoleculerServiceMethodCallEntity
                .get(serviceName + "." + selectedText);
        if (methods == null) {
            CommonUtils.showMethodNotFoundNotification(Objects.requireNonNull(editor.getProject()), new NoticeEntity("方法未找到", "方法 '" + selectedText + "' 未被调用", NotificationType.ERROR));
            return;
        }else if (methods.size() == 1){
            CommonUtils.navigateToMethod(Objects.requireNonNull(editor.getProject()), methods.get(0).getFilePath(), methods.get(0).getMethodName());
            return;
        }

        // 显示悬浮窗
        showMethodCallsPopup(editor, methods, selectionModel);
    }

    private static void showMethodCallsPopup(Editor editor, List<MoleculerServiceMethodCallEntity> methods, SelectionModel selectionModel) {
        if (methods.isEmpty()) return;

        // 创建列表模型
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (MoleculerServiceMethodCallEntity method : methods) {
            listModel.addElement("调用方法：" + method.getMethodName() +"          " + method.getFilePath().substring(method.getFilePath().lastIndexOf('/')));
        }

        // 创建列表组件
        JBList<String> list = new JBList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = list.getSelectedIndex();
                if (index != -1) {
                    MoleculerServiceMethodCallEntity selectedMethod = methods.get(index);
                    CommonUtils.navigateToMethod(Objects.requireNonNull(editor.getProject()), selectedMethod.getFilePath(), selectedMethod.getMethodName());
                }
            }
        });

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                // 设置边距
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                // 设置高亮颜色
                if (!isSelected) {
                    label.setForeground(new JBColor(new Color(0, 102, 204), new Color(104, 159, 220)));
                }

                // 设置字体
                label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 13));

                return label;
            }
        });

        // 创建滚动面板
        JBScrollPane scrollPane = new JBScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        // 获取选中文本的位置并计算弹出窗口位置
        int offset = selectionModel.getSelectionStart();
        Point point = editor.visualPositionToXY(editor.offsetToVisualPosition(offset));
        point.y -= 450; // 向上偏移以显示在选中文本上方

        // 创建并显示弹出窗口
        JBPopupFactory.getInstance()
                .createComponentPopupBuilder(scrollPane, list)
                .setTitle("调用方法列表")
                .setMovable(true)
                .setResizable(true)
                .createPopup()
                .show(new RelativePoint(editor.getContentComponent(), point));
    }
}
