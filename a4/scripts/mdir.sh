#!/bin/bash
#   read slaves files from conf directory
#   and create hadoop local directories in /tmp
# IMPORTANT: run this script from project root directory ie /CS455/a4/
tmpdir=bostwickRuddTurley

conf_dir=conf
slaves=`cat $conf_dir/slaves`
user=`whoami`

echo ${slaves[*]}
for host in ${slaves[*]}
do
    ssh $host rm -Rf /tmp/$tmpdir-name /tmp/$tmpdir-data /tmp/$tmpdir-hadoop-logs /tmp/$tmpdir-local
    ssh $host mkdir /tmp/$tmpdir-name /tmp/$tmpdir-data /tmp/$tmpdir-hadoop-logs /tmp/$tmpdir-local
    ssh $host chmod -R 777 /tmp/$tmpdir-name /tmp/$tmpdir-data /tmp/$tmpdir-hadoop-logs /tmp/$tmpdir-local
done

rm -Rf /tmp/$tmpdir-name
rm -Rf /tmp/$tmpdir-data
rm -Rf /tmp/$tmpdir-hadoop-logs
rm -Rf /tmp/$tmpdir-local

mkdir /tmp/$tmpdir-name
mkdir /tmp/$tmpdir-data
mkdir /tmp/$tmpdir-hadoop-logs
mkdir /tmp/$tmpdir-local

chmod -R 777 /tmp/$tmpdir-name
chmod -R 777 /tmp/$tmpdir-data
chmod -R 777 /tmp/$tmpdir-hadoop-logs
chmod -R 777 /tmp/$tmpdir-local
