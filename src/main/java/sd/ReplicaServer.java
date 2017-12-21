package grafo;

import static java.lang.Thread.sleep;
import java.util.Arrays;
import java.util.Collection;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

import io.atomix.*;
import io.atomix.copycat.server.storage.StorageLevel;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.local.LocalTransport;
import io.atomix.catalyst.transport.local.LocalServerRegistry;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import io.atomix.concurrent.DistributedLock;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.client.CopycatClient;

public class ReplicaServer {

	/*
		0 - numero de replicas que serao feitas
		1 - meu id entre as replicas
		2 - a primeira porta usada por replicas
		3 - tamanho de cada cluster
	*/
	public static void main (String[] args) {

		int numero_replicas = Integer.parseInt(args[0]);
		int id = Integer.parseInt(args[1]);
		int primeira_porta = Integer.parseInt(args[2]);
		int cluster = Integer.parseInt(args[3]);
		int porta = primeira_porta + id;

		boolean cria_cluster = (id % cluster == 0);

		System.out.println("Novo CopyCat Server na porta " + porta);

		Address add = new Address("localhost", porta);
		CopycatServer.Builder builder = CopycatServer.builder(add);
		builder.withStateMachine(GrafoStateMachine::new);
		builder.withTransport(NettyTransport.builder().withThreads(1).build());
		builder.withStorage(Storage.builder().withDirectory(new File("logs/cpcat/cpcat"+id)).withStorageLevel(StorageLevel.DISK).build());

		CopycatServer copycatServer = builder.build();

		//adiciona todos os comandos
		copycatServer.serializer().register(AdicionaVertice.class);
		copycatServer.serializer().register(LeVertice.class);

		if(cria_cluster) {
			System.out.println("====== Novo cluster sendo criado ======");
			CompletableFuture<CopycatServer> future = copycatServer.bootstrap();
			future.join();
		} else {
			try {
				sleep(1000);
			} catch(Exception e) {e.printStackTrace();}

			

			int mestre = primeira_porta + (id/cluster) * cluster;

			System.out.println("====== Me juntando a um cluster existente ======" + mestre);

			Collection<Address> cadd = Arrays.asList(new Address("localhost", mestre));
			copycatServer.join(cadd).join();
		}
	}

}