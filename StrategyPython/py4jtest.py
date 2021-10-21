from py4j.java_gateway import JavaGateway, GatewayParameters

if __name__ == '__main__':
    gateway = JavaGateway(gateway_parameters=GatewayParameters(address="10.112.173.179"))
    print(gateway.entry_point.matchTest("", "target"))
