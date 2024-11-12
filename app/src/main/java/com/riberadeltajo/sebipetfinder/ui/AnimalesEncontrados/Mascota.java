package com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados;

public class Mascota {
    private String nombre;
    private String descripcion;
    private String fotoUrl;
    private String telefono;
    private String ciudad;

    public Mascota(String nombre, String descripcion, String fotoUrl, String telefono,String ciudad) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fotoUrl = fotoUrl;
        this.telefono = telefono;
        this.ciudad = ciudad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

}
