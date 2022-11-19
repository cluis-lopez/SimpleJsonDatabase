mvn clean
mvn assembly:assembly

ssh pi@192.168.1.11 rm JavaRepo/*.jar

scp target/SimpleJsonDatabase-0.0.1-SNAPSHOT-jar-with-dependencies.jar pi@192.168.1.11:/home/pi/JavaRepo/SimpleJsonDatabase.jar

scp etc/config/Serverdata.cnf pi@192.168.1.11:/home/pi/JavaRepo/etc/config

ssh -t pi@192.168.1.11 "cd JavaRepo; java -cp SimpleJsonDatabase.jar com.clopez.SimpleJsonDatabase.Dataserver"



