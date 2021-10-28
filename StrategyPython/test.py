from strategies.s1.classname import echo
import sys
from hquant.strategy import Tick


def test():
    #echo()

    sys.path.append("strategies/s2")
    clsname = "classname"
    method = "echo"

    obj = __import__(clsname) # import module
    #c = getattr(obj,clsname)
    #obj = c() # new class
    print(obj)
    #obj.echo()
    mtd = getattr(obj,method)
    mtd() # call def

if __name__ == '__main__':
    tick = Tick()
    tick_str = sys.argv[1]
    for arg in tick_str.split("&"):
        field, value = arg.split("=")
        tick.__setattr__(field, value)
