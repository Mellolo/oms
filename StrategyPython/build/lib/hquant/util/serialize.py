# coding:utf-8
import os
import pickle
from inspect import isfunction


def dump_static(strategy, file_path: str):
    static_dict = dict(strategy.__class__.__dict__)
    d = dict()
    for field in static_dict.keys():
        if not isfunction(static_dict[field]) and not field.startswith("__") and not field.endswith("__"):
            d[field] = static_dict[field]
    f = open(file_path, 'wb')
    pickle.dump(d, f)


def load_static(strategy, file_path: str):
    if os.path.exists(file_path):
        f = open(file_path, 'rb')
        d = dict(pickle.load(f))
        for field in d.keys():
            setattr(strategy.__class__, field, d[field])
    return strategy
