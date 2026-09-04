#!/usr/bin/env bash
source local/config.txt || exit 1
curl -s "$baseurl/$webapp/userLoginServlet"
curl -s "$baseurl/$webapp/adminLoginServlet"
curl -s "$baseurl/$webapp/registrierenServlet"
curl -s "$baseurl/$webapp/Buchen"
curl -s "$baseurl/$webapp/Centers"
curl -s "$baseurl/$webapp/Vaccines"
curl -s "$baseurl/$webapp/Zeitslot"
curl -s "$baseurl/$webapp/LogoutServlet"









#curl -s "$baseurl/$webapp/hello"
#curl -s "$baseurl/$webapp/redis"
#curl -s "$baseurl/$webapp/redispool"

