mvn clean
mvn assembly:assembly

ssh pi@192.168.4.11 rm -rf JavaRepo/*

scp target/SimpleJsonDatabase-0.0.1-SNAPSHOT-jar-with-dependencies.jar pi@192.168.4.11:/home/pi/JavaRepo/SimpleJsonDatabase.jar

ssh pi@192.168.4.11 mkdir JavaRepo/etc
ssh pi@192.168.4.11 mkdir JavaRepo/etc/data
ssh pi@192.168.4.11 mkdir JavaRepo/etc/logs
ssh pi@192.168.4.11 mkdir JavaRepo/etc/config
ssh pi@192.168.4.11 mkdir JavaRepo/etc/data/Message_data

scp etc/config/Serverdata.cnf pi@192.168.4.11:/home/pi/JavaRepo/etc/config

ssh -t pi@192.168.4.11 "cd JavaRepo; java -cp SimpleJsonDatabase.jar com.clopez.SimpleJsonDatabase.Dataserver"



