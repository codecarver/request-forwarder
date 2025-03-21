# Request Forwarder

A Java-based application that forwards incoming HTTP GET requests to multiple backend servers in parallel. Built using Maven, Logback for logging, and Apache HttpClient for making HTTP requests.

---

## Table of Contents
1. Features
2. Prerequisites
3. Setup
4. Usage
5. Logs
6. Configuration
7. Dependencies
8. Project Structure
9. Contributing
10. License
11. Author
12. Acknowledgments

---

## Features
- Forwards incoming HTTP GET requests to multiple backend servers.
- Logs requests and responses to both the console and a daily rolling log file.
- Configurable backend servers and server port via application.properties.
- Handles timeouts for backend requests.
- Uses multi-threading to ensure that slow or unresponsive backend servers do not block others.

---

## Prerequisites
- Java 11 or later.
- Maven 3.x or later.

---

## Setup

1. Clone the Repository:
   Run the following command to clone the repository:
   git clone https://github.com/codecarver/request-forwarder.git
   cd request-forwarder

2. Build the Project:
   Build the project using Maven:
   mvn clean install

3. Configure Backend Servers:
   Edit the application.properties file in the src/main/resources directory to specify the backend servers and server port:
   ```
   # Server configuration
   server.port=8080

   # Backend servers with paths/endpoints (comma-separated list)
   backend.servers=http://192.168.1.101:8080/api,http://192.168.1.102:8080/api,http://192.168.1.103:8080/api
   ```
   
5. Run the Application:
   Run the application using the following command:
   java -jar target/request-forwarder.jar

---

## Usage
Send a GET request to the application:
curl "http://localhost:8080/resource?param1=value1&param2=value2"

The application will:
1. Log the request details.
2. Forward the request to all configured backend servers.
3. Log the responses from the backend servers.
4. Return a confirmation message to the client.

---

## Logs
- Logs are written to the logs/app.log file in the project directory.
- Logs are also printed to the console (sysout).
- Log files are rolled daily, and old logs are kept for 30 days.

---

## Configuration
application.properties:
- server.port: The port on which the application listens for incoming requests.
- backend.servers: A comma-separated list of backend server URLs.

logback.xml:
- Configures logging behavior, including log file location, rolling policy, and log format.

---

## Dependencies
- Apache HttpClient: For making HTTP requests to backend servers.
- Logback: For logging requests and responses.
- SLF4J: Logging facade.
- Apache Commons Configuration: For reading the application.properties file.

---

## Project Structure
```
request-forwarder/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── RequestForwarder.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── logback.xml
│   └── test/
│       └── java/
├── target/
├── logs/
├── pom.xml
└── README
```

---

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a new branch (git checkout -b feature/your-feature).
3. Commit your changes (git commit -m 'Add some feature').
4. Push to the branch (git push origin feature/your-feature).
5. Open a pull request.

---

## License
This project is licensed under the MIT License. See the LICENSE file for details.

---

## Author
Siwi (https://github.com/codecarver)

---

## Acknowledgments
- Apache HttpClient (https://hc.apache.org/httpcomponents-client-ga/)
- Logback (https://logback.qos.ch/)
- SLF4J (https://www.slf4j.org/)
- Apache Commons Configuration (https://commons.apache.org/proper/commons-configuration/)
