# coding:utf-8

class OrderManager:
    def __init__(self, running_strategy):
        self.running_strategy = running_strategy
        OrderManager.a = 1

    def buy(self, index: int, security: str, volume: int):
        return self.running_strategy.buy(index, security, volume)

    def sell(self, index: int, security: str, volume: int):
        return self.running_strategy.sell(index, security, volume)

    def get_position(self, index: int, security: str):
        return self.running_strategy.sell(index, security)
