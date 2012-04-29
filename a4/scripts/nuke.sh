#!/bin/bash
#   read slaves files from conf directory
#   and create hadoop local directories in /tmp

if [ $# -eq 1 ]; then
    conf_dir=$1
else
    conf_dir=$HOME/cs455/mapreduce/hadoop
fi

slaves=`cat $conf_dir/slaves`
user=`whoami`

echo ${slaves[*]}
for host in ${slaves[*]}
do
    ssh $host rm -rf /tmp/$user
#    ssh $host chmod 755 /tmp/$user /tmp/$user/hadoop /tmp/$user/hadoop/logs /tmp/$user/hadoop/hdfs
done
