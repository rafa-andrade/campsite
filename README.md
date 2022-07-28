# Campsite Project
REST API project to manage the campsite reservations

### Prerequisites and Dependencies
- Java 11
- Maven
- Docker

### Build the project
```
mvn clean install
```

### Create docker image
```
mvn package
mvn dockerfile:build
```

### DataBase
Campsite project is using H2 relational database for demonstration and proof of concept, it should be replaced by a stable database server in real production application as PostgreSQL  

###Cache
Campsite project is using ```ConcurrentMapCacheManager``` from spring for demonstration and proof of concept, it should be replaced by a stable cache server in real production application as Redis

###API Documentation
http://localhost:8080/swagger-ui/index.html