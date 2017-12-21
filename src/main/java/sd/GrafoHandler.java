package grafo;

import java.util.*;
import static java.lang.Thread.sleep;
import java.util.concurrent.Semaphore;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;
import java.lang.Double;

import io.atomix.copycat.server.storage.StorageLevel;
import io.atomix.*;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.Address;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import io.atomix.concurrent.DistributedLock;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.catalyst.transport.local.LocalTransport;
import io.atomix.catalyst.transport.local.LocalServerRegistry;

public class GrafoHandler implements Grafo.Iface {

    private TTransport []transports;
    private TProtocol []protocols;
    private Grafo.Client []clients;

    private CopycatClient[] copycatClients;

    HashMap<Integer, Semaphore> mutex_vertices = new HashMap<>();
    Semaphore graph_mutex = new Semaphore(1);
    int n, id, ini, vnum, numero_clusters, inicial_cluster,tamanho_cluster;

    void connectToId(int idx) throws TTransportException {
        transports[idx] = new TSocket("localhost", ini + idx);
        transports[idx].open();
        protocols[idx] = new TBinaryProtocol(transports[idx]);
        clients[idx] = new Grafo.Client(protocols[idx]);
        System.out.println("Server " + (ini + id) + " connected to server " + (ini + idx));
    }

    public void buildClusters() {
        for(int i=0;i<numero_clusters;i++) {
            CopycatClient.Builder b = CopycatClient.builder();
            b.withTransport(NettyTransport.builder().withThreads(1).build());
            
            copycatClients[i] = b.build();
            
            copycatClients[i].serializer().register(AdicionaVertice.class);
            copycatClients[i].serializer().register(LeVertice.class);

            System.out.println("Conectando ao Cluster " + i);

            Collection<Address> c = Arrays.asList(
                new Address("localhost", inicial_cluster + tamanho_cluster * i),
                new Address("localhost", inicial_cluster + tamanho_cluster * i + 1),
                new Address("localhost", inicial_cluster + tamanho_cluster * i + 2));

            try {
                CompletableFuture<CopycatClient> f = copycatClients[i].connect(c);
                f.join();
            } catch(Exception e) {
                System.out.println("Falha ao conectar ao Cluster" + i);
            }
        }
    }

    GrafoHandler (int n, int id, int ini, int numero_clusters, int inicial_cluster, int tamanho_cluster) {
        this.id = id;
        this.n = n;
        this.ini = ini;
        this.numero_clusters = numero_clusters;
        this.inicial_cluster = inicial_cluster;
        this.tamanho_cluster = tamanho_cluster;
        vnum = 100;

        transports = new TTransport[n];
        protocols = new TProtocol[n];
        clients = new Grafo.Client[n];

        copycatClients = new CopycatClient[numero_clusters];

        buildClusters();
        
        // for(int i=0;i<n;i++) {
        //     if(i != id) {
        //         boolean conn = false;
        //         int retry = 5;
        //         while(!conn && retry > 0) {
        //             try {
        //                 connectToId(i);
        //                 conn = true;
        //             } catch(TTransportException e) {
        //                 try {
        //                     System.out.println("Couldn't connect to server " + (ini+i) + " ... retrying in " + 1 + " second");
        //                     sleep(1000);
        //                     retry--;
        //                 } catch (InterruptedException ex) {
        //                     System.out.println(ex);
        //                 }
        //         }
        //         }
        //     }
        // }
    }

    int hash(int value) {
        return value % numero_clusters;
    }

    public Vertice findVertice(int nome) {
        int h = hash(nome);
        CompletableFuture<Object> f = copycatClients[h].submit(new LeVertice(nome));
        return (Vertice) f.join();
    }

	@Override
	public void adiciona_vertice(Vertice v) throws org.apache.thrift.TException {
        int h = hash(v.nome);

        CopycatClient c = copycatClients[h];
        Vertice vx = findVertice(v.nome);

		if(vx.nome == -1) {
			System.out.println("Vertice inserido : " + v.nome + "no cluster " + h);

            CompletableFuture<Object> f = c.submit(new AdicionaVertice(v));
            Object resultado = f.join();
		} else {
			System.out.println("Vertice nao inserido! Ja existe no grafo!");
		}
	}

	@Override
    public Vertice le_vertice(int nome) throws org.apache.thrift.TException {
        int h = hash(nome);

        CopycatClient c = copycatClients[h];
        Vertice vx = findVertice(nome);

        if(vx != null) return vx;

    	return new Vertice(-1, -1, "", -1);
    }

    @Override
    public void atualiza_vertice(int nome, Vertice v) throws org.apache.thrift.TException {
        int h = hash(nome);

        CopycatClient c = copycatClients[h];
        CompletableFuture<Object> f = c.submit(new AtualizaVertice(nome, v));
        Object res = f.join();

    }

    @Override
    public void deleta_vertice(int nome) throws org.apache.thrift.TException {

        int h = hash(nome);

        CopycatClient c = copycatClients[h];
        CompletableFuture<Object> f = c.submit(new DeletaVertice(nome));
        Object res = f.join();

        deleta_arestas_vertice(nome);
    }

    @Override
    public void deleta_arestas_vertice(int nome) throws org.apache.thrift.TException {
        int h = hash(nome);
        CopycatClient c = copycatClients[h];
        CompletableFuture<Object> f = c.submit(new ListarArestasVertice(nome));
        List<Aresta> l = (List<Aresta>) f.join(); 

        
        for(Aresta a : l) {
            if(!a.direcionada) {
                f = c.submit(new DeletaAresta(a.v2, nome));
                f.join();
            }
            f = c.submit(new DeletaAresta(a.v1, a.v2));
            f.join();
        }       
    }

    @Override
    public void adiciona_aresta(Aresta a) throws org.apache.thrift.TException {
        int u = a.v1;
        int v = a.v2;

        CopycatClient uc = copycatClients[hash(u)];
        CopycatClient vc = copycatClients[hash(v)];

        Aresta a2 = new Aresta(a); //aresta invertida
        int aux = a2.v1;
        a2.v1 = a2.v2;
        a2.v2 = aux;

        CompletableFuture<Object> f = uc.submit(new LeVertice(u));
        Vertice v1_check = (Vertice) f.join();

        f = vc.submit(new LeVertice(v));
        Vertice v2_check = (Vertice) f.join();

        if (v1_check.getNome() != -1 && v2_check.getNome() != - 1) { //nesse caso os vertices existem na rede

            f = uc.submit(new AdicionaAresta(a));
            boolean res = (boolean) f.join();

            if(res) {
                System.out.println("Adicionada a aresta " + a + "no vertice " + u);
            } else {
                System.out.println("Aresta "+ a +"nao adicionada!");
            }

            if(!a.direcionada) {
                f = vc.submit(new AdicionaAresta(a2));
                res = (boolean) f.join();

                if(res) {
                    System.out.println("Adicionada a aresta " + a2 + "no vertice " + v);
                } else {
                    System.out.println("Aresta "+ a2 +"nao adicionada!");
                }
            }

        } else {

            System.out.println("Algum dos vertices informados na aresta não existem!");

        }
    }

    @Override
    public Aresta le_aresta(int v1, int v2) throws org.apache.thrift.TException {
        int h = hash(v1);
        Aresta ret = new Aresta(-1,-1, -1, false,"");

        CopycatClient c = copycatClients[h];

        CompletableFuture<Object> f = c.submit(new LeVertice(v1));
        Vertice v = (Vertice) f.join();

        if (v.getNome() != -1) {

            f = c.submit(new LeAresta(v1,v2));
            Aresta a = (Aresta) f.join();
            if(a != null) ret = a;
        } else {
            System.out.println("Vertice origem informado nao existe!!");
        }

        return ret;
    }

    @Override
    public void atualiza_aresta(int v1, int v2, Aresta a) throws org.apache.thrift.TException {
        int u = v1;
        int v = v2;

        CopycatClient uc = copycatClients[hash(u)];
        CopycatClient vc = copycatClients[hash(v)];

        Aresta a2 = new Aresta(a); //aresta invertida
        int aux = a2.v1;
        a2.v1 = a2.v2;
        a2.v2 = aux;

        CompletableFuture<Object> f = uc.submit(new LeVertice(u));
        Vertice v1_check = (Vertice) f.join();

        f = vc.submit(new LeVertice(v));
        Vertice v2_check = (Vertice) f.join();

        if (v1_check.getNome() != -1 && v2_check.getNome() != -1) {

            f = uc.submit(new AtualizaAresta(u, v, a));
            boolean res = (boolean) f.join();

            if(res) {
                System.out.println("Aresta alterada!!");
            } else {
                System.out.println("Falha ao alterar aresta!!");
            }

            if (!a.direcionada) {
                f = vc.submit(new AtualizaAresta(v, u, a2));
                res = (boolean) f.join();

                if(res) {
                    System.out.println("Aresta alterada!!");
                } else {
                    System.out.println("Falha ao alterar aresta!!");
                }  
            }
        }
    }
    
    @Override
    public void deleta_aresta(int v1, int v2) throws org.apache.thrift.TException {
        int h = hash(v1);

        CopycatClient c = copycatClients[h];
        CompletableFuture<Object> f = c.submit(new DeletaAresta(v1, v2));
        boolean res = (boolean) f.join();

        if(res) {
            System.out.println("Deletado com sucesso!");
        } else {
            System.out.println("Falha ao deletar!");
        }
    }
    
    @Override
    public List<Vertice> listar_vertices() throws org.apache.thrift.TException {
        List<Vertice> aux = new ArrayList();

        for(int i = 0; i < numero_clusters; i++) {
            System.out.println(i);
            CopycatClient c = copycatClients[i];
            CompletableFuture f = c.submit(new ListarVerticesLocal());
            List<Vertice> res = (List<Vertice>) f.join();
            if(res != null) aux.addAll(res);
        }

        return aux;
    }

    @Override
    public List<Vertice> listar_vertices_local() throws org.apache.thrift.TException {
        CopycatClient c = copycatClients[id/tamanho_cluster];
        CompletableFuture f = c.submit(new ListarVerticesLocal());
        List<Vertice> aux = (List<Vertice>) f.join();

        System.out.println("\nListando vertices do Cluster " + id/tamanho_cluster);

        for(Vertice v : aux) {
            System.out.println(v);
        }

        System.out.println("");

        return aux;
    }

    @Override
    public List<Aresta> listar_arestas() throws org.apache.thrift.TException {
        List<Aresta> aux = new ArrayList();

        for(int i = 0; i < numero_clusters; i++) {
            CompletableFuture f = copycatClients[i].submit(new ListarArestasLocal());
            List<Aresta> res = (List<Aresta>) f.join();
            aux.addAll(res);
        }

        return aux;
    }

    @Override
    public List<Aresta> listar_arestas_local() throws org.apache.thrift.TException {
        CopycatClient c = copycatClients[id/tamanho_cluster];
        CompletableFuture f = c.submit(new ListarArestasLocal());
        List<Aresta> aux = (List<Aresta>) f.join();

        System.out.println("\nListando arestas do Cluster " + id/tamanho_cluster);

        for(Aresta v : aux) {
            System.out.println(v);
        }

        System.out.println("");

        return aux;
    }

    @Override
    public List<Aresta> listar_arestas_vertice(int nome) throws org.apache.thrift.TException {
        System.out.println("====== O vertice " + nome + " contem as seguintes arestas:");
        

        int h = hash(nome);
        
        CopycatClient c = copycatClients[h];
        CompletableFuture<Object> f = c.submit(new ListarArestasVertice(nome));
        List<Aresta> l = (List<Aresta>)f.join();


        for(Aresta a : l) {
            System.out.println(a);
        }

        return l;
    }

    @Override
    public List<Vertice> listar_vizinhos_vertice(int nome) throws org.apache.thrift.TException {
        List<Aresta> aux = new ArrayList();
        List<Vertice> ans = new ArrayList();

        System.out.println("O vertice " + nome + " é vizinho dos seguintes vertices:");

        for(int i = 0; i < numero_clusters; i++) {
            CompletableFuture f = copycatClients[i].submit(new ListarArestasLocal());
            aux = (List<Aresta>) f.join();

            for(Aresta a : aux) {
                if(a.v1 == nome) {
                    Vertice v = le_vertice(a.v2);
                    System.out.println(v);
                    ans.add(v);
                }
            }
        }

        System.out.println("");

        return ans;
     }

    @Override 
    public List<Double> dijkstra(int nome) throws org.apache.thrift.TException{
        //retorna uma lista de distancias do vertice nome para todos os outros

        // int h = hash(nome);
        // if(h != id) {
        //     double[] ans = clients[h].dijkstra(nome);
        //     return ans;
        // }

        double[] d = new double[vnum + 1];
        boolean visited[] = new boolean[vnum + 1];

        for(int i=0;i<vnum;i++) {
            d[i] = 1<<30;
            visited[i] = false;
        }

        d[nome] = 0;

        for(int i=0;i<vnum-1;i++) {
            int minidx = 0;
            for(int j=1;j<vnum;j++) {
                if(!visited[j] && d[minidx] > d[j]) {
                    minidx = j;
                }
            }
            if(minidx == 0) break;
            visited[minidx] = true;
            try{
                for(Aresta e : listar_arestas_vertice(minidx)) {
                    if(d[minidx] + e.peso < d[e.v2]) d[e.v2] = d[minidx] + e.peso;
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }  

        System.out.println("\n\n====== DISTANCIA DO VERTICE " + nome + " PARA TODOS =========\n\n");

        for(int i=0;i<vnum;i++) {
            if(le_vertice(i).nome != -1) {
                if(d[i] != (1<<30))System.out.println("d[" + i + "] = " + d[i]);
                else System.out.println("d[" + i + "] = INF");
            }
        }

        List<Double> list = new ArrayList();
        for(int i=0;i<vnum;i++) {
            if(d[i] != (1<<30)) list.add(new Double(d[i]));
        }

        return list;

    }

    public void graph_mutex_acquire() throws org.apache.thrift.TException  {
        try {
            graph_mutex.acquire();
        } catch(InterruptedException e) {
            e.printStackTrace();
            graph_mutex.release();
        }
    }

    public void graph_mutex_release() throws org.apache.thrift.TException {
        graph_mutex.release();
    }
}