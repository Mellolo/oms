# coding:utf-8
from ..throwable import ConstError, ConstCaseError, DefineError
from ..trade.order import OrderManager


class BaseStrategy:
    final_methods = ['buy', 'sell', 'get_position']
    const_fields = ['order_manager']

    def __init__(self, order_manager: OrderManager):
        self.order_manager = order_manager

    def __setattr__(self, key, value):
        if key not in BaseStrategy.const_fields:
            raise DefineError("Can't define a variable: '%s'" % key)
        if key in self.__dict__.keys() and key in BaseStrategy.const_fields:
            raise ConstError("Can't change a const variable: '%s'" % key)
        if not key.isupper():
            raise ConstCaseError("Const variable must be combined with upper letters:'%s'" % key)
        self.__dict__[key] = value

    def __new__(cls, *args, **kwargs):
        if cls != BaseStrategy:
            for method in BaseStrategy.final_methods:
                if method in cls.__dict__.keys():
                    raise Exception("method '{}' cannot be rewritten.".format(method))
        return super(BaseStrategy, cls).__new__(cls, *args, **kwargs)

    def buy(self, index: int, security: str, volume: int):
        return self.order_manager.buy(index, security, volume)

    def sell(self, index: int, security: str, volume: int):
        return self.order_manager.sell(index, security, volume)

    def get_position(self, index: int, security: str):
        return self.order_manager.get_position(index, security)
