package com.hengtiansoft.strategy.bo.strategy;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.hengtiansoft.eventbus.SubscribeEvent;
import com.hengtiansoft.strategy.bo.docker.callback.LogResultCallback;
import com.hengtiansoft.strategy.config.py4j.GatewayProperties;
import com.hengtiansoft.strategy.controller.RegisterController;
import com.hengtiansoft.strategy.exception.StrategyException;
import com.hengtiansoft.strategy.bo.strategy.event.TickEvent;
import com.hengtiansoft.strategy.model.RunningStrategyModel;
import com.hengtiansoft.strategy.model.StrategyModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.io.*;
import java.util.Stack;

@Slf4j
public class RunningStrategy extends BaseStrategy {

    private final static String CACHE_DIR = "strategy_cache/";
    private final static String WORKING_DIR = "/home/strategy_scripts/";
    private final static String CP_DIR = "/home/strategy_scripts/strategy/";
    private final static String IMAGE_NAME = "hquant:v1";

    private String id;
    private String userId;
    private String code;
    private String containerId;
    private DockerClient dockerClient;

    public RunningStrategy(String id, String userId, String code)
    {
        this.id = id;
        this.userId = userId;
        this.code = code;
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        if(wac!=null) {
            this.dockerClient = wac.getBean(DockerClient.class);
        }
    }

    public RunningStrategy(String id, StrategyModel strategy)
    {
        this(id, strategy.getUserId(), strategy.getCode());
    }

    public RunningStrategyModel getRunningStrategyModel() {
        return new RunningStrategyModel(id, userId, code);
    }

    public String getId() {
        return this.id;
    }

    public void init() {
        Stack<String> rollbackStack = new Stack<>();
        try {
            // 1. 创建文件（需要回滚）
            createStrategyPy();
            rollbackStack.push("deleteStrategyPy");
            // 2. 创建容器（需要回滚）
            CreateContainerResponse containerResponse = this.dockerClient
                    .createContainerCmd(RunningStrategy.IMAGE_NAME)
                    .withTty(true)
                    .withName(this.id)
                    .withCmd("/bin/bash")
                    .exec();
            rollbackStack.push("destroy");
            // 3. 获取容器id
            this.containerId = containerResponse.getId();
            // 4. 复制文件到容器中（需要回滚）
            this.dockerClient.copyArchiveToContainerCmd(this.containerId)
                    .withHostResource(RunningStrategy.CACHE_DIR + this.id +"/strategy.py")
                    .withRemotePath(RunningStrategy.CP_DIR)
                    .exec();
            // 5. 删除文件
            deleteStrategyPy();
        }
        catch (Exception e) {
            while(!rollbackStack.empty()) {
                switch (rollbackStack.pop()) {
                    case "destroy": {
                        destroy();
                        break;
                    }
                    case "deleteStrategyPy": {
                        deleteStrategyPy();
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            throw new StrategyException(this.id, String.format("Cannot init container: %s, %s", this.id, getStackTrace(e)));
        }
    }

    public void destroy() {
        try {
            this.dockerClient.killContainerCmd(this.containerId).exec();
        } catch (Exception e) {
            log.error(String.format("Error kill when destroying: %s", this.id));
        }
        try {
            this.dockerClient.removeContainerCmd(this.containerId).exec();
        } catch (Exception e) {
            log.error(String.format("Error remove when destroying: %s", this.id));
        }
    }

    final public void subscribe(String security) {
        addEventListened(TickEvent.class, security);
    }

    private boolean execDockerCmd(String... cmd) {
        try {
            // 1. 获取容器运行状态
            InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(this.containerId).exec();
            Boolean isRunning = inspectContainerResponse.getState().getRunning();
            // 2. 开启容器
            if(BooleanUtils.isFalse(isRunning)) {
                dockerClient.startContainerCmd(this.containerId).exec();
            }
            // 3. 执行命令
            ExecCreateCmdResponse execCreateCmdResponse = this.dockerClient
                    .execCreateCmd("py-docker")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withUser("strategy")
                    .withWorkingDir(RunningStrategy.WORKING_DIR)
                    .withCmd(cmd)
                    .exec();
            LogResultCallback resultCallback = dockerClient
                    .execStartCmd(execCreateCmdResponse.getId())
                    .exec(new LogResultCallback());
            resultCallback.awaitCompletion();
            // 4. 储存到数据库
            String out;
            if ((out=resultCallback.getResult())!=null) {
                // todo:存日志到数据库
                System.out.println(out);
            }
            String err;
            if ((err=resultCallback.getError())!=null) {
                // todo:存日志到数据库
                System.out.println(err);
            }
            if (resultCallback.isError()) {
                throw new StrategyException(this.id, String.format("Python script error: %s", this.id));
            }
            return true;
        } catch (Exception e) {
            log.error(String.format("Cannot execute command: %s, %s", this.id, getStackTrace(e)));
        }
        return false;
    }

    private GatewayProperties getGatewayProperties() {
        return getBeanByType(GatewayProperties.class);
    }

    private RegisterController getRegisterController() {
        return getBeanByType(RegisterController.class);
    }

    private <T> T getBeanByType(Class<T> clazz) {
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        if(wac==null) {
            throw new StrategyException(this.id, String.format("Null WebApplicationContext: %s", this.id));
        }
        return wac.getBean(clazz);
    }

    public void initialize() {
        GatewayProperties properties = getGatewayProperties();
        boolean execResult = execDockerCmd(
                "python",
                "./initialize.py",
                properties.getDefaultAddress(),
                String.valueOf(properties.getPort()),
                this.id
        );
        if(!execResult) {
            throw new StrategyException(this.id, String.format("Cannot initialize: %s", this.id));
        }
    }

    @SubscribeEvent
    public void handleTick(TickEvent tickEvent) {
        GatewayProperties properties = getGatewayProperties();
        boolean execResult = execDockerCmd(
                "python",
                "./handleTick.py",
                properties.getDefaultAddress(),
                String.valueOf(properties.getPort()),
                this.id,
                tickEvent.toString()
        );
        if(!execResult) {
            log.error(String.format("Cannot handleTick: %s", this.id));

        }
    }

    private void createStrategyPy()
    {
        File file = null;
        BufferedWriter bufferedWriter = null;
        try{
            file = new File(RunningStrategy.CACHE_DIR + this.id);
            if(!file.exists()) {
                file.mkdirs();
            }
            bufferedWriter = new BufferedWriter(new FileWriter(new File(file,"strategy.py")));
            bufferedWriter.write(this.code);
            bufferedWriter.close();
        }
        catch(IOException e) {
            // 回滚文件夹创建
            if(bufferedWriter!=null) {
                try {
                    bufferedWriter.close();
                } catch (IOException ex) {
                    log.error(String.format("Error abort directory creating when closing bufferedWriter: %s, %s", this.id, getStackTrace(ex)));
                }
            }
            if(file.exists()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException ex) {
                    log.error(String.format("Error abort directory creating aborting when deleting directory: %s, %s", this.id, getStackTrace(ex)));
                }
            }
            log.error(String.format("Error create directory: %s, %s", this.id, getStackTrace(e)));
            throw new StrategyException(this.id, String.format("Error create directory: %s", this.id));
        }
    }

    private void deleteStrategyPy()
    {
        try{
            File file = new File(RunningStrategy.CACHE_DIR + this.id);
            if(file.exists()) {
                FileUtils.deleteDirectory(file);
            }
        } catch (IOException e) {
            log.error(String.format("Error delete directory: %s, %s", this.id, getStackTrace(e)));
        }
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
