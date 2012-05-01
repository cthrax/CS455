JARPATH=/usr/local/hadoop-0.23.1/share/hadoop/common/hadoop-common-0.23.1.jar:/usr/local/hadoop-0.23.1/share/hadoop/mapreduce/*:/usr/local/hadoop-0.23.1/share/hadoop/common/lib/*:/usr/local/hadoop-0.23.1/share/hadoop/hdfs/*:./bin/

JFLAGS = -classpath ${JARPATH}
JC = javac

Deleter: 
	$(JC) $(JFLAGS) -d bin/ src/cs455/deleter/Deleter.java

Statistic: 
	$(JC) $(JFLAGS) -d bin/ src/cs455/statistics/Statistic.java

default: classes

classes: Statistic

clean:
	$(RM) ../bin/cs455/deletion/*.class ../bin/cs455/statistics/*.class ../bin/*.jar

del:
	java -classpath ${JARPATH} cs455.deleter.Deleter I am a a tree

stat:
	java -classpath ${JARPATH} cs455.statistics.Statistic hdfs://antero/books/9973.txt hdfs://antero/probabilities/test.out

jar:
	jar -cvf cs455.deleter.jar *.class
