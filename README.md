# universal-debezium-connector Project

This project is to create a source and sink connector between two db's.

followinf steps can be performed to run this project. 

step 1: run docker-compose1.yml 
step 2: then run docker ps on terminal and copy the image id of debezium-jdbc amd paste that id in copy-file.bat file
step 3: run this copy-file.bat 
step 4: then run the project from terminal by using this command mvn compile quarkus:dev -Ddebug=5006
step 5: and run the following script in mysql shell
            - CREATE USER 'test1'@'%' IDENTIFIED BY 'PASSWORD';
            -  GRANT ALL PRIVILEGES ON *.* TO 'test1'@'%' WITH GRANT OPTION;
            -   FLUSH PRIVILEGES;
 