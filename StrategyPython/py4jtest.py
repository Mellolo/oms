from py4j.java_gateway import JavaGateway

if __name__ == '__main__':
    gateway = JavaGateway()
    print(gateway.entry_point.matchTest("raw", "target"))

    # "Sends" python object to the Java side.
    # numbers = operator_example.randomBinaryOperator(operator)