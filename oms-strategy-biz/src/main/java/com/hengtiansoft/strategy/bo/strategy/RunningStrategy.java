package com.hengtiansoft.strategy.bo.strategy;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.hengtiansoft.eventbus.SubscribeEvent;
import com.hengtiansoft.strategy.bo.docker.callback.LogResultCallback;
import com.hengtiansoft.strategy.bo.engine.StrategyEngine;
import com.hengtiansoft.strategy.config.py4j.GatewayProperties;
import com.hengtiansoft.strategy.exception.StrategyException;
import com.hengtiansoft.strategy.bo.strategy.event.TickEvent;
import com.hengtiansoft.strategy.feign.MasterNodeService;
import com.hengtiansoft.strategy.model.RunningStrategyModel;
import com.hengtiansoft.strategy.model.StrategyModel;
import com.hengtiansoft.strategy.service.StrategyLogService;
import com.hengtiansoft.strategy.component.utils.ApplicationContextUtils;
import com.hengtiansoft.strategy.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.*;
import java.util.Stack;

@Slf4j
public class RunningStrategy extends BaseStrategy {

    private final static String CACHE_DIR = "strategy_cache/";
    private final static String WORKING_DIR = "/home/strategy_scripts/";
    private final static String CP_DIR = "/home/strategy_scripts/strategy/";
    private final static String IMAGE_NAME = "hquant:v2";

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
        ApplicationContext ac = ApplicationContextUtils.getApplicationContext();
        if(ac!=null) {
            this.dockerClient = ac.getBean(DockerClient.class);
        }
    }

    public RunningStrategy(String id, StrategyModel strategy)
    {
        this(id, strategy.getUserId(), strategy.getCode());
    }

    public RunningStrategy(String id, RunningStrategyModel strategy)
    {
        this(id, strategy.getUserId(), strategy.getCode());
    }

    public RunningStrategyModel getRunningStrategyModel() {
        return new RunningStrategyModel(id, userId, code);
    }

    public String getId() {
        return this.id;
    }

    public synchronized void init() {
        Stack<String> rollbackStack = new Stack<>();
        try {
            // 1. ??????????????????????????????
            createStrategyPy();
            rollbackStack.push("deleteStrategyPy");
            // 2. ??????????????????????????????
            CreateContainerResponse containerResponse = this.dockerClient
                    .createContainerCmd(RunningStrategy.IMAGE_NAME)
                    .withTty(true)
                    .withName(this.id)
                    .withCmd("/bin/bash")
                    .exec();
            rollbackStack.push("destroy");
            // 3. ????????????id
            this.containerId = containerResponse.getId();
            // 4. ??????????????????????????????????????????
            this.dockerClient.copyArchiveToContainerCmd(this.containerId)
                    .withHostResource(RunningStrategy.CACHE_DIR + this.id +"/strategy.py")
                    .withRemotePath(RunningStrategy.CP_DIR)
                    .exec();
            // 5. ????????????
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
            throw new StrategyException(this.id, String.format("Cannot init container: %s, %s",
                    this.id, ExceptionUtils.getStackTrace(e)));
        }
    }

    public synchronized void destroy() {
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

    final public void unsubscribe(String security) {
        removeEventListened(TickEvent.class, security);
    }

    private boolean execDockerCmd(String... cmd) {
        try {
            // 1. ????????????????????????
            InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(this.containerId).exec();
            Boolean isRunning = inspectContainerResponse.getState().getRunning();
            // 2. ????????????
            if(BooleanUtils.isFalse(isRunning)) {
                dockerClient.startContainerCmd(this.containerId).exec();
            }
            // 3. ????????????
            ExecCreateCmdResponse execCreateCmdResponse = this.dockerClient
                    .execCreateCmd(this.containerId)
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
            // 4. ??????????????????
            StrategyLogService strategyLogService = getStrategyLogService();
            String out;
            if (StringUtils.isNotBlank(out=resultCallback.getResult())) {
                strategyLogService.append(this.id, out);
            }
            String err;
            if (StringUtils.isNotBlank(err=resultCallback.getError())) {
                strategyLogService.append(this.id, err);
                throw new StrategyException(this.id, String.format("Python script error: %s", this.id));
            }
            return true;
        } catch (Exception e) {
            log.error(String.format("Cannot execute command: %s, %s",
                    this.id, ExceptionUtils.getStackTrace(e)));
        }
        return false;
    }

    private GatewayProperties getGatewayProperties() {
        return getBeanByType(GatewayProperties.class);
    }

    private StrategyEngine getStrategyEngine() {
        return getBeanByType(StrategyEngine.class);
    }

    private ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        return getBeanByType(ThreadPoolTaskExecutor.class);
    }

    private MasterNodeService getMasterNodeService() {
        return getBeanByType(MasterNodeService.class);
    }

    private StrategyLogService getStrategyLogService() {
        return getBeanByType(StrategyLogService.class);
    }

    private <T> T getBeanByType(Class<T> clazz) {
        ApplicationContext ac = ApplicationContextUtils.getApplicationContext();
        if(ac==null) {
            throw new StrategyException(this.id, String.format("Null WebApplicationContext: %s", this.id));
        }
        return ac.getBean(clazz);
    }

    public synchronized void initialize() {
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
    public synchronized void handleTick(TickEvent tickEvent) {
        try {
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
                throw new StrategyException(this.id, String.format("Cannot handleTick: %s", this.id));
            }
        } catch (Exception e) {
            StrategyEngine strategyEngine = getStrategyEngine();
            strategyEngine.unregisterStrategy(this.id);
            ThreadPoolTaskExecutor poolTaskExecutor = getThreadPoolTaskExecutor();
            poolTaskExecutor.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            MasterNodeService masterNodeService = getMasterNodeService();
                            masterNodeService.unregister(RunningStrategy.this.id);
                        }
                    }
            );

        }
    }

    private synchronized void createStrategyPy()
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
        catch(Exception e) {
            // ?????????????????????
            if(bufferedWriter!=null) {
                try {
                    bufferedWriter.close();
                } catch (IOException ex) {
                    log.error(String.format("Error abort directory creating when closing bufferedWriter: %s, %s",
                            this.id, ExceptionUtils.getStackTrace(ex)));
                }
            }
            if(file.exists()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException ex) {
                    log.error(String.format("Error abort directory creating aborting when deleting directory: %s, %s",
                            this.id, ExceptionUtils.getStackTrace(ex)));
                }
            }
            log.error(String.format("Error create directory: %s, %s", this.id, ExceptionUtils.getStackTrace(e)));
            throw new StrategyException(this.id, String.format("Error create directory: %s", this.id));
        }
    }

    private synchronized void deleteStrategyPy()
    {
        try{
            File file = new File(RunningStrategy.CACHE_DIR + this.id);
            if(file.exists()) {
                FileUtils.deleteDirectory(file);
            }
        } catch (IOException e) {
            log.error(String.format("Error delete directory: %s, %s", this.id, ExceptionUtils.getStackTrace(e)));
        }
    }
}
