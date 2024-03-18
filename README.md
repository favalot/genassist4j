# GenAssist4j
Java adaptation of Microsoft's AutoGen, facilitating the creation of Next-Gen Large Language Model Applications.

## 1. What is GenAssist4j?

GenAssist4j is a framework that enables the development of LLM applications using multiple agents that can converse with each other to solve tasks. These agents are customizable, conversable and seamlessly allow human participation. The agents can operate in various modes that employ combinations of LLMs, human inputs, and tools.

The following example can be found in the [genassist4j-example](genassist4j-example/src/main/java/com/favalot/genassist4j/example). 

## 2. Quickstart

### 2.1 Maven Repository
Prerequisites for building:
* Java 17 or later
* Unix-like environment (Linux, Mac OS X are recommended)
* Maven (version 3.8.6 recommended, requires at least 3.5.4)

### 2.2 Environment Setup
Using GenAssist4j requires OpenAI's APIs, you'll need to set the environment variable.

## 3. Multi-Agent Conversation Framework

GenAssist4j enables the next-gen LLM applications with a generic multi-agent conversation framework. Features of this use case include:

- **Multi-agent conversations**: GenAssist4j agents can communicate with each other to solve tasks. 
- **Customization**: GenAssist4j agents can be customized to meet specific application needs.
- **Human participation**: GenAssist4j seamlessly allows human participation. 

### 3.1 Auto Feedback From Code Execution Example
[Auto Feedback From Code Execution Example](genassist4j-example/src/main/java/com/favalot/genassist4j/example/AutoFeedbackFromCodeExecutionExample.java)

### 3.2 Group Chat Example
[Group Chat Example](genassist4j-example/src/main/java/com/favalot/genassist4j/example/GroupChatExample.java)

After running, you can check the file [group_chat_output.log](data/group_chat/group_chat_output.log) for the output logs.


## 4. Run Test Cases from Source

This project uses Spotless to format code. If you make any mo