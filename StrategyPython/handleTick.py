import sys

from hquant.strategy import Tick
from hquant.trade import OrderManager
from hquant.util import dump_static, load_static
from strategy.strategy import Strategy

if __name__ == '__main__':
    address = sys.argv[1]
    port = int(sys.argv[2])
    strategy_id = sys.argv[3]
    order_manager = OrderManager(address, port, strategy_id)

    strategy = Strategy()
    strategy.set_order_manager(order_manager)

    load_static(strategy, "./strategy/save.json")

    tick = Tick()
    tick_str = sys.argv[4]
    for arg in tick_str.split("&"):
        field, value = arg.split("=")
        tick.__setattr__(field, value)

    strategy.handle_tick(tick)

    dump_static(strategy, "./strategy/save.json")


