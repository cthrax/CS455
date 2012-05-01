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
	/usr/local/hadoop-0.23.1/bin/hadoop jar a4.jar cs455.statistics.Statistic /books/* /probabilities/

count:
	/usr/local/hadoop-0.23.1/bin/hadoop jar bin/wordcount.jar org.myorg.WordCount /books/* /myout

jar:
	cd bin
	jar -cvf a4.jar bin/
	mv a4.jar ..
	cd ..
