#!/bin/bash

tmpdir=bostwickRuddTurley

conf_dir=conf
slaves=`cat $conf_dir/slaves`

echo ${slaves[*]}
for host in ${slaves[*]}
do
   ssh $host rm /tmp/$tmpdir-data/current/VERSION
done
