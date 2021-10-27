from hquant import *


class Strategy(BaseStrategy):
    def handle_tick(self, tick):
        if tick.open < tick.close:
            self.buy(0, '600002', 200)
        elif tick.open > tick.close:
            self.sell(0, '600003', 300)
        Strategy.last_tick = tick
