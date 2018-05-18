JC = javac
J = java

.SUFFIXES: .java .class

.java.class:
	$(JC) *.java

default: .java.class

clean:
	$(RM) *.class *.log

server:
	$(J) Server

client:
	$(J) Client
