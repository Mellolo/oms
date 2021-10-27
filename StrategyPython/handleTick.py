import sys

from hquant.strategy import Tick
from hquant.trade import OrderManager
from hquant.util import dump_strategy, load_strategy
from strategy.strategy import Strategy

if __name__ == '__main__':
    address = sys.argv[1]
    port = int(sys.argv[2])
    strategy_id = sys.argv[3]
    order_manager = OrderManager(address, port, strategy_id)

    strategy = Strategy()
    strategy.set_order_manager(order_manager)

    load_strategy(strategy, "./strategy/save.json")

    tick = Tick()
    tick.open = sys.argv[4]
    tick.close = sys.argv[5]

    strategy.handle_tick(tick)

    dump_strategy(strategy, "./strategy/save.json")


