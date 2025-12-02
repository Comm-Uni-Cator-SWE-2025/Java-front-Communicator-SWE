#!/bin/bash
DIR="$(dirname "$0")"
cd "$DIR"
java -Dswecomm.logdir="$DIR" -jar core-backend.jar &
