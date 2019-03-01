package com.github.lzy.hotfix.controller;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.github.lzy.hotfix.service.HotfixService;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

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

    @Resource
    private EurekaClient eurekaClient;

    @Resource
    private HotfixService hotfixService;

    @RequestMapping("/")
    public String main(Model model) throws UnknownHostException {
        model.addAttribute("processList", hotfixService.getProcessList());
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
        return Result.success(hotfixService.getProcessList());
    }

    @RequestMapping("/hostList")
    @ResponseBody
    public Result<List<String>> hostList() {
        Application application = eurekaClient.getApplication(APPLICATION_NAME);
        List<InstanceInfo> instances = Optional.ofNullable(application)
                .map(Application::getInstances)
                .orElse(Collections.emptyList());
        List<String> hostNames = instances.stream().map(InstanceInfo::getHostName).collect(toList());
        return Result.success(hostNames);
    }

    @RequestMapping("/hotfix")
    @ResponseBody
    public Result<HotfixResult> hotfix(@RequestParam("file") MultipartFile file,
            @RequestParam("targetPid") String targetPid,
            @RequestParam(value = "proxyServer", required = false) String proxyServer) throws Exception {
        if (proxyServer != null && !proxyServer.isEmpty()) {
            logger.info("Redirect hotfix request {} to {}", file, proxyServer);
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),
                    file.getBytes());
            MultipartBody.Part classFile = MultipartBody.Part.createFormData("file",
                    file.getName(), requestBody);
            RequestBody pidBody = RequestBody.create(MediaType.parse("multipart/form-data"), targetPid);
            return getProxyClient(proxyServer).reloadClass(classFile, pidBody).execute().body();
        }
        String targetClass = hotfixService.hotfix(file, targetPid);
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