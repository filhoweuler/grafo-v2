#!/bin/bash

cluster_num=$1

cluster_size=$2

cluster_port=$3

server_port=$4

n=$((cluster_size * cluster_num))

id=0

while [ $id -lt $n ]; do
	echo Criando a replica $id
	java -cp "grafo-distribuido-0.1.jar" grafo.ReplicaServer $n $id $cluster_port $cluster_size &
	((id++))
done

id=0

echo Aguardando CopyCat iniciar ...
sleep 5

while [ $id -lt $n ]; do
	echo Startando um server na porta $id
	porta=$((server_port + id))
	java -cp "grafo-distribuido-0.1.jar" grafo.GrafoServer $n $server_port $porta $cluster_num $cluster_port $cluster_size &
	((id++))
done