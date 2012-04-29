#!/bin/bash

HADOOP_HOME=/usr/local/hadoop-0.23.1
#Stop cluster
$HADOOP_HOME/sbin/stop-yarn.sh
$HADOOP_HOME/sbin/stop-dfs.sh
#Stop daemon
$HADOOP_HOME/sbin/yarn-daemon.sh stop nodemanager
$HADOOP_HOME/sbin/yarn-daemon.sh stop resourcemanager

read -p "Wait a moment..."
echo "======= antero ======="
jps
echo "======= garlic ======="
ssh garlic jps
