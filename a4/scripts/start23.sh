#!/bin/bash

HADOOP_HOME=/usr/local/hadoop-0.23.1
#Start daemon
$HADOOP_HOME/sbin/yarn-daemon.sh start resourcemanager
$HADOOP_HOME/sbin/yarn-daemon.sh start nodemanager

#Start cluster
$HADOOP_HOME/sbin/start-dfs.sh
$HADOOP_HOME/sbin/start-yarn.sh
read -p "Wait a moment..."
echo ======= antero ===========
jps
echo ======= garlic ===========
ssh garlic jps
read -p "Show nodemanager log file (y/n)?"
["$REPLY" == "y"] || less +G /tmp/turley/hadoop/logs/yarn-turley-nodemanager-antero.log
read -p "Show resourcemanager log file (y/n)?"
["$REPLY" == "y"] || less +G /tmp/turley/hadoop/logs/yarn-turley-resourcemanager-antero.log
