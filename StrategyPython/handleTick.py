import sys
from hquant.trade import OrderManager

if __name__ == '__main__':
    address = sys.argv[1]
    port = sys.argv[2]
    strategy_id = sys.argv[3]
    order_manager = OrderManager(address, port, strategy_id)

