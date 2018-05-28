#!/bin/sh
#

export serviceRootUrl=http://localhost:8080/FROST-Server
export bus_busImplementationClass=de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus
export bus_mqttBroker=tcp://mosquitto:1883
export persistence_persistenceManagerImplementationClass=de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.PostgresPersistenceManagerLong
export persistence_alwaysOrderbyId=false
export persistence_db_driver=org.postgresql.Driver
export persistence_db_url=jdbc:postgresql://database:5432/sensorthings
export persistence_db_username=sensorthings
export persistence_db_password=ChangeMe

java -jar target/FROST-Server.MQTT-*-SNAPSHOT-jar-with-dependencies.jar
