package com.github.lzy.hotfix.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.github.lzy.hotfix.model.HotfixResult;
import com.github.lzy.hotfix.model.JvmProcess;
import com.github.lzy.hotfix.model.Result;
import com.github.lzy.hotfix.proxy.AgentProxyClient;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author liuzhengyang
 */
@Controller
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private static final String APPLICATION_NAME = "LETS-HOTFIX";

    @Value("${agent.path}")
    private String agentPath;

    @Resource
    private EurekaClient eurekaClient;

    @RequestMapping("/")
    public String main(Model model) throws UnknownHostException {
        model.addAttribute("processList", getProcessList());
        model.addAttribute("hostname", InetAddress.getLocalHost().getHostName());
        Application application = eurekaClient.getApplication(APPLICATION_NAME);
        List<InstanceInfo> instances = Optional.ofNullable(application)
                .map(Application::getInstances).orElse(Collections.emptyList());
        model.addAttribute("instances", instances);
        return "main";
    }

    @RequestMapping("/processList")
    @ResponseBody
    public Result<List<JvmProcess>> processList(@RequestParam(value = "proxyServer",
            required = false) String proxyServer) throws IOException {
        if (proxyServer != null && !proxyServer.isEmpty()) {
            return getProxyClient(proxyServer).getJvmProcess().execute().body();
        }
        return Result.success(getProcessList());
    }

    private List<JvmProcess> getProcessList() {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        return list.stream()
                .map(JvmProcess::new)
                .collect(Collectors.toList());
    }

    @RequestMapping("/hotfix")
    @ResponseBody
    public Result<HotfixResult> hotfix(@RequestParam("file") MultipartFile file,
            @RequestParam("targetPid") String targetPid,
            @RequestParam(value = "proxyServer", required = false) String proxyServer) throws Exception {
        if (proxyServer != null && !proxyServer.isEmpty()) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),
                    file.getBytes());
            MultipartBody.Part classFile = MultipartBody.Part.createFormData("file",
                    file.getName(), requestBody);
            RequestBody pidBody = RequestBody.create(MediaType.parse("multipart/form-data"), targetPid);
            return getProxyClient(proxyServer).reloadClass(classFile, pidBody).execute().body();
        }
        logger.info("Hotfix {} {}", targetPid, file.getOriginalFilename());
        VirtualMachine attach = VirtualMachine.attach(targetPid);
        ClassReader classReader = new ClassReader(file.getBytes());
        String className = classReader.getClassName();
        String targetClass = className.replaceAll("/", ".");
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
        return Result.success(new HotfixResult(targetClass));
    }

    private AgentProxyClient getProxyClient(String proxyServer) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(proxyServer)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(AgentProxyClient.class);
    }
}