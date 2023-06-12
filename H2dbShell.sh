#!/bin/bash

cd ~/.m2/repository/com/h2database/h2/2.1.214 || exit
java -cp h2-2.1.214.jar org.h2.tools.Shell
