/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import org.openide.awt.ToolbarPool;
import org.openide.modules.ModuleInstall;

public class PCTModuleInstall extends ModuleInstall {

    @Override
    public void restored() {
        ToolbarPool.getDefault().setConfiguration("PCTToolbarLayout");
    }
}
