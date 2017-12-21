namespace java grafo

struct Vertice { 1:i32 nome, 2:i32 cor, 3:string descricao, 4:double peso }

struct Aresta { 1:i32 v1, 2:i32 v2, 3:double peso, 4:bool direcionada, 5:string descricao}



service Grafo {
	void adiciona_vertice(1:Vertice v);
	Vertice le_vertice(1:i32 nome);
	void atualiza_vertice(1:i32 nome, 2:Vertice v);
	void deleta_vertice(1:i32 nome);
	void deleta_arestas_vertice(1:i32 nome);

	void adiciona_aresta(1: Aresta a);
	Aresta le_aresta(1:i32 v1, 2:i32 v2);
	void atualiza_aresta(1:i32 v1, 2:i32 v2, 3: Aresta a);
	void deleta_aresta(1:i32 v1, 2:i32 v2);

	list<Vertice> listar_vertices();
	list<Vertice> listar_vertices_local();
	list<Aresta> listar_arestas();
	list<Aresta> listar_arestas_local();
	list<Aresta> listar_arestas_vertice(1:i32 nome);
	list<Vertice> listar_vizinhos_vertice(1:i32 nome);

	list<double> dijkstra(1:i32 nome);

	void graph_mutex_acquire();
	void graph_mutex_release();

}