JARPATH=/usr/local/hadoop-0.23.1/share/hadoop/common/hadoop-common-0.23.1.jar:/usr/local/hadoop-0.23.1/share/hadoop/mapreduce/*:/usr/local/hadoop-0.23.1/share/hadoop/common/lib/*:/usr/local/hadoop-0.23.1/share/hadoop/hdfs/*:./bin/

JFLAGS = -classpath ${JARPATH}
JC = javac

all: default

Deleter: 
	$(JC) $(JFLAGS) -d bin/ src/cs455/deleter/Deleter.java

Statistic: 
	$(JC) $(JFLAGS) -d bin/ src/cs455/statistics/Statistic.java
	
WordCount:
	$(JC) $(JFLAGS) -d bin/ src/org/myorg/WordCount.java

default: classes

classes: Statistic Deleter 

clean:
	$(RM) bin/cs455/deletion/*.class bin/cs455/statistics/*.class *.jar bin/org/myorg/*.class

clean-hdfs:
	scripts/hadoop23.sh fs -rm -f -r /user/turley/probabilities

del:
	/usr/local/hadoop-0.23.1/bin/hadoop jar a4.jar cs455.deleter.Deleter /books/* probabilities/

stat:
	/usr/local/hadoop-0.23.1/bin/hadoop jar a4.jar cs455.statistics.Statistic /books probabilities/

count:
	/usr/local/hadoop-0.23.1/bin/hadoop jar a4.jar org.myorg.WordCount /books/* wordout

jar:
	scripts/makejar.sh
