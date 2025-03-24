cmd : 

curl -X POST http://localhost:8080/api/project/generate ^
     -H "Content-Type: application/json" ^
     -H "Accept: application/zip" ^
     -o generated-project.zip ^
     -d "{ \"group\": \"com.example\", \"artifact\": \"demo\", \"name\": \"demo\", \"description\": \"Demo project for Spring Boot\", \"packageName\": \"com.example.demo\", \"packaging\": \"jar\" }"



Postman :

curl --location 'http://localhost:8080/api/project/generate' \
--header 'Content-Type: application/json' \
--header 'Accept: application/octet-stream' \
--data '{
  "group": "com.example",
  "artifact": "demo",
  "name": "demo",
  "description": "Demo project for Spring Boot",
  "packageName": "com.example.demo",
  "packaging": "jar"
}'

     
