# coding:utf-8
from ..decorator import singleton
from ..util.throwable import ConstError, ConstCaseError

from py4j.java_gateway import JavaGateway, GatewayParameters

CONST_FIELDS = ('RUNNING_STRATEGY', )


@singleton
class OrderManager:

    def __setattr__(self, key, value):
        if key in self.__dict__.keys() and key in CONST_FIELDS:
            raise ConstError("Can't change a const variable: '%s'" % key)
        if not key.isupper() and key in CONST_FIELDS:
            raise ConstCaseError("Const variable must be combined with upper letters:'%s'" % key)
        self.__dict__[key] = value

    def __init__(self, address: str, port: int, strategy_id: str):
        gateway = JavaGateway(gateway_parameters=GatewayParameters(address=address, port=port))
        self.RUNNING_STRATEGY = gateway.entry_point.getStrategy(strategy_id)

    def buy(self, index: int, security: str, volume: int):
        return self.RUNNING_STRATEGY.buy(index, security, volume)

    def sell(self, index: int, security: str, volume: int):
        return self.RUNNING_STRATEGY.sell(index, security, volume)

    def get_position(self, index: int, security: str):
        return self.RUNNING_STRATEGY.sell(index, security)

    def subscribe(self, security: str):
        return self.RUNNING_STRATEGY.subscribe(security)
