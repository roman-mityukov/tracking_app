#!/usr/bin/bash

git log -n 10 --no-merges --pretty=format:"%h - %an, %ai : %s"