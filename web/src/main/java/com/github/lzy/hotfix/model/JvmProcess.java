package com.github.lzy.hotfix.model;

import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * @author liuzhengyang
 */
public class JvmProcess {
    private String pid;
    private String displayName;

    public JvmProcess(VirtualMachineDescriptor virtualMachineDescriptor) {
        this.pid = virtualMachineDescriptor.id();
        this.displayName = virtualMachineDescriptor.displayName();
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
