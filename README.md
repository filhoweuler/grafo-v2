<h1>Banco de Dados de Grafos Distribuido</h1>

O projeto é compilado usando o maven.

<code>mvn package</code>

Na pasta target existem 2 scripts. Um inicia um custer de servidores (CopyCat + Thrift) e o outro finaliza o mesmo.

<code> ./starta_cluster.sh NUMERO_CLUSTERS TAMANHO_CLUSTER PORTA_INICIAL_COPYCAT PORTA_INICIAL_THRIFT </code>

<code> ./finaliza </code>