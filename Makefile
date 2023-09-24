.PHONY: all build

all: build

build:
	@./gradlew clean bootJar

# Plain process without subprocess, dmn, timer and delay
starter:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with subprocess, dmn, timer and delay
starter-v2:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV2 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with subprocess, dmn and delay (without timer)
starter-v3:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV3 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

worker:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar --app.worker.enabled=true

worker-bar:
	@LOGGING_LEVEL_COM_GITHUB_DDDPAUL_ZEEBEEXAMPLE_WORKERS=OFF \
	java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.worker.enabled=true --app.worker.progress-bar.enabled=true
