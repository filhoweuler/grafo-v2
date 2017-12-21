package grafo;

import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import java.util.HashMap;

public class GrafoServer {
	private static Grafo.Processor processor;
	private static GrafoHandler handler;
	public static void main(String [] args) {
		int porta = 0;
		int ini = 0;
		int n = 0;
		int numero_clusters = 0;
		int inicial_cluster = 0;
		int tamanho_cluster = 0;

		/*  0 - numero de servidores
			1 - porta inicial dos servidores
			2 - porta deste servidor
			3 - NUmero de clusters no projeto
			4 - Porta inicial dos Clusters
			5 - Tamanho Cluster
		*/

		if ( args.length != 6 ) {
			System.out.println("ERRO : NUMERO DE PARAMETROS INCORRETO");
			return;
		} else {
			ini = Integer.parseInt(args[1]);
			porta = Integer.parseInt(args[2]);
			n = Integer.parseInt(args[0]);
			numero_clusters = Integer.parseInt(args[3]);
			inicial_cluster = Integer.parseInt(args[4]);
			tamanho_cluster = Integer.parseInt(args[5]);
		}

		try {
			handler = new GrafoHandler(n, porta - ini, ini, numero_clusters,inicial_cluster,tamanho_cluster);
			processor = new Grafo.Processor(handler);
			TServerTransport st = new TServerSocket(porta);
			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(st).processor(processor));
			System.out.println("Iniciando o servidor na porta " + porta + " ...");
	 		server.serve();
	  	} catch (Exception x){
			x.printStackTrace();
	 	}
	}
}