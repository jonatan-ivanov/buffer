#! /bin/bash

function run {
   spring run *.groovy
}

function jar {
   spring jar buffer.jar .
}

function dockerBuild {
   spring jar buffer.jar . && docker-compose build
}

function dockerRun {
   spring jar buffer.jar . && docker-compose up --build
}

$1
