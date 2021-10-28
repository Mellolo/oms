#!/usr/bin/env python
#-*- coding:utf-8 -*-

#############################################
# File Name: setup.py
# Author: LuozhouLin
# Mail: zhumavip@163.com
# Created Time:  2021-10-28 01:12:10
#############################################

from setuptools import setup, find_packages

setup(
    name = "hquant",
    version = "0.0.2",
    keywords = ("quant", "hquant"),
    description = "A quant package",
    license = "apache 3.0",

    author = "LuozhouLin",
    author_email = "21921066@zju.edu.cn",

    packages = find_packages(),
    include_package_data = True,
    platforms = "any",
    install_requires = ["py4j == 0.10.9.2"]
)