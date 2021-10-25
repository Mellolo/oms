# coding:utf-8
from .context import Context


def buy(context: Context, index: int, security: str, volume: int):
    return context.order_manager.buy(index, security, volume)


def sell(context: Context, index: int, security: str, volume: int):
    return context.order_manager.sell(index, security, volume)


def get_position(context: Context, index: int, security: str):
    return context.order_manager.get_position(index, security)
