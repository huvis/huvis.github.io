/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.toolbox.statusBar;

import org.openide.awt.StatusDisplayer;

/**
 *
 * @author ybrise
 */
public class StatusBarHandler {

    public static void showMsg(String msg) {
        StatusDisplayer.getDefault().setStatusText(msg);
    }

    public static void showMsg(String msg, int importance) {
        StatusDisplayer.getDefault().setStatusText(msg, importance);
    }
}
