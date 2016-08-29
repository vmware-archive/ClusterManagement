set LOCATOR_PORT=10334
set SERVER2_PORT=40405
set PROJECT_JARS=..\target\clusterManager-0.0.1.jar

#call ..\setenvironment.bat

set START_LOCATOR="start locator --name=locator1 --port=10334 --properties-file=config/locator.properties --load-cluster-configuration-from-dir=true --initial-heap=256m --max-heap=256m  --J=-Dgemfire.OSProcess.ENABLE_OUTPUT_REDIRECTION=true"

set START_SERVER1="start server --name=server1 --server-port=0 --properties-file=config/gemfire.properties --initial-heap=1g --max-heap=1g --J=-Dgemfire.OSProcess.ENABLE_OUTPUT_REDIRECTION=true"


geode -e %START_LOCATOR% -e %START_SERVER1% -e "deploy --jar=%PROJECT_JARS%" -e "list members" -e "list regions" -e "exit"
