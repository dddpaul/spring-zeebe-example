.PHONY: all build

all: build

build:
	@./gradlew clean bootJar

starter: build
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.threads=$(threads) --app.starter.count=$(count)

worker: build
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar --app.worker.enabled=true
