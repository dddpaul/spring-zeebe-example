.PHONY: all build

all: build

build:
	@./gradlew clean bootJar

# Plain process without subprocess, dmn, timer and delay
starter:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with subprocess, dmn, timer and delay
starter-v2:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV2 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with dmn, timer and delay, all inside call activity
starter-v2_1:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV2_1 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with dmn, timer and delay, prepended with empty service task
starter-v2_2:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV2_2 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with dmn, timer and delay, prepended with gateway
starter-v2_3:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV2_3 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with dmn, timer and delay, prepended with real service task to set timeout
starter-v2_4:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV2_4 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with dmn, timer and delay, prepended with REST connector to set timeout
starter-v2_5:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV2_5 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with start timer event
starter-v3:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV3 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with custom connector
starter-v4:
	@CONNECTOR_TELEGRAM_OUTBOUND_TYPE=non-existent-type-1 \
	CONNECTOR_MYCONNECTOR_TYPE=non-existent-type-2 \
	java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV4 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with bundled REST connector
starter-v4_1:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV4_1 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

# Process with dmn, timer and delay, prepended with wait message
starter-v5:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.starter.enabled=true --spring.main.web-application-type=none \
	--app.starter.process=CarInsuranceApplicationProcessV5 \
	--app.starter.threads=$(threads) --app.starter.count=$(count) --app.starter.random=$(random)

worker:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar --app.worker.enabled=true

worker-loom:
	@java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar --app.worker.enabled=true --app.worker.virtual-thread-pool.enabled=true

worker-bar:
	@LOGGING_LEVEL_COM_GITHUB_DDDPAUL_ZEEBEEXAMPLE_WORKERS=OFF \
	java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.worker.enabled=true --app.worker.progress-bar.enabled=true

worker-bar-loom:
	@LOGGING_LEVEL_COM_GITHUB_DDDPAUL_ZEEBEEXAMPLE_WORKERS=OFF \
	java -jar build/libs/spring-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.worker.enabled=true --app.worker.virtual-thread-pool.enabled=true --app.worker.progress-bar.enabled=true

http:
	@cd node_http; ../node_modules/.bin/http-live -p 10000

http-delay:
	@cd node_http; ../node_modules/.bin/http-live -p 10000 -m 1000 -x 5000
