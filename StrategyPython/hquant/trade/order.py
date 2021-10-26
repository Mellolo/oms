# coding:utf-8
from ..throwable import ConstError, ConstCaseError, DefineError
from py4j.java_gateway import JavaGateway, GatewayParameters


class OrderManager:
    final_methods = ['buy', 'sell', 'get_position']
    const_fields = ['running_strategy']

    def __setattr__(self, key, value):
        if key not in OrderManager.const_fields:
            raise DefineError("Can't define a variable: '%s'" % key)
        if key in self.__dict__.keys() and key in OrderManager.const_fields:
            raise ConstError("Can't change a const variable: '%s'" % key)
        if not key.isupper():
            raise ConstCaseError("Const variable must be combined with upper letters:'%s'" % key)
        self.__dict__[key] = value

    def __new__(cls, *args, **kwargs):
        if cls != OrderManager:
            for method in OrderManager.final_methods:
                if method in cls.__dict__.keys():
                    raise Exception("method '{}' cannot be rewritten.".format(method))
        return super(OrderManager, cls).__new__(cls, *args, **kwargs)

    def __init__(self, address, port, strategy_id):
        gateway = JavaGateway(gateway_parameters=GatewayParameters(address=address, port=port))
        self.running_strategy = gateway.entry_point.getStrategy(strategy_id)

    def buy(self, index: int, security: str, volume: int):
        return self.running_strategy.buy(index, security, volume)

    def sell(self, index: int, security: str, volume: int):
        return self.running_strategy.sell(index, security, volume)

    def get_position(self, index: int, security: str):
        return self.running_strategy.sell(index, security)
