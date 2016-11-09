#!/bin/sh
echo "start skript"
scriptExit=0

# Current path
currentPath=`dirname $0`

# Logfile path
logfile="log.txt"

# Maven dependency plugin for downloading artifact
mvndependencyplugin="org.apache.maven.plugins:maven-dependency-plugin"
mvndependencypluginversion="2.9"

# Artifact definition
groupId="com.siemens.ra.cg.ext.iltis"
artefactId="cg-ext-iltis-ear"
version="1.6-SNAPSHOT"
packaging="ear"

# Directory where to download the artifact
outputDirectory="./deployments/"

# POT script location
potLocation="/opt/puzzle-openshift-tool/bin/pot"

# rhc parameter
server='${brokerurl}'
appname='${appServer.name}${node.name}'
namespace='${domain}'
servercartridge="${cartridges}"
gearsize='${gear}'
additionalcartridges=""
user='${username}'
password='${password}'


# Loglevel prefix
info="      "
error="ERROR "

#----------------------------------------------------------------
# functions
#----------------------------------------------------------------

# Log to logfile
do_log(){
  now=$(date +"%T")
  echo "$now: $1 > $2" >> $logfile
}

logInfo(){
  do_log "$info" "$1"
}

logError(){
  do_log "$error" "$1"
}

# Execute command and log errors to logfile
sh() {
  # echo "Executing '$1'"
  logInfo "Start executing $2"
  OUTPUT=`/bin/sh -c "$1" 2>&1`
  EXITCODE=$?
  if [ $EXITCODE -ne 0 ]; then
    logError "Execution error: $2! $OUTPUT"
  else
	logInfo "$2 sucessfully terminated"
  fi

  return $EXITCODE
}

sh_exit_on_error() {
   RESULT=sh "$1" "$2"
     if [ $RESULT -ne 0 ]; then
     logError "Terminate script execution"
     exit $RESULT
   fi
}

# Initialize arguments and logfile
init() {

# Initialize log file
now=$(date)
echo "Start executing deployment script on $now" > $logfile

if [ "$user" ]; then
      user="-l$user"
fi

if [ "$password" ]; then
      password="-p$password"
fi

if [ "$namespace" ]; then
      namespace="-n$namespace"
fi

if [ "$gearsize" ]; then
      gearsize="--gear-size $gearsize"
fi

export POT_SERVER_$namespace=${brokerurl}
export POT_USER_$namespace=${username}
export POT_PASSWORD_$namespace=${password}
export POT_NAMESPACE_$namespace=${domain}
export POT_APP_$namespace=${appServer.name}${node.name}

}


#----------------------------------------------------------------
# Script execution
#----------------------------------------------------------------

#Init
init

# go to current path
sh "cd $currentPath" "go to current path $currentPath"
sh "cd .." "go to parent folder"

# change action hook execution right
sh_exit_on_error "chmod +x .openshift/action_hooks/pre_start" "make action hook executable"

# Download artifact
logInfo "Downloading artifact $groupId:$artefactId:$version:$packaging to directory $outputDirectory"
sh_exit_on_error "mvn $mvndependencyplugin:$mvndependencypluginversion:copy -Dartifact=$groupId:$artefactId:$version:$packaging -DoutputDirectory=$outputDirectory" "download artifact"

# Create domain
logInfo "Create domain $namespace on server $server if not yet exists"
sh "rhc --config=/dev/null --noprompt --server=$server $user $password domain create $namespace" "create domain"

# Create gear
logInfo "Create gear $appname on domain $namespace on server $server"
sh "rhc --config=/dev/null --noprompt --server=$server $user $password $namespace -a$appname app create $servercartridge $gearsize $additionalcartridges" "create gear"

# Build binary deploymentartifact
logInfo "Build binary"
sh "$potLocation tar . $outputDirectory*" "build binary"

# Deploy binary to openshift sever
logInfo "Deploy binary to openshift sever on domain $namespace"
sh_exit_on_error "$potLocation deploy $namespace" "deploy to $namespace"

logInfo "Deployment script terminated with exitcode $scriptExit"
echo "script terminated with exitcode $scriptExit"