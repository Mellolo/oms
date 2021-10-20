package com.hengtiansoft.strategy.gateway;

import py4j.CallbackClient;
import py4j.GatewayServer;
import py4j.Py4JNetworkException;

import javax.net.ServerSocketFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class JavaGatewayServer extends GatewayServer {

    public JavaGatewayServer(Object entryPoint, int port, int pythonPort, String defaultAddress) {
        super(
            entryPoint,
            port,
            getInetAddress(defaultAddress),
            0,
            0,
            null,
            (new CallbackClient(pythonPort, getInetAddress(defaultAddress))),
            ServerSocketFactory.getDefault()
        );
    }

    private static InetAddress getInetAddress(String defaultAddress) {
        try {
            return InetAddress.getByName(defaultAddress);
        } catch (UnknownHostException e) {
            throw new Py4JNetworkException(e);
        }
    }
}
