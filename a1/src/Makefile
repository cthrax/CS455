.SUFFIXES: .java .class
.java.class:
	javac $<

all: compile
all_javas := all.javas

# compile - Compile the source
.PHONY: compile

compile: $(all_javas)
	javac @$<

# all_javas - Gather source file list
.INTERMEDIATE: $(all_javas)

$(all_javas):
	find cdn -name '*.java' > $@

clean:
	rm **/*.class
