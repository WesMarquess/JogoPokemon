package model;

import java.util.List;

public class Pokemon {

    private Integer id;
    private String nome;
    private List<String> tipos;
    private List<String> fraquezas;
    private List<String> ataques;

    public Pokemon(Integer id, String nome, List<String> tipos, List<String> fraquezas, List<String> ataques) {
        this.id = id;
        this.nome = nome;
        this.tipos = tipos;
        this.fraquezas = fraquezas;
        this.ataques = ataques;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public List<String> getTipos() {
        return tipos;
    }

    public List<String> getFraquezas() {
        return fraquezas;
    }

    public List<String> getAtaques() {
        return ataques;
    }
}
