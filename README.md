# ShopCore (Java)

> **Learners:** start with [LEARNERS.md](LEARNERS.md), how to pick an issue, fix it and get it checked.

ShopCore is the back-office engine of a fictional Indian online store: money and FX, the product
catalogue, inventory across warehouses, pricing and coupons, GST and VAT, carts, the order
lifecycle, shipping rates and delivery promises, a double-entry ledger and sales reports.

It is a plain Java 21 library with no runtime dependencies. Tests use JUnit 5.

```
make build       # compile
make test        # run all tests -> reports/junit/*.xml
make lint        # Checkstyle + Spotless -> reports/lint.json
make coverage    # tests + JaCoCo -> reports/coverage.xml (Cobertura)
```

`main` carries every open issue: in a full `make test` run the tests named in open issues fail
until someone fixes them, and everything else passes.

Run `make deps` once with network access; every other target then works offline (`mvn -o`).
See [CONTRIBUTING.md](CONTRIBUTING.md) for the workflow and [ARCHITECTURE.md](ARCHITECTURE.md) for
the module map.
