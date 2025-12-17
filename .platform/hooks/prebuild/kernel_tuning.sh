#!/bin/bash
set -e

# 포트 범위 늘리기 (덤으로 ulimit도 증가함)
echo "10240 65535" > /proc/sys/net/ipv4/ip_local_port_range

# 톰캣 디폴트 max-connections 에 맞추기 위해 늘림
# sudo sysctl -w net.core.somaxconn=8192
# sudo sysctl -w net.ipv4.tcp_max_syn_backlog=8192