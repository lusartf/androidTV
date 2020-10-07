package com.example.theoplayertv.models;

public class Categoria {

    private int id;         // Id de Categoria API
    private String name;    // Nombre categoria API
    private String icon;    // URL de Imagen de Categoria API

    public Categoria(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Categoria(int id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return name;
    }
}
