package com.github.lzy.hotfix.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.github.lzy.hotfix.model.JvmProcess;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * @author liuzhengyang
 */
@Controller
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Value("${agent.path}")
    private String agentPath;

    @RequestMapping("")
    public String main(Model model) {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        List<JvmProcess> processList = list.stream()
                .map(JvmProcess::new)
                .collect(Collectors.toList());
        model.addAttribute("processList", processList);
        return "main";
    }

    @RequestMapping("/hotfix")
    @ResponseBody
    public String hotfix(@RequestParam("file") MultipartFile file,
            @RequestParam("targetPid") String targetPid,
            @RequestParam("targetClass") String targetClass
    ) throws Exception {
        VirtualMachine attach = VirtualMachine.attach(targetPid);
        Path replaceClassFile = Files.write(Paths.get("/tmp/" + targetClass), file.getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        logger.info("Save replace class file to {}", replaceClassFile);
        String agentArgs = String.join(",", targetClass,
                replaceClassFile.toFile().getAbsolutePath());
        try {
            attach.loadAgent(agentPath, agentArgs);
        } finally {
            attach.detach();
            replaceClassFile.toFile().delete();
        }
        return "ok";
    }
}
