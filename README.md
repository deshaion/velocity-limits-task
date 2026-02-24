## Velocity Limit Service

This Java Spring Boot application is designed to manage real-time fund loading for customer accounts while enforcing specific velocity limits. The system processes incoming JSON transaction payloads to determine if a load attempt should be accepted or declined based on a customer's recent activity.

### Core functionality:
- Evaluates each fund load request against three distinct financial limits:
	- Daily velocity limit: A customer can load a maximum of $5,000 per day.
	- Weekly velocity limit: A customer can load a maximum of $20,000 per week.
	- Daily load attempt limit: A customer can attempt to load funds a maximum of 3 times per day.

### Implementation


### Future Enhancements:
