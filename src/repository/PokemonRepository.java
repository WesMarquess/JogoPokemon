package repository;

import model.Pokemon;

import java.sql.*;
import java.util.List;

public class PokemonRepository {
    private static final String URL = "jdbc:h2:./pokemon_db";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private final Connection connection;

    public PokemonRepository(Connection connection) throws SQLException {
        this.connection = connection;
        criarTabelas();
    }

    public void criarTabelas() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS pokemon(
                    id INT NOT NULL PRIMARY KEY,
                    nome VARCHAR(100),
                    tipo VARCHAR(100),
                    fraqueza VARCHAR(100)
                    );
                    """);

            st.execute("""
                        CREATE TABLE IF NOT EXISTS pokemon_movimento (
                            pokemon_id  INT,
                            nome        VARCHAR(100),
                            pp_maximo   INT,
                            tipo        VARCHAR(50),
                            precisao    INT
                        )
                    """);
            st.close();
        }
    }

    public void salvar(Pokemon pokemon) throws SQLException {
        if (existePorId(pokemon.getId())) {
            System.out.println("Pokémon ID " + pokemon.getId() + " já existe, pulando...");
            return;
        }

        String sqlPokemon = "INSERT INTO pokemon (id, nome) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sqlPokemon)) {
            ps.setInt(1, pokemon.getId());
            ps.setString(2, pokemon.getNome());
            ps.executeUpdate();
        }

        inserirLista(pokemon.getId(), pokemon.getTipos(), "pokemon_tipo", "tipo");

        inserirLista(pokemon.getId(), pokemon.getFraquezas(), "pokemon_fraqueza", "fraqueza");

        inserirLista(pokemon.getId(), pokemon.getAtaques(), "pokemon_ataque", "ataque");
    }

    private void inserirLista(int pokemonId, List<String> valores,
                              String tabela, String coluna) throws SQLException {

        if (valores == null || valores.isEmpty()) return;

        String sql = "INSERT INTO " + tabela + " (pokemon_id, " + coluna + ") VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (String valor : valores) {
                ps.setInt(1, pokemonId);
                ps.setString(2, valor);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private boolean existePorId(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pokemon WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }


    public void close() throws SQLException {
        connection.close();
    }
}
