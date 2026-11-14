# ShopCore (Java) — grading interface, see CONTRIBUTING.md.
#
#   make build                      compile main sources            (mvn -o -q compile)
#   make test                       full suite -> reports/junit/*.xml
#   make test TESTS="a#b;c#d"       only these test IDs (com.shopcore.tax.RulesTest#method)
#   make lint                       Checkstyle + Spotless -> reports/lint.json
#   make coverage                   full suite + JaCoCo -> reports/coverage.xml (Cobertura)
#   make deps [M2_REPO=dir]         fetch every plugin/dependency so the targets above run offline
#   make functions                  regenerate .virtint/functions.json
#
# Every target exits 0 when it passes and non-zero otherwise (GNU make turns a failed recipe into
# exit 2). `make test` and `make coverage` also write the runner's own exit code (0 pass, 1 tests
# failed, >=2 infra/build error) to reports/exit-code. The grader reads the outcome from
# reports/exit-code, reports/junit/*.xml and reports/status.json:
#   {"phase": "build"|"test"|"coverage", "result": "pass"|"tests_failed"|"error",
#    "tests_total": N, "tests_failed": N}

SHELL       := /bin/bash
MVN         ?= mvn
JAVA        ?= java
M2_REPO     ?= $(HOME)/.m2/repository
TESTS       ?=
MIN_COVERAGE ?= 0.90

MVN_COMMON  := -B -Dstyle.color=never -Dmaven.repo.local=$(M2_REPO)
MVN_OFFLINE := $(MVN) $(MVN_COMMON) -o -q
TOOLS       := $(JAVA) -XX:-UsePerfData scripts/Tools.java

# "a#b;c#d" -> "a#b,c#d" for surefire -Dtest.
comma       := ,
TEST_FILTER := $(strip $(subst ;,$(comma),$(TESTS)))
ifneq ($(TEST_FILTER),)
  TEST_ARG := -Dtest='$(TEST_FILTER)' -Dsurefire.failIfNoSpecifiedTests=true
endif

.PHONY: all build test lint coverage deps functions functions-check check clean help

all: build test

help:
	@sed -n '2,12p' Makefile

STATUS      := reports/status.json
# Runner exit code (0 pass, 1 tests failed, >=2 infra/build error); GNU make itself always exits
# 2 on a failed recipe, so the grader reads this file instead of make's exit status.
EXIT_FILE   := reports/exit-code

build:
	@mkdir -p reports && rm -f $(STATUS)
	@$(MVN_OFFLINE) compile \
	  || { $(TOOLS) status $(STATUS) build error; echo "make build: compile error" >&2; exit 2; }
	@$(TOOLS) status $(STATUS) build pass

test:
	@rm -rf reports/junit $(STATUS) $(EXIT_FILE) && mkdir -p reports/junit
	@rc=0; $(MVN_OFFLINE) test -Dmaven.test.failure.ignore=true $(TEST_ARG) || rc=2; \
	  if [ $$rc -eq 0 ]; then $(TOOLS) junit-status reports/junit $(STATUS) test; rc=$$?; \
	  else $(TOOLS) status $(STATUS) test error; echo "make test: build or infrastructure error" >&2; fi; \
	  echo $$rc > $(EXIT_FILE); exit $$rc

coverage:
	@rm -rf reports/junit reports/coverage.xml $(STATUS) $(EXIT_FILE) target/jacoco.exec target/site/jacoco && mkdir -p reports/junit
	@rc=0; $(MVN_OFFLINE) -Pcoverage test -Dmaven.test.failure.ignore=true || rc=2; \
	  if [ $$rc -eq 0 ]; then $(TOOLS) cobertura target/site/jacoco/jacoco.xml reports/coverage.xml || rc=2; fi; \
	  if [ $$rc -eq 0 ]; then $(TOOLS) junit-status reports/junit $(STATUS) coverage; rc=$$?; \
	  else $(TOOLS) status $(STATUS) coverage error; echo "make coverage: build or infrastructure error" >&2; fi; \
	  echo $$rc > $(EXIT_FILE); exit $$rc

lint:
	@mkdir -p reports target
	@rm -f reports/lint.json target/checkstyle-result.xml
	@$(MVN_OFFLINE) checkstyle:check || { echo "make lint: checkstyle crashed" >&2; exit 2; }
	@$(MVN_OFFLINE) spotless:check > target/spotless.log 2>&1; \
	  $(TOOLS) lint target/checkstyle-result.xml target/spotless.log $$? reports/lint.json

# Online: resolve everything the offline targets need (plugins resolve some artifacts lazily,
# so go-offline alone is not enough; the warm-up run touches every goal once).
deps:
	$(MVN) $(MVN_COMMON) -q dependency:go-offline -Pcoverage
	$(MVN) $(MVN_COMMON) -q -Pcoverage test -Dmaven.test.failure.ignore=true
	$(MVN) $(MVN_COMMON) -q test -Dtest='com.shopcore.money.MoneyTest#plus_*' -Dsurefire.failIfNoSpecifiedTests=false
	$(MVN) $(MVN_COMMON) -q checkstyle:check spotless:check || true
	$(MVN) $(MVN_COMMON) -q clean

functions:
	$(TOOLS) functions . .virtint/functions.json

functions-check:
	$(TOOLS) functions-check . .virtint/functions.json

# Everything CI runs.
check: build lint test coverage functions-check
	$(TOOLS) coverage-min reports/coverage.xml $(MIN_COVERAGE)
	@if grep -q '"path"' reports/lint.json; then echo "lint errors, see reports/lint.json" >&2; exit 1; fi

clean:
	rm -rf target reports
