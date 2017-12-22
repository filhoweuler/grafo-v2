package grafo;

import java.util.ArrayList;

import io.atomix.*;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.Address;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.storage.StorageLevel;
import io.atomix.concurrent.DistributedLock;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.Command;
import io.atomix.copycat.Query;

import java.util.HashMap;
import java.util.List;

public class GrafoStateMachine extends StateMachine {

	private HashMap<Integer, Vertice> vertices = new HashMap<>();
    private HashMap<Integer, List<Aresta> > AdjList = new HashMap<>();
    public int vnum = 0;


	public GrafoStateMachine() {
		vnum = 100;
		//System.out.println("Mais uma machine");
	}

	public Object adiciona_vertice(Commit<AdicionaVertice> commit) {
		try {
			Vertice v = (Vertice) commit.operation().v();
			vertices.put(v.nome, v);
			AdjList.put(v.nome, new ArrayList());
			if(v.nome > vnum) vnum = v.nome;
			System.out.println("Vertice criado!");
		} catch (Exception e) {
			return false;
		} finally {
			commit.release();
		}
		return true;
	}

	public Object le_vertice(Commit<LeVertice> commit) {
		Vertice v = new Vertice(-1, -1, "", -1);

		try {
			int nome = (int) commit.operation().nome();
			if (vertices.containsKey(nome))
    			v = vertices.get(nome);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			commit.release();
		}

		return v;
	}

	public Object atualiza_vertice(Commit<AtualizaVertice> commit) {
		try {
			int nome = (int)commit.operation().nome();
			Vertice v = (Vertice)commit.operation().v();

			if(vertices.containsKey(nome)) {
				vertices.put(nome, v);
			} else {
				System.out.println("Vertice " + nome + " nao existe no Grafo! ");
			}
		} catch(Exception e) {
			return false;
		} finally {
			commit.release();
		}

		return true;
	}

	public Object deleta_vertice(Commit<DeletaVertice> commit) {
		try {
			int nome = (int)commit.operation().nome();

			if(vertices.containsKey(nome)) {
				vertices.remove(nome);
				//deleta_arestas_vertice(new DeletaArestasVertice(nome));
			}
		} catch (Exception e) {
			return false;
		} finally {
			commit.release();
		}
		return true;
	}

	public Object adiciona_aresta(Commit<AdicionaAresta> commit) {
		try {
			Aresta a = (Aresta) commit.operation().a();
			int u = a.v1;
			int v = a.v2;
			if(!AdjList.get(u).contains(a))
				AdjList.get(u).add(a);
			else
				return false;
		} catch (Exception e) {
			return false;
		} finally {
			commit.release();
		}
		return true;
	}

	public Object le_aresta(Commit<LeAresta> commit) {
		try {
			int u = (int) commit.operation().v1();
			int v = (int) commit.operation().v2();

			for(Aresta a : AdjList.get(u)) {
				if(u == a.v1 && v == a.v2)
					return a;
			}
		} catch (Exception e) {
			return null;
		} finally {
			commit.release();
		}
		return null;
	}

	public Object atualiza_aresta(Commit<AtualizaAresta> commit) {
		try {
			int u = (int) commit.operation().v1();
			int v = (int) commit.operation().v2();
			Aresta a = (Aresta) commit.operation().a();

			for(int i = 0; i < AdjList.get(u).size(); i++) {
                if(AdjList.get(u).get(i).v1 == u && AdjList.get(u).get(i).v2 == v) {                    
                    AdjList.get(u).set(i, a);
                    //System.out.println("Aresta alterada! " + AdjList.get(u).get(i));
                    return true;
                }
            }
		} catch (Exception e) {
			return false;
		} finally {
			commit.release();
		}
		return false;
	}

	public Object deleta_aresta(Commit<DeletaAresta> commit) {
		try {
			int v1 = (int)commit.operation().v1();
			int v2 = (int)commit.operation().v2();

			for(int i=0;i<AdjList.get(v1).size();i++) {
				if(AdjList.get(v1).get(i).v1 == v1 && AdjList.get(v1).get(i).v2 == v2) {
					AdjList.get(v1).remove(i);
				}
			}
		} catch (Exception e) {
			return false;
		} finally {
			commit.release();
		}
		return true;
	}

	public Object listar_vertices_local(Commit<ListarVerticesLocal> commit) {
		commit.release();
		
		return new ArrayList(vertices.values());
	}

	public Object listar_arestas_local(Commit<ListarArestasLocal> commit) {
		commit.release();

		List<Aresta> aux = new ArrayList();

		for(List<Aresta> l : AdjList.values()) {
            aux.addAll(l);
        }

		return aux;
	}

	public Object listar_arestas_vertice(Commit<ListarArestasVertice> commit) {
		int nome = (int)commit.operation().nome();
		commit.release();
		return AdjList.get(nome);
	}

}

class AdicionaVertice implements Command<Object> {
	private final Object v;

	public AdicionaVertice(Object v) {
		this.v = v;
	}

	public Object v() {
		return v;
	}
} 

class LeVertice implements Query<Object> {
	private final Object nome;

	public LeVertice(Object nome) {
		this.nome = nome;
	}

	public Object nome() {
		return nome;
	}
}

class AtualizaVertice implements Command<Object> {
	private final Object nome;
	private final Object v;

	public AtualizaVertice(Object nome, Object v) {
		this.nome = nome;
		this.v = v;
	}

	public Object nome() { return nome; }

	public Object v() { return v; }

}

class DeletaVertice implements Command<Object> {
	private final Object nome;

	public DeletaVertice(Object nome) {
		this.nome = nome;
	}

	public Object nome() {
		return nome;
	}	
}

class AdicionaAresta implements Command<Object> {
	private final Object a;

	public AdicionaAresta(Object a) {
		this.a = a;
	}

	public Object a() {
		return a;
	}
}

class LeAresta implements Query<Object> {
	private final Object v2;
	private final Object v1;

	public LeAresta(Object v1, Object v2) {
		this.v2 = v2;
		this.v1 = v1;
	}

	public Object v2() { return v2; }

	public Object v1() { return v1; }
}

class AtualizaAresta implements Command<Object> {

	private final Object v2;
	private final Object v1;
	private final Object a;

	public AtualizaAresta(Object v1, Object v2, Object a) {
		this.v2 = v2;
		this.v1 = v1;
		this.a = a;
	}

	public Object v2() { return v2; }

	public Object v1() { return v1; }

	public Object a() { return a; }
	
}

class DeletaAresta implements Command<Object> {
	private final Object v2;
	private final Object v1;

	public DeletaAresta(Object v1, Object v2) {
		this.v2 = v2;
		this.v1 = v1;
	}

	public Object v2() { return v2; }

	public Object v1() { return v1; }
}

class ListarVerticesLocal implements Query<Object> {}

class ListarArestasLocal implements Query<Object> {}

class ListarArestasVertice implements Query<Object> {
	private final Object nome;

	public ListarArestasVertice(Object nome) {
		this.nome = nome;
	}

	public Object nome() { return nome; }
}

class ListarVizinhosVertice implements Query<Object> {
	private final Object nome;

	public ListarVizinhosVertice(Object nome) {
		this.nome = nome;
	}

	public Object nome() { return nome; }
}

class Dijkstra implements Query<Object> {
	private final Object nome;

	public Dijkstra(Object nome) {
		this.nome = nome;
	}

	public Object nome() { return nome; }
}