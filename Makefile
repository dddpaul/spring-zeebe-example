.PHONY: all build

all: build

build:
	@./gradlew clean bootJar

starter:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

worker:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar --app.worker.enabled=true

worker-bar:
	@LOGGING_LEVEL_COM_GITHUB_DDDPAUL_ZEEBEEXAMPLE_WORKERS=WARN \
	java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.worker.enabled=true --app.worker.progress-bar.enabled=true
