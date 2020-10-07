package com.example.theoplayertv.models;

public class Canal {
    private int logo;
    private String nombre;
    private String categoria;
    private String url;

    public Canal() {

    }

    public Canal(int logo, String nombre, String categoria, String url) {
        this.logo = logo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.url = url;
    }

    public int getLogo() {
        return logo;
    }

    public void setLogo(int logo) {
        this.logo = logo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
