from strategies.s1.classname import echo
import sys


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
    test()