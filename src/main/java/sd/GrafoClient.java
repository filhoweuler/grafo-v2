package grafo;

import java.util.*;
import java.util.Scanner;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;



public class GrafoClient {
	static void massert(boolean b) {
		if(!b) {
			System.out.println("Assertion failed.");
			System.exit(1);
		}
	}
	public static void main(String [] args) {
		int porta = 0;
		String servidor = "";

		if ( args.length != 2) {
			System.out.println("CLIENTE: NUMERO DE PARAMETROS INCORRETO");
			return;
		} else {
			porta = Integer.parseInt(args[1]);
			servidor = args[0];
		}

		try {
			TTransport transport = new TSocket(servidor, porta);
			transport.open();
 			TProtocol protocol = new TBinaryProtocol(transport);
			Grafo.Client client = new Grafo.Client(protocol);

			//List<Vertice> l = client.listar_vertices();
			//massert(l.size() == 0);
  			
  			Vertice v = new Vertice(2, 25, "O vertex", 5.5);
  			client.adiciona_vertice(v);

  			v = null;
  			v = client.le_vertice(2);
  			massert(v.getNome() == 2);

  			//v.setDescricao("c");
  			//client.atualiza_vertice(2, v);
  			//v = client.le_vertice(2);
  			//l = client.listar_vertices();
  			//massert(v.getDescricao() == "c");
  			//massert(l.size() == 1);

  			//client.deleta_vertice(2);
  			//v = client.le_vertice(2);
  			//massert(v.getNome() == -1);
  			//l = client.listar_vertices();
  			//massert(l.size() == 0);



  			// client.adiciona_vertice(v);

  			// Vertice v2 = client.le_vertice(v.nome);

  			// //assert(v2 != null);

  			// System.out.println(v2);
			// Scanner sc = new Scanner(System.in);
  	// 		int op;

  	// 		do {
  	// 			System.out.print("\033[H\033[2J");  
   //  			System.out.flush(); 	
  	// 			System.out.println("***MENU****");
  	// 			System.out.println("1 - Adicionar vertice");
  	// 			System.out.println("2 - Adicionar aresta");
  	// 			System.out.println("3 - Listar vertices");
			// 	System.out.println("4 - Listar arestas");
			// 	System.out.println("5 - Sair");
			// 	op = sc.nextInt();

			// 	switch(op) {
			// 		case 1:
			// 			Vertice v = novo_vertice();
			// 			client.adiciona_vertice(v);
			// 			break;
			// 		case 2:
			// 			Aresta a = nova_aresta();
			// 			client.adiciona_aresta(a);
			// 			break;
			// 		case 3:
			// 			client.listar_vertices();
			// 			break;
			// 		case 4:
			// 			client.listar_arestas();
			// 			break;
			// 		case 5:
			// 			break;
			// 		default:
			// 			System.out.println("Opcao invalida.");
			// 	}

  	// 		} while (op != 5);

			// Vertice v;
			Aresta a;

			v = new Vertice(1, 1, "Vertice um.", 1.1);
			client.adiciona_vertice(v);

			v = null;
  			v = client.le_vertice(1);
  			massert(v.getNome() == 1);

			// v = new Vertice(1, 1, "Vertice ocho.", 1.1);
			// client.adiciona_vertice(v);

			// v = new Vertice(2, 1, "Vertice dois.", 1.1);
			// client.adiciona_vertice(v);

			v = new Vertice(3, 1, "Vertice tres.", 1.1);
			client.adiciona_vertice(v);

			v = null;
  			v = client.le_vertice(3);
  			massert(v.getNome() == 3);

			v = new Vertice(4, 1, "Vertice quetro.", 1.1);
			client.adiciona_vertice(v);

			v = null;
  			v = client.le_vertice(4);
  			massert(v.getNome() == 4);

			v = new Vertice(5, 1, "Vertice cinco.", 1.1);
			client.adiciona_vertice(v);

			v = null;
  			v = client.le_vertice(5);
  			massert(v.getNome() == 5);

			v = new Vertice(6, 1, "Vertice seis.", 1.1);
			client.adiciona_vertice(v);

			v = new Vertice(7, 1, "Vertice setes.", 1.1);
			client.adiciona_vertice(v);

			v = new Vertice(8, 1, "Vertice ocho.", 1.1);
			client.adiciona_vertice(v);

			// a = new Aresta(1, 2, 1.55667, true, "Aresta um");
			// client.adiciona_aresta(a);
			
			// a = new Aresta(3, 1, 1.55667, true, "Aresta dois");
			// client.adiciona_aresta(a);

			// a = new Aresta(4, 1, 1.55667, true, "Aresta tres");
			// client.adiciona_aresta(a);

			// a = new Aresta(4, 3, 1.55667, false, "Aresta quatro");
			// client.adiciona_aresta(a);

			// a = new Aresta(4, 5, 1.55667, true, "Aresta cinco");
			// client.adiciona_aresta(a);

			// a = new Aresta(5, 7, 2.5, true, "Aresta cinco");
			// client.adiciona_aresta(a);

			// a = new Aresta(2, 3, 1.55667, true, "Aresta seis");
			// client.adiciona_aresta(a);

			// client.listar_arestas();
			// client.listar_vertices();

			// client.listar_arestas_vertice(4);
			// client.listar_vizinhos_vertice(4);

			// client.dijkstra(4);

			// client.deleta_vertice(4);
			// client.listar_arestas();

			// client.deleta_aresta(1, 2);
			// client.deleta_aresta(1, 2);

			// a = new Aresta(4, 3, 2.55667, false, "Aresta quatro");
			// client.atualiza_aresta(4, 3, a);
			// client.listar_arestas();
			
			// v.setCor(88);
			// client.atualiza_vertice(8, v);
			// client.listar_vertices();



			// int cnt = 0;

			// while(cnt < 100000) {
			// 	client.graph_mutex_acquire();
			// 	cnt++;
			// 	a = client.le_aresta(2, 3);
			// 	a.setPeso(a.getPeso() + 1.0);
			// 	client.atualiza_aresta(2, 3, a);
			// 	client.graph_mutex_release();
			// }

			// client.listar_arestas();

			transport.close();

		} catch (TException x) {
			x.printStackTrace();
			System.out.println(x.getMessage());
		}
	}

	private static Vertice novo_vertice() {
		Scanner sc = new Scanner(System.in);
		
		int nome, cor;
		String desc;
		double peso;

		nome = sc.nextInt();
		cor = sc.nextInt();
		sc.nextLine();
		desc = sc.nextLine();
		peso = sc.nextDouble();

		Vertice retorno = new Vertice(nome, cor, desc, peso);
		return retorno;
	}

	private static Aresta nova_aresta() {
		Scanner sc = new Scanner(System.in);

		int v1, v2, dir;
		double peso;
		boolean direcionada;
		String desc;

		v1 = sc.nextInt();
		v2 = sc.nextInt();
		peso = sc.nextDouble();
		dir = sc.nextInt();
		sc.nextLine();
		desc = sc.nextLine();

		if(dir != 0) direcionada = true;
		else direcionada = false;

		Aresta retorno = new Aresta(v1, v2, peso, direcionada, desc);
		return retorno;
	}
}