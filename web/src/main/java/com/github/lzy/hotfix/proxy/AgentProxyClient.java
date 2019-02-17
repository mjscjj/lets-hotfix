package com.github.lzy.hotfix.proxy;

import java.util.List;

import com.github.lzy.hotfix.model.HotfixResult;
import com.github.lzy.hotfix.model.JvmProcess;
import com.github.lzy.hotfix.model.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * @author liuzhengyang
 */
public interface AgentProxyClient {
    @GET("/processList")
    Call<Result<List<JvmProcess>>> getJvmProcess();

    @Multipart
    @POST("/hotfix")
    Call<Result<HotfixResult>> reloadClass(@Part MultipartBody.Part file,
            @Part("targetPid") RequestBody targetPid);
}
