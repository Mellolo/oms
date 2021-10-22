from py4j.java_gateway import JavaGateway, GatewayParameters

if __name__ == '__main__':
    gateway = JavaGateway(gateway_parameters=GatewayParameters(address="10.112.173.221"))
    # print(gateway.entry_point.matchTest("aaa", None))
    print(gateway.entry_point.getStrategy("77c1a69e-d45c-4d70-bb48-a339e42586f6").buy(0,'61',200))
