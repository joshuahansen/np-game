JC = javac
JVM = java

.SUFFIXES: .java .class

.java.class:
	$(JC) singlePlayer/*.java multiplayer/*.java

PACKAGES := \
	. \
	singlePlayer \
	multiplayer \

CLASSES := $(shell find $(PACKAGES) -type f -name '*.java')

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) singlePlayer/*.class singlePlayer/*.log multiplayer/*.class multiplayer/*.log

MPserver:
	$(JVM) multiplayer.Server

MPclient:
	$(JVM) multiplayer.Client

SPserver:
	$(JVM) singlePlayer.Server

SPclient:
	$(JVM) singlePlayer.Client
