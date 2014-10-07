#
# makefile for compiling java classes
#

# define output directory relative to the root of the project
OUTPUT_DIR = bin

# define a makefile variable for the java compiler
#
JCC = javac
JARCC = jar

# define a makefile variable for compilation flags
# the -g flag compiles with debugging information
#
JFLAGS = -g -d ${OUTPUT_DIR}
JARFLAGS = cvfm

# typing 'make' will invoke the first target entry in the makefile 
# (the default one in this case)
#
default: WebServer.class
all: WebServer.class WebServer.jar

# Target entries for building the class and jar files as necessary
#
$(OUTPUT_DIR):
	mkdir $(OUTPUT_DIR)

WebServer.class: WebServer.java $(OUTPUT_DIR)
	$(JCC) $(JFLAGS) WebServer.java

WebServer.mf: $(OUTPUT_DIR)
	echo "Manifest-Version: 1.0" > $(OUTPUT_DIR)/WebServer.mf
	echo "Main-Class: WebServer" >> $(OUTPUT_DIR)/WebServer.mf

WebServer.jar: WebServer.class WebServer.mf $(OUTPUT_DIR)
	cd $(OUTPUT_DIR); $(JARCC) $(JARFLAGS) WebServer.jar WebServer.mf WebServer.class openLinkButtonAction.class
	$(RM) $(OUTPUT_DIR)/WebServer.mf

binary: WebServer.jar
	$(RM) $(OUTPUT_DIR)/WebServer.class
	$(RM) $(OUTPUT_DIR)/openLinkButtonAction.class

test: WebServer.class
	cd $(OUTPUT_DIR); java WebServer &

run: WebServer.jar
	cd $(OUTPUT_DIR); java -jar WebServer.jar &

# To start over from scratch, type 'make clean'.  
# Deletes the output directory so that a future 'make' will rebuild everything
#
clean:
	$(RM) -r $(OUTPUT_DIR)

# A help target to view all possible targets
#
help:
	@echo ""
	@echo "    default:   Builds the WebServer class"
	@echo "    all:       Builds the WebServer class and a JAR file for it"
	@echo "    test:      Test the WebServer class"
	@echo "    run:       Run the WebServer program by compiling it into a JAR file"
	@echo "    help:      View this menu"
	@echo ""