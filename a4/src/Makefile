JARPATH=/Library/hadoop/hadoop-0.23.1/share/hadoop/common/hadoop-common-0.23.1.jar:/Library/hadoop/hadoop-0.23.1/share/hadoop/mapreduce/*
JFLAGS = -classpath ${JARPATH} -d ../bin
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Deleter.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) ../bin/*.class ../bin/*.jar


jar:
	jar -cvf cs455.deleter.jar *.class
