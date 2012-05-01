#!/bin/bash

HADOOP_HOME=/usr/local/hadoop-0.23.1

if [ $1 == "start" ]; then 
    $HOME/cs455/scripts/start23.sh
elif [ $1 == "stop" ]; then 
    $HOME/cs455/scripts/stop23.sh
elif [ $1 == "format" ]; then 
    $HOME/cs455/scripts/nuke.sh
    $HOME/cs455/scripts/mdir.sh
    $HOME/cs455/scripts/format.sh
else
    $HADOOP_HOME/bin/hadoop $@
fi
