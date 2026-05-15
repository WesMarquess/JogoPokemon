package Application;

import api.PokemonApi;
import repository.PokemonRepository;

public class Main {
    public static void main(String[] args) {
        try {
            PokemonApi api = new PokemonApi();
            PokemonRepository repository = new PokemonRepository();

            //repository.listarPokemons();
            repository.buscarPorNome("pikachu");
            //repository.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}