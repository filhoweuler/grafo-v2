#!/bin/bash

./finaliza.sh

./starta_cluster.sh 3 3 5000 9000

sleep 8

java -cp grafo-distribuido-0.1.jar grafo.GrafoClient localhost 9000