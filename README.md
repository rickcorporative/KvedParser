How to run tests:

Download project from git.

- Run test from command line with maven

1. Go to project directory
2. Run command line(windows) or terminal(mac os)
3. Type command:
    # Run all tests:
    - mvn clean test

    # Run a single test case.
    - mvn clean test -Dcucumber.filter.tags=@TestID
    
    # Run tests in a specific number of threads
    - mvn clean test -Dcucumber.filter.tags=@TestTag -Ddataproviderthreadcount=NumberOfThreads

    Run allure report:
    - allure serve allure-results