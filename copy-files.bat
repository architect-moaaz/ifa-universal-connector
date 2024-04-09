echo 'copying jar files'
docker cp C:\Users\Akhil\Downloads\applications\universal-connectors\universal-debezium-connector\src\main\resources\jars\mysql-connector-java-8.0.30.jar eec341b200f5:/kafka/libs/
echo 'copied mysql-source-jdbc jar'
docker cp C:\Users\Akhil\Downloads\applications\universal-connectors\universal-debezium-connector\src\main\resources\jars\mongodb-kafka-connect-mongodb-1.8.1/ eec341b200f5:/kafka/connect/
echo 'copied mongodb-sink into kafka-connect'
echo 'copied mysql-source-jdbc jar'
docker cp C:\Users\Akhil\Downloads\applications\universal-connectors\universal-debezium-connector\src\main\resources\jars\init-kafka-connect-odp-1.3.3/ eec341b200f5:/kafka/connect/
docker restart eec341b200f5
echo 'restarted the kafka-connect'