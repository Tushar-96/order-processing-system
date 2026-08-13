# Event-Driven Order Processing System

A full-stack microservices application developed by Tushar and Tejas. Users can register, browse products, maintain account-specific carts, place orders, track order status, and cancel confirmed orders.

Order processing and inventory reservation communicate asynchronously through Apache Kafka.

## Architecture

```text
React Frontend
      |
      v
API Gateway :8080
      |
      +----------> Auth Service :8081 ------> Auth PostgreSQL :5433
      |
      +----------> Order Service :8082 -----> Order PostgreSQL :5434
      |                    |
      |                    v
      |               Apache Kafka :9092
      |                    |
      +----------> Inventory Service :8083 -> Inventory PostgreSQL :5435
```

## Main Features

- User registration and login
- BCrypt password hashing
- JWT authentication and session restoration
- API Gateway routing, CORS, circuit breakers, and fallbacks
- Product catalog and stock management
- Account-specific persistent shopping carts
- Order placement using product IDs and quantities
- Kafka-based inventory reservation
- Order statuses: `PENDING`, `CONFIRMED`, `INVENTORY_REJECTED`, `CANCELLED`, `SHIPPED`, and `DELIVERED`
- Stock restoration when a confirmed order is cancelled
- Idempotent Kafka event processing
- Structured API and frontend error handling

## Technology Stack

### Backend

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring Cloud Gateway
- Apache Kafka
- PostgreSQL
- Maven
- Testcontainers

### Frontend

- React
- Vite
- React Router
- Axios
- CSS

### Infrastructure

- Docker Compose
- GitHub
- GitHub Desktop

## Project Structure

```text
backend/
├── api-gateway/
├── auth-service/
├── order-service/
└── inventory-service/

frontend/
docker-compose.yml
.env.example
README.md
```

## Service Ports

| Component | Port |
|---|---:|
| React frontend | 5173 |
| API Gateway | 8080 |
| Auth Service | 8081 |
| Order Service | 8082 |
| Inventory Service | 8083 |
| Apache Kafka | 9092 |
| Auth PostgreSQL | 5433 |
| Order PostgreSQL | 5434 |
| Inventory PostgreSQL | 5435 |

## Prerequisites

Install:

- Java 21 or the version configured in the Maven projects
- Maven, or use the included Maven wrappers
- Node.js and npm
- Docker Desktop
- Git

## Environment Setup

Copy the example configuration:

```powershell
Copy-Item .env.example .env
Copy-Item frontend/.env.example frontend/.env
```

Set a secure JWT secret in the current PowerShell terminal:

```powershell
$jwtBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($jwtBytes)
$rng.Dispose()

$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
```

The Auth Service and Order Service must use the same JWT secret.

Frontend configuration:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Never commit real `.env` files or production secrets.

## Start Infrastructure

From the repository root:

```powershell
docker compose up -d
docker compose ps
```

This starts:

- Three PostgreSQL databases
- Apache Kafka

## Start Backend Services

Open a separate VS Code terminal for each service.

### Auth Service

```powershell
cd backend/auth-service
mvn spring-boot:run
```

### Order Service

```powershell
cd backend/order-service
mvn spring-boot:run
```

### Inventory Service

```powershell
cd backend/inventory-service
mvn spring-boot:run
```

### API Gateway

```powershell
cd backend/api-gateway
mvn spring-boot:run
```

## Start the Frontend

```powershell
cd frontend
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

All frontend API requests should go through:

```text
http://localhost:8080
```

## Main API Endpoints

### Authentication

| Method | Endpoint | Authentication |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |
| GET | `/api/v1/auth/me` | Bearer token |

### Products

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/products` | List products |
| GET | `/api/v1/products/{productId}` | Get a product |
| POST | `/api/v1/products` | Create a product |
| PUT | `/api/v1/products/{productId}` | Update a product |
| DELETE | `/api/v1/products/{productId}` | Delete a product |

### Orders

| Method | Endpoint | Authentication |
|---|---|---|
| POST | `/api/v1/orders` | Bearer token |
| GET | `/api/v1/orders/user` | Bearer token |
| GET | `/api/v1/orders/{orderId}` | Bearer token |
| DELETE | `/api/v1/orders/{orderId}` | Bearer token |

Example order request:

```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

## Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `order.created.v1` | Order Service | Inventory Service |
| `inventory.result.v1` | Inventory Service | Order Service |
| `order.cancelled.v1` | Order Service | Inventory Service |
| `order.created.v1.dlt` | Kafka error handling | Manual investigation |

## Order Workflow

```text
Customer places order
        |
        v
Order Service creates PENDING order
        |
        v
OrderCreatedEvent
        |
        v
Inventory Service validates stock
        |
        +---- insufficient stock ----> INVENTORY_REJECTED
        |
        +---- stock available --------> CONFIRMED
                                              |
                                              v
                                      Customer cancels
                                              |
                                              v
                                    OrderCancelledEvent
                                              |
                                              v
                                  Inventory stock restored
```

## Running Tests

### Auth Service

```powershell
cd backend/auth-service
mvn test
```

### Order Service

```powershell
cd backend/order-service
mvn test
```

### Inventory Service

Docker Desktop must be running because integration tests use Testcontainers.

```powershell
cd backend/inventory-service
mvn test
```

### API Gateway

```powershell
cd backend/api-gateway
mvn test
```

### Frontend

```powershell
cd frontend
npm run lint
npm run build
```

## Stop Infrastructure

```powershell
docker compose down
```

To also remove database and Kafka volumes:

```powershell
docker compose down -v
```

Warning: `-v` permanently removes local database and Kafka data.

## Development Workflow

- `main`: stable and demo-ready
- `dev`: shared integration branch
- `feature/*`: individual work branches

Developers:

- Tushar
- Tejas
