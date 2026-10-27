# Campus Equipment Booking Service
Java 17 / Spring Boot REST API for a CI/CD lab assessment.

## Build and test
`mvn clean test`

## Run
`mvn spring-boot:run` — port 8080.

## Endpoints
- `GET /api/equipment`
- `GET /api/equipment/{id}`
- `POST /api/bookings`
- `GET /api/bookings/{id}`
- `POST /api/bookings/{id}/cancel`
- `GET /actuator/health`

Inventory is in memory and resets on restart. No database is required.

---

## Set B: Booking Cancellation and Inventory Restoration

### Prerequisites
Java 17, Maven 3.6+, Docker, Minikube, kubectl.

### Build and test
```bash
mvn clean test        # runs 13 unit tests
mvn clean package     # produces target/campus-equipment-booking-1.0.0.jar
```

### Docker
```bash
docker build -t campus-equipment-booking:set-b .
docker run -d --name booking-set-b -p 8080:8080 campus-equipment-booking:set-b
curl http://localhost:8080/actuator/health      # {"status":"UP",...}
```
The image uses the `eclipse-temurin:17-jre` base, copies the packaged JAR, and exposes port 8080.

### Kubernetes (Minikube)
```bash
minikube start --driver=docker
minikube image load campus-equipment-booking:set-b
kubectl apply -f k8s/
kubectl rollout status deployment/campus-equipment-booking
kubectl get deployments,pods,svc -l app=campus-equipment-booking
```
Health check through the NodePort Service:
```bash
curl $(minikube service campus-equipment-booking --url)/actuator/health
```
Scale to two replicas:
```bash
kubectl scale deployment campus-equipment-booking --replicas=2
```
Notes:
- `imagePullPolicy: IfNotPresent` makes Kubernetes use the image loaded into Minikube instead of pulling it from a registry.
- NodePort 30081 is used because 30080 was already allocated by another Service in the shared lab cluster.
- Readiness and liveness probes use `/actuator/health/readiness` and `/actuator/health/liveness`.
- Inventory is held in memory per pod, so with two replicas each pod keeps its own independent inventory. A production setup would need a shared datastore.

### Defect fixed: booking capacity boundary
`BookingService.create` checked `quantity > availableQuantity + 1`, so a request for exactly one unit more than the available stock was accepted and inventory went negative (for example, 5 projectors booked when 4 were available, leaving -1).
The condition is now `quantity > availableQuantity`. A booking is accepted only when quantity is positive and does not exceed available stock.

Tests:
- `bookingExactlyAvailableQuantityAccepted` books exactly 4 of 4. It is confirmed and inventory drops to 0.
- `bookingOneMoreThanAvailableRejectedWithoutSideEffects` requests 5 of 4. It is rejected, inventory stays at 4, and no booking is created.
- `overByOneAfterPartialBookingRejected` applies the same over-by-one check after a partial booking.

These tests failed against the original code and pass after the fix.

### Change request: booking cancellation
`POST /api/bookings/{id}/cancel` cancels a CONFIRMED booking and restores its quantity to the equipment inventory. Only CONFIRMED bookings can be cancelled. A repeated cancellation returns HTTP 409 Conflict and does not restore inventory a second time.

Tests:
- `cancellingConfirmedBookingRestoresInventoryAndMarksCancelled`: status becomes CANCELLED and inventory returns to its original level.
- `repeatedCancellationRejectedAndInventoryNotRestoredTwice`: the second cancel throws and inventory stays at 8, not 11.
- `repeatedCancellationDoesNotAffectOtherBookings`: other bookings stay CONFIRMED and inventory is unaffected.
- `cancellingUnknownBookingRejected`: an unknown ID returns 404.
