JC = javac
JVM = java

.SUFFIXES: .java .class

.java.class:
	$(JC) singlePlayer/*.java multiplayer/*.java

default: .java.class

clean:
	$(RM) singlePlayer/*.class multiplayer/*.class *.log

MPserver:
	$(JVM) multiplayer.Server

MPclient:
	$(JVM) multiplayer.Client

SPserver:
	$(JVM) singlePlayer.Server

SPclient:
	$(JVM) singlePlayer.Client
