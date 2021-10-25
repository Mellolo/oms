# coding:utf-8
from ..trade.order import OrderManager


class Context:
    def __init__(self, order_manager: OrderManager):
        self.order_manager = order_manager
