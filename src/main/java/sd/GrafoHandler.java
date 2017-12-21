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

	HashMap<Integer, Vertice> vertices = new HashMap<>();
    HashMap<Integer, List<Aresta> > AdjList = new HashMap<>();
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

        System.out.println("Adiciona called");

        CopycatClient c = copycatClients[h];
        Vertice vx = findVertice(v.nome);

        System.out.println("findVertice called");

		if(vx.nome == -1) {
			System.out.println("Vertice inserido : " + v.nome + "no cluster " + h);
			vertices.put(v.nome, v);
            AdjList.put(v.nome, new ArrayList());
            if(v.nome > vnum) vnum = v.nome;

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

        if(h != id) {
            clients[h].atualiza_vertice(nome, v);
            return;
        }

        if(vertices.containsKey(nome)) {
            vertices.put(nome, v);
        } else {
            System.out.println("Vertice " + nome + " nao existe no Grafo! ");
        }
    }

    @Override
    public void deleta_vertice(int nome) throws org.apache.thrift.TException {

        int h = hash(nome);

        if(h != id) {
            clients[h].deleta_vertice(nome);
            return;
        }

        if(vertices.containsKey(nome)) {
            vertices.remove(nome);
            deleta_arestas_vertice(nome);
            // for(int i = 0; i < n; i++)
            //     if(i != id)
            //         clients[i].deleta_arestas_vertice(nome);
        }
    }

    @Override
    public void deleta_arestas_vertice(int nome) throws org.apache.thrift.TException {
        if(AdjList.containsKey(nome)) {
            for(Aresta a : AdjList.get(nome)) {
                if(!a.direcionada) {
                    deleta_aresta(a.v2, nome);
                }
            }
            AdjList.remove(nome);
        }        
    }

    @Override
    public void adiciona_aresta(Aresta a) throws org.apache.thrift.TException {
        int u = a.v1;
        int v = a.v2;

        int h = hash(u);

        if(h != id) {
            clients[h].adiciona_aresta(a);
            return;
        }

        Aresta a2 = new Aresta(a);
        int aux = a2.v1;
        a2.v1 = a2.v2;
        a2.v2 = aux;

        Vertice v1_check = le_vertice(u);
        Vertice v2_check = le_vertice(v);

        if (v1_check != null && v2_check != null) { //nesse caso os vertices existem na rede

            if(!AdjList.containsKey(u)) {
                AdjList.put(u, new ArrayList());
            }

            if(!AdjList.containsKey(v)) {
                AdjList.put(v, new ArrayList());
            }
            
            if(!AdjList.get(u).contains(a)) {
                System.out.println("Adicionada a aresta " + a + "no vertice " + u);
                AdjList.get(u).add(a);

            }

            if(!a.direcionada && !AdjList.get(v).contains(a2)) {
                AdjList.get(v).add(a2);
            }

        } else {

            System.out.println("Algum dos vertices informados na aresta não existem!");

        }
    }

    @Override
    public Aresta le_aresta(int v1, int v2) throws org.apache.thrift.TException {
        int h = hash(v1);

        if(h != id) {
            return clients[h].le_aresta(v1, v2);
        }

        Vertice v = le_vertice(v1);

        if (v != null && AdjList.containsKey(v1)) {
            for(Aresta a : AdjList.get(v1)) {
                if (a.v1 == v1 && a.v2 == v2) {
                    return a;
                }
            }
        }

        return null;
    }

    @Override
    public void atualiza_aresta(int v1, int v2, Aresta a) throws org.apache.thrift.TException {
        int h = hash(v1);

        if(h != id) {
            clients[h].atualiza_aresta(v1, v2, a);
            return;
        }

        Vertice v1_check = le_vertice(v1);
        Vertice v2_check = le_vertice(v2);

        if (v1_check != null && v2_check != null) {
            if(!AdjList.containsKey(v1)) {
                AdjList.put(v1, new ArrayList());
            }

            for(int i = 0; i < AdjList.get(v1).size(); i++) {
                if(AdjList.get(v1).get(i).v1 == v1 && AdjList.get(v1).get(i).v2 == v2) {                    
                    AdjList.get(v1).set(i, a);
                    System.out.println("Aresta alterada! " + AdjList.get(v1).get(i));
                }
            }

            if (!a.direcionada) {
                if(!AdjList.containsKey(v2)) {
                    AdjList.put(v2, new ArrayList());
                }
                for(int i = 0; i < AdjList.get(v2).size(); i++) {
                    if(AdjList.get(v2).get(i).v1 == v2 && AdjList.get(v2).get(i).v2 == v1) {
                        Aresta a2 = new Aresta(a);
                        int ax = a2.v1;
                        a2.v1 = a2.v2;
                        a2.v2 = ax;
                        AdjList.get(v2).set(i, a2);
                        // System.out.println("Aresta alterada! " + AdjList.get(v2).get(i));
                    }
                }   
            }
        }
    }
    
    @Override
    public void deleta_aresta(int v1, int v2) throws org.apache.thrift.TException {
        int h = hash(v1);

        if(h != id) {
            clients[h].deleta_aresta(v1, v2);
            return;
        }

        for(int i = 0; i < AdjList.get(v1).size(); i++) {
            if (AdjList.get(v1).get(i).v1 == v1 && AdjList.get(v1).get(i).v2 == v2) {
                AdjList.get(v1).remove(i);
            }
        }
    }
    
    @Override
    public List<Vertice> listar_vertices() throws org.apache.thrift.TException {
        List<Vertice> aux = listar_vertices_local();

        for(int i = 0; i < n; i++) {
            if (i != id) {
                aux.addAll(clients[i].listar_vertices_local());
            }
        }

        return aux;
    }

    @Override
    public List<Vertice> listar_vertices_local() throws org.apache.thrift.TException {
        List<Vertice> aux = new ArrayList();

        System.out.println("\nListando vertices do Server " + id);

        for(Vertice v : vertices.values()) {
            System.out.println(v);
            aux.add(v);
        }

        System.out.println("");

        return aux;
    }

    @Override
    public List<Aresta> listar_arestas() throws org.apache.thrift.TException {
        List<Aresta> aux = listar_arestas_local();

        for(int i = 0; i < n; i++) {
            if (i != id) {
                aux.addAll(clients[i].listar_arestas_local());
            }
        }

        return aux;
    }

    @Override
    public List<Aresta> listar_arestas_local() throws org.apache.thrift.TException {
        List<Aresta> aux = new ArrayList();

        System.out.println("\nListando arestas no Server " + id + ".");

        for(List<Aresta> l : AdjList.values()) {
            for (Aresta x : l) {
                System.out.println(x);
                aux.add(x);
            }
        }

        System.out.println("");

        return aux;
    }

    @Override
    public List<Aresta> listar_arestas_vertice(int nome) throws org.apache.thrift.TException {
        System.out.println("====== O vertice " + nome + " contem as seguintes arestas:");
        

        int h = hash(nome);
        if(h != id) {
            List<Aresta> ans = clients[h].listar_arestas_vertice(nome);
            return ans;
        }

        if(!AdjList.containsKey(nome)) {
            System.out.println("Vertice nao existe o grafo!");
            return new ArrayList();
        }

        for(Aresta a : AdjList.get(nome)) {
            System.out.println(a);
        }

        return AdjList.get(nome);
    }

    @Override
    public List<Vertice> listar_vizinhos_vertice(int nome) throws org.apache.thrift.TException {
        List<Aresta> aux = new ArrayList();
        List<Vertice> ans = new ArrayList();

        System.out.println("O vertice " + nome + " é vizinho dos seguintes vertices:");

        for(int i = 0; i < 5; i++) {
            if(id != i) aux = clients[i].listar_arestas_local();
            else aux = listar_arestas_local();
            for(Aresta a : aux) {
                if(a.v1 == nome) {
                    Vertice v = le_vertice(a.v2);
                    System.out.println(v);
                    ans.add( v );
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