package com.hengtiansoft.strategy.bo.strategy;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.hengtiansoft.eventbus.SubscribeEvent;
import com.hengtiansoft.strategy.bo.docker.callback.LogResultCallback;
import com.hengtiansoft.strategy.config.py4j.GatewayProperties;
import com.hengtiansoft.strategy.exception.StrategyException;
import com.hengtiansoft.strategy.bo.event.TickEvent;
import com.hengtiansoft.strategy.model.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.io.*;

@Slf4j
public class RunningStrategy extends BaseStrategy {

    private final static String CACHE_DIR = "strategy_cache/";
    private final static String WORKING_DIR = "/home/strategy_scripts/";
    private final static String CP_DIR = "/home/strategy_scripts/strategy/";
    private final static String IMAGE_NAME = "hquant:v1";

    private String id;
    private Strategy strategy;
    private DockerClient dockerClient;
    private String containerId;

    public RunningStrategy(String id, Strategy strategy)
    {
        this.id = id;
        this.strategy = strategy;
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        if(wac!=null) {
            this.dockerClient = wac.getBean(DockerClient.class);
        }
    }

    public void init() {
        if(!createStrategyPy()) {
            throw new StrategyException(this.id, String.format("Cannot create strategy.py: %s", this.id));
        }
        try {
            CreateContainerResponse containerResponse = this.dockerClient
                    .createContainerCmd(RunningStrategy.IMAGE_NAME)
                    .withName(this.id)
                    .withCmd("/bin/bash")
                    .exec();
            this.containerId = containerResponse.getId();
            this.dockerClient.copyArchiveToContainerCmd(this.containerId)
                    .withHostResource(RunningStrategy.CACHE_DIR + this.id +"/strategy.py")
                    .withRemotePath(RunningStrategy.CP_DIR)
                    .exec();
            if(!deleteStrategyPy()) {
                throw new StrategyException(this.id, String.format("Cannot delete strategy.py: %s", this.id));
            }
        }
        catch (StrategyException e) {
            destroy();
            throw new StrategyException(this.id, String.format("Cannot init container: %s, %s", this.id, getStackTrace(e)));
        }
        catch (Exception e) {
            destroy();
            deleteStrategyPy();
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

    final public void subscribe(String security)
    {
        addEventListened(TickEvent.class, security);
    }

    @SubscribeEvent
    public void HandleTick(TickEvent tickEvent)
    {
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        if(wac==null) {
            log.error(String.format("Null WebApplicationContext: %s", this.id));
            return;
        }
        GatewayProperties properties = wac.getBean(GatewayProperties.class);

        Boolean isRunning = null;
        try {
            InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(this.containerId).exec();
            isRunning = inspectContainerResponse.getState().getRunning();
        } catch (Exception e) {
            try {
                init();
            } catch (Exception ex) {
                log.error(String.format("Error init when HandleTick: %s", this.id));
            }
            throw new StrategyException(this.id, String.format("Cannot init HandleTick: %s, %s", this.id, getStackTrace(e)));
        }
        if(BooleanUtils.isFalse(isRunning)) {
            dockerClient.startContainerCmd(this.containerId).exec();
        }

        ExecCreateCmdResponse execCreateCmdResponse = this.dockerClient
                .execCreateCmd("py-docker")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withUser("strategy")
                .withWorkingDir(RunningStrategy.WORKING_DIR)
                .withCmd("python",
                        "./handleTick.py",
                        properties.getDefaultAddress(),
                        String.valueOf(properties.getPort()),
                        this.id,
                        tickEvent.toString()
                )
                .exec();

        LogResultCallback resultCallback = dockerClient
                .execStartCmd(execCreateCmdResponse.getId())
                .exec(new LogResultCallback());
        try {
            resultCallback.awaitCompletion();
        } catch (Exception e) {
            throw new StrategyException(this.id, String.format("Cannot handle tick: %s ", this.id));
        }
        // todo:存日志到数据库
        System.out.println(resultCallback.getResult());
    }

    private boolean createStrategyPy()
    {
        File file = null;
        BufferedWriter bufferedWriter = null;
        try{
            file = new File(RunningStrategy.CACHE_DIR + this.id);
            if(!file.exists()) {
                file.mkdirs();
            }
            bufferedWriter = new BufferedWriter(new FileWriter(new File(file,"strategy.py")));
            bufferedWriter.write(strategy.getCode());
            bufferedWriter.close();
            return true;
        }
        catch(IOException e) {
            if(file.exists()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException ex) {
                    log.error(String.format("Error delete directory when aborting: %s, %s", this.id, getStackTrace(ex)));
                }
            }
            log.error(String.format("Error create directory: %s, %s", this.id, getStackTrace(e)));
        }
        finally {
            if(bufferedWriter!=null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    log.error(String.format("Error create directory: %s, %s", this.id, getStackTrace(e)));
                }
            }
        }
        return false;
    }

    private boolean deleteStrategyPy()
    {
        try{
            File file = new File(RunningStrategy.CACHE_DIR + this.id);
            if(file.exists()) {
                FileUtils.deleteDirectory(file);
            }
            return true;
        } catch (IOException e) {
            log.error(String.format("Error delete directory: %s, %s", this.id, getStackTrace(e)));
        }
        return false;
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
