Extremely simple database program. Each "database" stores single type of Java objects serialized as Json. Each database stores these objects in a single file.

- UserDatabase & GroupDatabase are designed to store small objects with high number of read and few modifications. Each modification, requires the re-write of the entire datafile
-  MessageDatabase is designed to continuously store time-stamped messages with very few read access to that data but with file being updated frequently. In this case the datafile is appended with every new message arriving to the database. Files are rotated to avoid managing very large files.


Access to the databses is made through a REST API using HTTP POST requests using the URI

<host>:port/<database>/<operation>

And enclosing parameters in the request body using Json format.

Curl examples:

# curl --header "Content-Type: application/json" -X POST http://localhost:8081/UserDatabase/findByName -d "carlos"

# curl --header "Content-Type: application/json" -X POST http://localhost:8081/MessageDatabase/getChatByIdNumber -d'{"id":3,"number":5}'
