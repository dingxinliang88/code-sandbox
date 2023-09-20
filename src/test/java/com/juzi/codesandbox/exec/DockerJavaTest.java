package com.juzi.codesandbox.exec;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.io.IOException;
import java.util.List;

/**
 * @author codejuzi
 */
public class DockerJavaTest {
    public static void main(String[] args) {
        // 创建client
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(DockerProperties.DOCKER_HOST)
                .withApiVersion(DockerProperties.API_VERSION)
                .build();
        try (DockerClient dockerClient = DockerClientBuilder.getInstance(config).build()) {

            // 1、拉取镜像
            String image = "nginx:stable";
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            try {
                PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                    @Override
                    public void onNext(PullResponseItem item) {
                        System.out.println("Pull Image => " + item.getStatus());
                        super.onNext(item);
                    }
                };
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
                System.out.println("拉取完成");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // 2、创建容器
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
            CreateContainerResponse response = containerCmd.withCmd("echo", "Hello Nginx").exec();
            String containerId = response.getId();
            System.out.println("Container Id: " + containerId);

            // 3、查看容器状态
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
            List<Container> containerList = listContainersCmd.withShowAll(true).exec();
            for (Container container : containerList) {
                System.out.println(container);
            }

            // 4、启动容器
            dockerClient.startContainerCmd(containerId).exec();

            // 5、查看启动容器日志
            LogContainerResultCallback resultCallback = new LogContainerResultCallback() {
                @Override
                public void onNext(Frame item) {
                    System.out.println(containerId + " 此容器日志:" + new String(item.getPayload()));
                    super.onNext(item);
                }
            };
            dockerClient.logContainerCmd(containerId)
                    .withStdErr(true) // 错误输出
                    .withStdOut(true) // 标准输出
                    .exec(resultCallback)
                    .awaitCompletion(); // 异步操作

            // 6、删除容器
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true) // 强制删除
                    .exec();

            // 删除所有容器
            for (Container container : containerList) {
                if (container.getId() != null) {
                    dockerClient.removeContainerCmd(container.getId())
                            .withForce(true) // 强制删除
                            .exec();
                }
            }

            // 7、删除镜像
            dockerClient.removeImageCmd(image).exec();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
