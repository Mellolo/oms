package com.hengtiansoft.strategy.bo.strategy;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.hengtiansoft.eventbus.SubscribeEvent;
import com.hengtiansoft.strategy.exception.StrategyException;
import com.hengtiansoft.strategy.model.Strategy;
import com.hengtiansoft.strategy.bo.event.TickEvent;
import org.apache.commons.lang.BooleanUtils;


public class RunningStrategy extends BaseStrategy {

    private String id;
    private String containerId;
    private DockerClient dockerClient;

    public RunningStrategy(String id, DockerClient dockerClient)
    {
        this.id = id;
        this.dockerClient = dockerClient;
    }

    public boolean init() {
        CreateContainerResponse containerResponse = this.dockerClient
                .createContainerCmd("镜像名称")
                .withName(this.id)
                .withCmd("/bin/bash")
                .exec();
        this.dockerClient.copyArchiveToContainerCmd()
    }

    public void destroy() {
        this.dockerClient.stopContainerCmd(this.id).exec();
        this.dockerClient.removeContainerCmd(this.id).exec();
    }

    //@SubscribeEvent
    //@DisallowConcurrentEvents
    public void HandleTickSynchronized(TickEvent tickEvent) throws Exception
    {
    }

    final public void subscribe(String security)
    {
        addEventListened(TickEvent.class, security);
    }

    @SubscribeEvent
    public void HandleTick(TickEvent tickEvent)
    {
        Boolean isRunning = null;
        try {
            InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(this.containerId).exec();
            isRunning = inspectContainerResponse.getState().getRunning();
        } catch (Exception e) {
            if(!init()) {
                throw new StrategyException(this.id, String.format("Cannot create container: %s", this.id));
            }
        }
        if(BooleanUtils.isFalse(isRunning)) {
            dockerClient.startContainerCmd(this.containerId).exec();
        }
    }
}
