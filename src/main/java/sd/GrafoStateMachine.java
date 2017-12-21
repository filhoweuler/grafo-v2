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
		System.out.println("Mais uma machine");
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

class LeVertice implements Command<Object> {
	private final Object nome;

	public LeVertice(Object nome) {
		this.nome = nome;
	}

	public Object nome() {
		return nome;
	}
}