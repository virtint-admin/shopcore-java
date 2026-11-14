# Contributing

## Setup

You need JDK 21, Maven 3.9 and GNU make. Fetch dependencies once with `make deps`; after that the
build runs offline.

## Workflow

1. Fork the repository and branch from `main`.
2. Reproduce the bug with the failing test named in the ticket:
   `make test TESTS="com.shopcore.tax.TaxCalculatorTest#splitGst_oddPaiseToSgst"`
   (several IDs are separated by `;`).
3. Fix the code under `src/main/java`. Do not edit tests or build files: changes to `src/test/**`,
   `pom.xml`, `Makefile`, `.github/**`, `reports/**` or `CODEOWNERS` are rejected.
4. Run your issue's tests again and `make lint` before opening the pull request. `main` carries
   every open issue, so in a full `make test` run the tests named in other open issues fail. That
   is expected; your change must not break any other test.

## Conventions

- Money never passes through `double`: use `Money` (minor units) and `BigDecimal` with an explicit
  `RoundingMode`.
- Times are `Instant`s; calendar days are computed in an explicit `ZoneId`.
- Every public method has at least three tests in `<Class>Test`, named `<method>_<case>`, so a
  failing test points at one method. The index lives in `.virtint/functions.json`
  (`make functions` regenerates it).
- Formatting is google-java-format (`mvn spotless:apply`); Checkstyle rules are in
  `config/checkstyle.xml`.

## Grading outputs

`make test` and `make coverage` write `reports/junit/*.xml`, `reports/status.json` and
`reports/exit-code` (0 pass, 1 tests failed, 2 build or infrastructure error).
