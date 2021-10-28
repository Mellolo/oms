from hquant import *


class Strategy(BaseStrategy):
    def initialize(self):
        self.subscribe("600002")
        self.subscribe("600003")

    def handle_tick(self, tick):
        if tick.open < tick.close:
            self.buy(0, '600002', 200)
        elif tick.open > tick.close:
            self.sell(0, '600003', 300)
        Strategy.last_tick = tick
