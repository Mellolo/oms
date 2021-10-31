import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.29.131:2375")
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        try {
            // 1. 获取容器运行状态
            InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd("py-docker").exec();
            Boolean isRunning = inspectContainerResponse.getState().getRunning();
            // 2. 开启容器
            if(BooleanUtils.isFalse(isRunning)) {
                dockerClient.startContainerCmd("py-docker").exec();
            }
            // 3. 执行命令
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient
                    .execCreateCmd("py-docker")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withUser("root")
                    .withWorkingDir("/home/strategy_scripts/")
                    .withCmd("python",
                            "./handleTick.py")
                    .exec();
            ExecStartResultCallback resultCallback = dockerClient
                    .execStartCmd(execCreateCmdResponse.getId())
                    .exec(new ExecStartResultCallback(null,System.err));
            resultCallback.awaitCompletion();
            // 4. 储存到数据库
            // todo:存日志到数据库
            //System.out.println(resultCallback.getResult());
            //return true;
        } catch (Exception e) {
        }

    }

}
