import sys
import os

if __name__ == '__main__':
    runningId = sys.argv[1]
    sys.path.append(os.getcwd()+"/StrategyPython/strategies/"+runningId)
    module = __import__("strategy")
    handleTick = getattr(module, "handleTick")

    param = sys.argv[2]
    handleTick(param)