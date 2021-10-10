from py4j.java_gateway import JavaGateway

def handleTick(s):
    gateway = JavaGateway()
    print(gateway.entry_point.matchTest("raw", "target"))
    print(s)