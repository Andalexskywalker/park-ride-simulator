---
description: How to initialize and run the PRR Simulator ecosystem
---

To get the entire system running (7 microservices + Frontend + PostgreSQL), follow these steps:

### 1. Build the Backend (Optional but Recommended)
Verifies that all services compile correctly before building Docker images.
```bash
mvn clean install -DskipTests
```

### 2. Launch with Docker Compose
This will start the database, discovery server, gateway, all microservices, and the frontend.
> [!NOTE]
> The first run might take a while as it downloads images and compiles the services.
```bash
docker-compose up --build
```

### 3. Access the System
Once all containers are healthy (check with `docker ps`):

- **Frontend Dashboard**: [http://localhost:3001](http://localhost:3001)
- **API Gateway**: [http://localhost:8080](http://localhost:8080)
- **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)

### 4. Troubleshooting
If the frontend cannot connect to the backend:
- Ensure `postgres-db` is healthy.
- Ensure `eureka-server` is up.
- Ensure all services are registered in Eureka (check [http://localhost:8761](http://localhost:8761)).
- If you see `404 Not Found` on API calls, check the API Gateway logs.

### 5. Initial Setup (Data Seeding)
Since the database starts empty:
1. Go to the Frontend [Register Page](http://localhost:3001/register).
2. Create a new user account.
3. Login to get your JWT token.
4. Use the dashboard to see Parks and Sessions.
