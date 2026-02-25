## Velocity Limit Service

This Java Spring Boot application is designed to manage real-time fund loading for customer accounts while enforcing specific velocity limits. The system processes incoming JSON transaction payloads to determine if a load attempt should be accepted or declined based on a customer's recent activity.

### Core functionality:
- Evaluates each fund load request against three distinct financial limits:
	- Daily velocity limit: A customer can load a maximum of $5,000 per day.
	- Weekly velocity limit: A customer can load a maximum of $20,000 per week.
	- Daily load attempt limit: A customer can attempt to load funds a maximum of 3 times per day.

### Project Structure & Classes
##### Config
- CurrencyDeserializer: Handles the conversion of currency string inputs (e.g., "$5000") into numeric formats for processing.
- LoadLimitProperties: Manages external configuration for velocity limits, such as daily/weekly maximums and transaction counts.

##### IO
- TransactionReader / TxtFileReaderComponent: Responsible for parsing incoming transaction data from text-based input sources.
- ResponseWriter / TxtFileWriterComponent: Handles the generation and formatting of output responses after transaction processing.

##### Model
- Payload: Represents the incoming transaction data (ID, Customer ID, Amount, Timestamp).
- Response: Encapsulates the processing result for a transaction (ID, Customer ID, Accepted/Rejected status).
- VelocityStats: Data model used to track a customer's cumulative totals and transaction counts over specific periods.

##### Repository
- VelocityRepository: Manages the persistence and retrieval of customer velocity data.

##### Service
- VelocityLimitEngine: The core logic component that evaluates whether a transaction adheres to defined velocity rules.
- VelocityStatsService: Manages the state and updates of customer transaction history and limits.
- LoadFundsManager: Orchestrates the high-level workflow of reading a transaction, validating it via the engine, and recording the result.

##### Application Root
- VelocityLimitsApplication: The main entry point for the Spring Boot application.
- VelocityLimitsRunner: A command-line runner that triggers the file processing logic upon application startup.

### Future Enhancements:
- For performance purposes (if it's a case in this project)
  - Add Cache in [VelocityStatsServiceImpl.java](src/main/java/ca/bank/velocitylimitsapp/service/VelocityStatsServiceImpl.java) to avoid extra calls to DB
  - Add batching when load attempts are inserted into DB