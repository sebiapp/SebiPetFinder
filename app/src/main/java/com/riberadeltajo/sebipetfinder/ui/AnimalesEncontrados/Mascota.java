package com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados;

public class Mascota {
    private int id;
    private String nombre;
    private String descripcion;
    private String fotoUrl;
    private String telefono;
    private String ciudad;
    private String user_id;
    private int isMascotaPerdida;

    public Mascota(int id,String nombre, String descripcion, String fotoUrl, String telefono, String ciudad, String user_id, int isMascotaPerdida) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fotoUrl = fotoUrl;
        this.telefono = telefono;
        this.ciudad = ciudad;
        this.user_id = user_id;
        this.isMascotaPerdida = isMascotaPerdida;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
    public boolean isMascotaPerdida() {
        return isMascotaPerdida == 1;
    }

    public void setMascotaPerdida(int mascotaPerdida) {
        this.isMascotaPerdida = mascotaPerdida;
    }
}
