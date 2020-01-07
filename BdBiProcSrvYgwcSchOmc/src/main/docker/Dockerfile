FROM docker.tfitsoft.com/base/centos7:jdk8
ADD BdBiProcSrvYgwcSchOmc.jar BdBiProcSrvYgwcSchOmc.jar
ENV JAVA_OPTS="-server -Xmx1g -Xms1g -Xmn256g -XX:SurvivorRatio=4 -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+UseParallelGC -XX:ParallelGCThreads=4 -XX:+UseParallelOldGC -XX:+UseAdaptiveSizePolicy -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/logs/gateway -Xloggc:/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10M"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /BdBiProcSrvYgwcSchOmc.jar" ]
VOLUME ["/opt/logs/BdBiProcSrvYgwcSchOmc"]
ARG port
EXPOSE $port