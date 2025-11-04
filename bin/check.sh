#!/usr/bin/env bash
source local/config.txt || exit 1
curl -s "$baseurl/$webapp/"
curl -s "$baseurl/$webapp/hello"
curl -s "$baseurl/$webapp/redis"
curl -s "$baseurl/$webapp/redispool"

