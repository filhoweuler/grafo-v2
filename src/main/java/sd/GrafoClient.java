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

	private static void principal() {
		System.out.println("MENU PRINCIPAL");
		System.out.println("1 - Vertice");
		System.out.println("2 - Aresta");
		System.out.println("3 - Dijkstra");
		System.out.println("4 - SAIR");
	}

	private static void vertice() {
		System.out.println("MENU VERTICE");
		System.out.println("1 - Novo");
		System.out.println("2 - Consultar");
		System.out.println("3 - Alterar");
		System.out.println("4 - Deletar");
		System.out.println("5 - Listar");
		System.out.println("6 - SAIR");
	}

	private static void arestas() {
		System.out.println("MENU ARESTA");
		System.out.println("1 - Novo");
		System.out.println("2 - Consultar");
		System.out.println("3 - Alterar");
		System.out.println("4 - Deletar");
		System.out.println("5 - Listar");
		System.out.println("6 - SAIR");
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

			Vertice vt = new Vertice(2, 25, "O vertex", 5.5);
  			client.adiciona_vertice(vt);

			vt = new Vertice(1, 1, "Vertice ocho.", 1.1);
			client.adiciona_vertice(vt);

			vt = new Vertice(2, 1, "Vertice dois.", 1.1);
			client.adiciona_vertice(vt);

			vt = new Vertice(3, 1, "Vertice tres.", 1.1);
			client.adiciona_vertice(vt);

			vt = new Vertice(4, 1, "Vertice quetro.", 1.1);
			client.adiciona_vertice(vt);

			vt = new Vertice(5, 1, "Vertice cinco.", 1.1);
			client.adiciona_vertice(vt);

			vt = new Vertice(6, 1, "Vertice seis.", 1.1);
			client.adiciona_vertice(vt);

			vt = new Vertice(7, 1, "Vertice setes.", 1.1);
			client.adiciona_vertice(vt);

			vt = new Vertice(8, 1, "Vertice ocho.", 1.1);
			client.adiciona_vertice(vt);

			Aresta at = new Aresta(1, 2, 1.55667, true, "Aresta um");
			client.adiciona_aresta(at);
			
			at = new Aresta(3, 1, 1.55667, true, "Aresta dois");
			client.adiciona_aresta(at);

			at = new Aresta(4, 1, 1.55667, true, "Aresta tres");
			client.adiciona_aresta(at);

			at = new Aresta(4, 3, 1.55667, false, "Aresta quatro");
			client.adiciona_aresta(at);

			at = new Aresta(4, 5, 1.55667, true, "Aresta cinco");
			client.adiciona_aresta(at);

			at = new Aresta(5, 7, 2.5, true, "Aresta cinco");
			client.adiciona_aresta(at);

			at = new Aresta(2, 3, 1.55667, true, "Aresta seis");
			client.adiciona_aresta(at);

			int op_main = 0;

			while (op_main != 4) {
				principal();
				Scanner sc = new Scanner(System.in);
				op_main = sc.nextInt();
				switch(op_main) {
					case 1:
						int op_v = 0;
						while(op_v != 6) {
							vertice();
							op_v = sc.nextInt();
							switch(op_v) {
								case 1:
									Vertice v = novo_vertice();
									client.adiciona_vertice(v);
									break;
								case 2:
									System.out.println("Qual vertice deseja consultar?");
									int q = sc.nextInt();
									Vertice vx = client.le_vertice(q);
									if(vx.getNome() == -1) {
										System.out.println("Vertice nao encontrado!");
									} else {
										System.out.println(vx);
									}
									break;
								case 3:
									Vertice va = novo_vertice();
									client.atualiza_vertice(va.nome, va);
									break;
								case 4:
									System.out.println("Qual vertice deseja deletar?");
									int qd = sc.nextInt();
									client.deleta_vertice(qd);
									break;
								case 5:
									List<Vertice> l = client.listar_vertices();
									System.out.println("VERTICES NO GRAFO:");
									System.out.println("");
									for(Vertice vit : l) {
										System.out.println(vit);
									}
									break;
								case 6:
									break;
								default:
									System.out.println("Opcao invalida!");
							}
						}
						break;
					case 2:
						op_v = 0;
						while(op_v != 6) {
							arestas();
							op_v = sc.nextInt();
							switch(op_v) {
								case 1:
									Aresta a = nova_aresta();
									client.adiciona_aresta(a);
									break;
								case 2: {
									System.out.println("Qual aresta deseja consultar?");
									int v1 = sc.nextInt();
									int v2 = sc.nextInt();
									Aresta ax = client.le_aresta(v1, v2);
									if(ax.getV1() == -1) {
										System.out.println("Aresta nao encontrada!");
									} else {
										System.out.println(ax);
									}
									break;
								}
								case 3: {
									Aresta va = nova_aresta();
									client.atualiza_aresta(va.getV1(), va.getV2(), va);
									break;
								}
								case 4: {
									System.out.println("Qual aresta deseja deletar?");
									int v1 = sc.nextInt();
									int v2 = sc.nextInt();
									client.deleta_aresta(v1, v2);
									break;
								}
								case 5: {
									List<Aresta> l = client.listar_arestas();
									System.out.println("ARESTAS NO GRAFO:");
									System.out.println("");
									for(Aresta vit : l) {
										System.out.println(vit);
									}
									break;
								}
								case 6:
									break;
								default:
									System.out.println("Opcao invalida!");
							}
						}
						break;
					case 3:
						System.out.println("A partir de qual vertice deseja o algoritmo?");
						int opd = sc.nextInt();
						client.dijkstra(opd);
						break;
					case 4:
						break;
					default:
						System.out.println("Opcao invalida!");
				}
			}


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