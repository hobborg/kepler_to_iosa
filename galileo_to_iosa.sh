#!/usr/bin/env bash
JARFILE=`find . -iname "*iosa*.jar" -type f -print -quit`;
echo "${JARFILE}";
jar tf "${JARFILE}" 2>&1 > /dev/null;
if [ $? -ne 0 ]; then
	echo "ERROR: main JAR file not found. Did you build the project?";
	exit 1;
fi
java -jar "${JARFILE}" "$@";
exit 0;
