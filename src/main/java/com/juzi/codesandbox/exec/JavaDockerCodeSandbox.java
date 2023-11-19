package com.juzi.codesandbox.exec;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.juzi.codesandbox.model.ExecuteCodeRequest;
import com.juzi.codesandbox.model.ExecuteCodeResponse;
import com.juzi.codesandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Docker实现代码沙箱
 *
 * @author codejuzi
 */
@Slf4j
@Component
public class JavaDockerCodeSandbox extends CodeSandboxTemplate {


    // todo 替换成实际的地址
    private static final String DOCKER_HOST = "xxx";

    private static final String API_VERSION = "1.43";

    private boolean FIRST_INIT = true;

    private static final Long TIME_OUT = 3000L;

    @Override
    protected List<ExecuteMessage> runCode(File userCodeFile, List<String> inputList) {
        // 3、创建容器，上传编译文件
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(DOCKER_HOST)
                .withApiVersion(API_VERSION)
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        // 3.1 拉取镜像
        String image = "openjdk:8-alpine";
        if (FIRST_INIT) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            try {
                PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                    @Override
                    public void onNext(PullResponseItem item) {
                        log.info("Pull Image => " + item.getStatus());
                        super.onNext(item);
                    }
                };
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
                log.info("Pull Image Succeed!");
                FIRST_INIT = false;
            } catch (InterruptedException e) {
                log.error("Pull Image Failed!");
                throw new RuntimeException(e);
            }
        }

        // 3.2 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        // 创建容器配置
        HostConfig hostConfig = new HostConfig();
        // 限制内存
        hostConfig.withMemory(100 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);
        // 设置CPU核数
        hostConfig.withCpuCount(1L);
        // 限制用户使用 root 权限写文件
        hostConfig.withReadonlyRootfs(true);
        // 开启Linux安全配置
        String linuxSecurityConfig = ResourceUtil.readUtf8Str("linux/security_config.json");
        hostConfig.withSecurityOpts(Collections.singletonList("seccomp=" + linuxSecurityConfig));
        // TODO 设置容器挂载目录
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app/code")));
//        hostConfig.setBinds(new Bind("/root/oj-codes", new Volume("/app/code")));
        CreateContainerResponse response = containerCmd
                .withHostConfig(hostConfig)
                // 禁用网络
                .withNetworkDisabled(true)
                // 开启输入输出
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                // 开启交互终端
                .withTty(true)
                .exec();

        String containerId = response.getId();

        // 3.3 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // 3.4 执行命令：docker exec containerId java -cp /app/code Main args
        List<ExecuteMessage> execMessageList = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();

        // 最大内存占用
        final Long[] maxMemory = {0L};
        final String[] dockerMessage = new String[1];
        final List<String> outputList = new ArrayList<>();
        final String[] errorDockerMessage = new String[1];
        long time;

        for (String inputArgs : inputList) {
            String[] inputArgsArr = inputArgs.split(" ");
            String[] cmdArr = ArrayUtil.append(new String[]{"java", "-cp", "/app/code", "Main"}, inputArgsArr);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArr)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            String execId = execCreateCmdResponse.getId();
            log.info("创建执行命令ID：{}", execId);

            final boolean[] isTimeOut = {true};
            if (execId == null) {
                throw new RuntimeException("执行命令不存在");
            }
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onNext(Frame frame) {
                    // 获取程序执行信息
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorDockerMessage[0] = new String(frame.getPayload());
                        log.error("错误输出结果：{}", errorDockerMessage[0]);
                    } else {
                        dockerMessage[0] = new String(frame.getPayload()).replace("\n", "");
                        outputList.add(dockerMessage[0]);
                        log.info("输出结果：{}", dockerMessage[0]);
                    }
                    super.onNext(frame);
                }

                @Override
                public void onComplete() {
                    // 设置不超时
                    isTimeOut[0] = false;
                    super.onComplete();
                }
            };

            // 3.5 获取占用的内存
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onStart(Closeable closeable) {
                }

                @Override
                public void onNext(Statistics statistics) {
                    Long usageMemory = Optional.ofNullable(statistics.getMemoryStats().getUsage()).orElse(0L);
                    maxMemory[0] = Math.max(usageMemory, maxMemory[0]);
                }

                @Override
                public void onError(Throwable throwable) {
                }

                @Override
                public void onComplete() {
                }

                @Override
                public void close() {

                }
            });
            statsCmd.exec(statisticsResultCallback);

            try {
                // 执行启动命令
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
                stopWatch.stop();
                // 获取总时间
                time = stopWatch.getLastTaskTimeMillis();
                // 关闭统计
                statsCmd.close();
            } catch (InterruptedException e) {
                log.error("程序执行异常");
                throw new RuntimeException(e);
            }

            ExecuteMessage executeMessage = new ExecuteMessage();
            if (isTimeOut[0]) {
                executeMessage.setMessage("超时");
            }
            executeMessage.setTime(time);
            executeMessage.setErrorMessage(errorDockerMessage[0]);
            executeMessage.setMemory(maxMemory[0]);
            execMessageList.add(executeMessage);
        }

        for (int i = 0; i < execMessageList.size(); i++) {
            if (i < outputList.size())
                execMessageList.get(i).setMessage(outputList.get(i));
        }

        // 删除容器
        dockerClient.removeContainerCmd(containerId)
                .withForce(true) // 强制删除
                .exec();
        return execMessageList;
    }

}
