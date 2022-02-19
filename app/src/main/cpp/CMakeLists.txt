cmake_minimum_required(VERSION 3.18.1)
project("scany")

add_library(yuv-rgb-convert SHARED yuv-rgb-convert.cpp)
find_library(log-lib log)
target_link_libraries(yuv-rgb-convert ${log-lib})
