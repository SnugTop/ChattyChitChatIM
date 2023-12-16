build:
	mvn package

run-server: build
	mvn exec:java -Dexec.mainClass="me.snugtop.server.ChattyChatChatServer" -Dexec.args="8080"

run-client: build
	mvn exec:java -Dexec.mainClass="me.snugtop.client.ChattyChatChatClient" -Dexec.args="localhost 8080"
