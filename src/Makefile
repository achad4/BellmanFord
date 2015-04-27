JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Host.java \
	DistanceVector.java \
	Cost.java \
	BFClient.java \
	Node.java \
	NetworkMessage.java \

default: classes

classes: $(CLASSES:.java=.class)
clean:
	$(RM) *.class