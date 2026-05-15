package repository;

import api.PokemonApi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PokemonRepository {
    private static final String URL = "jdbc:h2:./pokemon_db";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    Connection connection;
    PokemonApi api;

    public PokemonRepository() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        api = new PokemonApi();
        System.out.println("Conectado com sucesso");
        criarTabelaPokemon();
        criarTabelaMovimentos();
        criarTabelaEvolucaoPokemon();
    }

    private void criarTabelaPokemon() {
        try (Statement st = connection.createStatement()) {

            st.execute("""
                    CREATE TABLE IF NOT EXISTS pokemon (
                        id INT PRIMARY KEY UNIQUE,
                        nome VARCHAR(200) NOT NULL UNIQUE,
                        tipo VARCHAR(200),
                        fraqueza VARCHAR(500)
                    )
                    """);

            System.out.println("Tabela pokemon criada com sucesso");
        } catch (SQLException e) {
            System.out.println("Erro ao criar tabela pokemon " + e.getMessage());
        }
    }

    private void criarTabelaMovimentos() {

        try (Statement st = connection.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS movimentos_pkm (
                        id INT PRIMARY KEY UNIQUE,
                        pokemon_id INT,
                        nome VARCHAR(200),
                        level INT,
                        FOREIGN KEY (pokemon_id) REFERENCES pokemon(id)
                    )
                    """);
            System.out.println("Tabela movimentos criada com sucesso");
        } catch (SQLException e) {
            System.out.println("Erro ao criar tabela movimentos " + e.getMessage());
        }
    }

    private void criarTabelaEvolucaoPokemon() {

        try (Statement st = connection.createStatement()) {

            st.execute("""
                    CREATE TABLE IF NOT EXISTS evolucao_pkm (
                        id INT PRIMARY KEY UNIQUE,
                        pokemon_id INT,
                        evolui_para VARCHAR(200),
                        FOREIGN KEY (pokemon_id) REFERENCES pokemon(id)
                    )
                    """);
            System.out.println("Tabela evolução criada com sucesso");
        } catch (SQLException e) {
            System.out.println("Erro ao criar tabela evolução" + e.getMessage());
        }
    }

    public void salvar(int id, String nome,
                       List<String> tipos,
                       List<String> fraquezas,
                       List<String[]> ataques) throws SQLException {

        try (PreparedStatement pstmt = connection.prepareStatement(
                "MERGE INTO pokemon(id, nome, tipo, fraqueza) KEY (id) VALUES (?, ?, ?, ?)")) {
            pstmt.setInt(1, id);
            pstmt.setString(2, nome);
            pstmt.setString(3, String.join(",", tipos));
            pstmt.setString(4, String.join(",", fraquezas));
            pstmt.executeUpdate();
        }

        if (ataques != null && !ataques.isEmpty()) {
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO movimentos_pkm (id, pokemon_id, nome, level) VALUES (?, ?, ?, ?)")) {

                int movimentoIdBase = id * 1000;

                for (int i = 0; i < ataques.size(); i++) {
                    String[] dadosAtaque = ataques.get(i);
                    if (dadosAtaque.length >= 2) {
                        pstmt.setInt(1, movimentoIdBase + i);
                        pstmt.setInt(2, id);
                        pstmt.setString(3, dadosAtaque[0]);
                        pstmt.setInt(4, Integer.parseInt(dadosAtaque[1]));
                        pstmt.addBatch();
                    }
                }
                pstmt.executeBatch();
            }
        }
    }

    public void salvarPokemonPeloJson(int id, String jsonBruto) throws SQLException {
        try {
            // 1. Localizamos a posição da palavra "forms", pois o nome real está dentro dela
            String ancora = "\"forms\"";
            int posicaoAncora = jsonBruto.indexOf(ancora);

            if (posicaoAncora == -1) {
                throw new RuntimeException("Não foi possível encontrar a seção 'forms' no JSON.");
            }

            // 2. Agora buscamos o "name" APÓS a posição da âncora
            String chaveNome = "\"name\":\"";
            int inicio = jsonBruto.indexOf(chaveNome, posicaoAncora) + chaveNome.length();
            int fim = jsonBruto.indexOf("\"", inicio);

            String nome = jsonBruto.substring(inicio, fim);

            // Listas vazias para manter compatibilidade com seu método salvar original
            List<String> tipos = new ArrayList<>();
            tipos.add("desconhecido");
            List<String> fraquezas = new ArrayList<>();
            List<String[]> ataques = new ArrayList<>();

            // 3. Salva no banco
            this.salvar(id, nome, tipos, fraquezas, ataques);

            System.out.println("Pokemon " + nome + " (ID: " + id + ") salvo com sucesso!");

        } catch (Exception e) {
            throw new SQLException("Erro ao processar JSON: " + e.getMessage());
        }
    }

    public void buscarPorNome(String nome) {
        String sql = "SELECT * FROM pokemon WHERE nome = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nome);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getInt("id"));
                    System.out.println(rs.getString("nome"));
                    System.out.println(rs.getString("tipo")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }


    public void listarPokemons() {
        String selectQuery = "SELECT * FROM pokemon";

        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            ResultSet rs = pstmt.executeQuery();
            System.out.println("--- LISTANDO TODOS OS POKEMONS ---");

            boolean encontrouAlgum = false;

            while (rs.next()) {
                encontrouAlgum = true;
                System.out.println("ID: " + rs.getInt("id") +
                        " | Nome: " + rs.getString("nome") +
                        " | Tipo: " + rs.getString("tipo"));
            }

            if (!encontrouAlgum) {
                System.out.println("O banco de dados está vazio.");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar: " + e.getMessage());
        }
    }


    public void close() throws SQLException {
        connection.close();
    }

}