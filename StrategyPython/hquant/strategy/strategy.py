# coding:utf-8
from ..util.throwable import ConstError, ConstCaseError
from ..trade.order import OrderManager

FINAL_METHODS = ('buy', 'sell', 'get_position', 'set_order_manager')
CONST_FIELDS = ('ORDER_MANAGER', )


class BaseStrategy:
    def __setattr__(self, key, value):
        if key in self.__dict__.keys() and key in CONST_FIELDS:
            raise ConstError("Can't change a const variable: '%s'" % key)
        if not key.isupper() and key in CONST_FIELDS:
            raise ConstCaseError("Const variable must be combined with upper letters:'%s'" % key)
        self.__dict__[key] = value

    def __new__(cls, *args, **kwargs):
        if cls != BaseStrategy:
            for method in FINAL_METHODS:
                if method in cls.__dict__.keys():
                    raise Exception("method '{}' cannot be rewritten.".format(method))
        return super(BaseStrategy, cls).__new__(cls, *args, **kwargs)

    def set_order_manager(self, order_manager: OrderManager):
        self.ORDER_MANAGER = order_manager

    def buy(self, index: int, security: str, volume: int):
        return self.ORDER_MANAGER.buy(index, security, volume)

    def sell(self, index: int, security: str, volume: int):
        return self.ORDER_MANAGER.sell(index, security, volume)

    def get_position(self, index: int, security: str):
        return self.ORDER_MANAGER.get_position(index, security)
