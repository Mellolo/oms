import sys
from hquant.gateway import get_running_strategy
from hquant.trade import OrderManager
from hquant.api.context import Context

if __name__ == '__main__':
    address = sys.argv[1]
    port = sys.argv[2]
    id = sys.argv[3]
    order_manager = OrderManager(get_running_strategy(address, port, id))
    context = Context(order_manager)

