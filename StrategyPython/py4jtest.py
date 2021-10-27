from py4j.java_gateway import JavaGateway, GatewayParameters

if __name__ == '__main__':
    gateway = JavaGateway(gateway_parameters=GatewayParameters(address="10.112.173.113"))
    # print(gateway.entry_point.matchTest("aaa", None))
    print(gateway.entry_point.getStrategy("522a18d7-cdf2-4a95-90f7-adcfd20831a1"))
