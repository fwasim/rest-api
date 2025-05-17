# Running the code
## Running Spring application 
1. Run `docker compose up` in the terminal to run the docker container containing the database
2. Run `./gradlew bootRun` in your terminal to run launch the Spring application

## Running the unit tests and integration tests
Run the command `./gradlew clean test jacocoTestReport --info` from the terminal

## Running the integration tests
In the terminal run the command `./gradlew test --tests "com.example.demo_springboot.BatteryServiceIntegrationTest" --info` to run the integration tests
