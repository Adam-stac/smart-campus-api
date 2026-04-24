# Smart Campus API

## Overview

The Smart Campus API is a RESTful web service built using JAX-RS (Jersey 3.1.5) and an embedded Grizzly HTTP server. It provides an interface for managing university campus infrastructure, specifically Rooms and the Sensors deployed within them.

The API supports full CRUD operations for rooms and sensors, maintains a log of sensor readings, and enforces business logic constraints such as preventing deletion of occupied rooms and blocking readings from sensors under maintenance.

All data is stored in-memory using ConcurrentHashMap structures. The API follows RESTful principles including proper HTTP status codes, JSON request/response bodies, resource nesting, and a HATEOAS-inspired discovery endpoint.

---

## API Structure

```
GET    /api/v1                              - Discovery endpoint
GET    /api/v1/rooms                        - Get all rooms
POST   /api/v1/rooms                        - Create a room
GET    /api/v1/rooms/{roomId}               - Get room by ID
DELETE /api/v1/rooms/{roomId}               - Delete a room

GET    /api/v1/sensors                      - Get all sensors
GET    /api/v1/sensors?type={type}          - Get sensors filtered by type
POST   /api/v1/sensors                      - Create a sensor
GET    /api/v1/sensors/{sensorId}           - Get sensor by ID
DELETE /api/v1/sensors/{sensorId}           - Delete a sensor
POST   /api/v1/sensors/{sensorId}/readings  - Add a reading
GET    /api/v1/sensors/{sensorId}/readings  - Get all readings for a sensor
```

---

## How to Build and Run

### Prerequisites
- Java JDK 17 or 21
- Maven 3.8+
- Git

### Steps

1. Clone the repository:
```bash
git clone https://github.com/Adam-stac/smart-campus-api.git
cd smart-campus-api
```

2. Build the project:
```bash
mvn clean install
```

3. Run the server:
```bash
mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
```

Or run `Main.java` directly from IntelliJ by clicking the green run button.

4. The server will start at:
```
http://localhost:8080/api/v1/
```

5. To stop the server press Enter in the console.

---

## Sample curl Commands

> **Windows users:** use `curl.exe` instead of `curl` in PowerShell.

**1. Get API discovery info:**
```bash
curl http://localhost:8080/api/v1/
```

**2. Create a room:**
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Library", "capacity": 50}'
```

**3. Create a sensor:**
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "currentValue": 400.0, "roomId": "ROOM-XXXXXXXX"}'
```

**4. Get all sensors filtered by type:**
```bash
curl http://localhost:8080/api/v1/sensors?type=CO2
```

**5. Post a sensor reading:**
```bash
curl -X POST http://localhost:8080/api/v1/sensors/SENS-XXXXXXXX/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 450.0}'
```

**6. Delete a sensor:**
```bash
curl -X DELETE http://localhost:8080/api/v1/sensors/SENS-XXXXXXXX
```

**7. Delete a room (fails if sensors assigned):**
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/ROOM-XXXXXXXX
```

---

## Report -- Question Answers

### Part 1.1 -- JAX-RS Resource Lifecycle

JAX-RS creates a brand new instance of each resource class every time a request comes in. This is called per-request lifecycle. I noticed this early on because any data I stored as an instance variable inside a resource class would just disappear between requests. It took me a moment to understand why but it makes sense once you realise the runtime does not reuse the same object.

To get around this I created a separate DataStore class that holds all the data in static ConcurrentHashMap fields. Because they are static they belong to the class itself rather than any particular instance, so all requests share the same data regardless of how many resource objects get created and thrown away.

I used ConcurrentHashMap specifically rather than a plain HashMap because the server handles multiple requests at the same time. If two requests tried to write to a regular HashMap simultaneously you could end up with corrupted data or lost entries. ConcurrentHashMap is designed for concurrent access so it handles that safely without me having to write any synchronisation code myself.

---

### Part 1.2 -- HATEOAS

HATEOAS stands for Hypermedia as the Engine of Application State. The idea is that instead of making clients memorise every URL in the API, the responses themselves contain links that tell the client where it can go next. It is considered advanced REST design because it makes the API self-describing.

The practical benefit for developers is that you do not have to rely purely on documentation that might be out of date. If the API changes a URL, a client following links will still work because it reads the URL from the response rather than having it hardcoded. In my implementation the discovery endpoint at GET /api/v1 returns a map showing where the rooms and sensors collections live. A client can hit that one URL and find everything else from there without needing to know the structure in advance.

---

### Part 2.1 -- IDs vs Full Objects

If you return only IDs in a list response, the payload is much smaller which is good for bandwidth. But the client then has to make a separate request for each ID to get the actual data, which means more round trips and more latency. That could be a real problem if you have hundreds of rooms.

Returning full objects means one request gets everything. The payload is bigger but the client has all the information it needs straight away without any follow up calls. For this project I went with full objects because the data model is not that large and it keeps the client side simple. If this were a production system with thousands of rooms I would look at pagination or returning a summary version of each object rather than the full thing.

---

### Part 2.2 -- DELETE Idempotency

Strictly speaking my DELETE is not fully idempotent. The first time you delete a room it returns 204 No Content. If you send the same request again it returns 404 because the room is already gone. A purely idempotent operation would return the same status code every time.

That said, the actual state of the system is the same after every call. The room does not exist either way, so no extra damage is done by calling DELETE multiple times. The 404 on the second call is just the server being honest that the resource was not there. In practice most APIs behave this way and it is generally considered acceptable even if it is not textbook idempotent.

---

### Part 3.1 -- @Consumes and Content Type Mismatch

The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that this endpoint only wants to receive JSON. If a client sends a request with a different Content-Type like text/plain or application/xml, JAX-RS intercepts it before the method even runs and sends back a 415 Unsupported Media Type response automatically.

This is useful because you do not have to write any validation code yourself to handle the wrong format. The framework takes care of it. It also means Jackson will never try to parse something it cannot handle, which avoids unexpected errors deeper in the application.

---

### Part 3.2 -- @QueryParam vs Path-Based Filtering

Using a query parameter like GET /api/v1/sensors?type=CO2 is the better approach for filtering compared to putting the filter in the path like GET /api/v1/sensors/type/CO2.

The main reason is that query parameters are optional by nature. With a single endpoint I can handle both getting all sensors and getting filtered sensors without needing two separate paths. If the type parameter is not provided I just return everything. A path like /sensors/type/CO2 implies that is a completely different resource rather than a filtered view of the same collection, which is semantically wrong. Query parameters are also what most HTTP clients and tools expect for search and filter operations so it makes the API more predictable and easier to work with.

---

### Part 4.1 -- Sub-Resource Locator Pattern

The sub-resource locator pattern lets you split a large resource class into smaller focused ones. In my implementation SensorResource has a method mapped to {sensorId}/readings that just returns a new instance of SensorReadingResource. The key thing is that this method has no HTTP verb annotation on it. That tells JAX-RS to pass the request along to whatever object gets returned rather than handling it directly.

The benefit is that all the readings logic lives in SensorReadingResource rather than being mixed in with sensor logic inside SensorResource. If I had put everything in one class it would have become very long and hard to follow. Keeping them separate means each class has one job, which makes the code easier to read, debug, and extend later. Adding new sub-resources in the future would not require touching the existing classes at all.

---

### Part 5.2 -- 422 vs 404

A 404 means the URL you requested does not exist. But when a client posts a sensor with an invalid roomId, the URL /api/v1/sensors is completely valid and exists. The problem is not the URL, it is the content of the request body.

A 422 Unprocessable Entity is more accurate here because it tells the client that the request arrived fine, the JSON was readable, but something inside the payload could not be processed. In this case the roomId referenced a room that does not exist in the system. Returning a 404 would confuse the client into thinking the sensors endpoint is missing. A 422 points directly at the data problem and gives the client a clear signal about what needs to be fixed.

---

### Part 5.4 -- Stack Trace Security Risks

If a Java stack trace gets sent back in an API response it gives away a lot about how the application is built. You can see the package names, class names, method names, and even line numbers. Someone with bad intentions can use that to understand the structure of the codebase and look up known vulnerabilities for the specific frameworks and versions they can identify.

Stack traces can also leak file system paths and internal logic that should never be visible outside the server. In my implementation the global ExceptionMapper catches any unexpected error and returns a plain 500 response with a generic message. The actual error gets logged on the server side so developers can still investigate it, but nothing sensitive is ever exposed to whoever made the request.

---

### Part 5.5 -- Filters vs Inline Logging

If you put logging statements inside every resource method you end up repeating the same code across the entire codebase. Every time you add a new endpoint you have to remember to add the logging. If you want to change the log format you have to update every single method. That is a lot of maintenance for something that should just happen automatically.

Using a JAX-RS filter means the logging is defined once in one place and applies to every request and response automatically. My ApiLoggingFilter implements both ContainerRequestFilter and ContainerResponseFilter so it logs the method and URI on the way in and the status code on the way out, for every single endpoint without any extra work. The resource classes stay clean and only contain business logic, which is how it should be.
