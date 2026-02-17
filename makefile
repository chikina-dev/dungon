.PHONY: infra infra-down push build dev

infra:
	cd infra && docker compose up -d

infra-down:
	cd infra && docker compose down

push:
	cp target/dungeon-0.1.0.jar ./infra/plugins/

build:
	mvn clean package

log:
	cd infra && docker compose logs -f minecraft

dev: build push infra

fmt:
	ktlint --editorconfig=.editorconfig --format src/main/kotlin/**/*.kt

lint:
	ktlint --editorconfig=.editorconfig src/main/kotlin/**/*.kt