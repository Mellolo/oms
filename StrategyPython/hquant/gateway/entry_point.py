# coding:utf-8
from py4j.java_gateway import JavaGateway, GatewayParameters


def get_running_strategy(address, port, id):
    gateway = JavaGateway(gateway_parameters=GatewayParameters(address=address, port=port))
    return gateway.entry_point.get(id)
