package com.riberadeltajo.sebipetfinder.Mensajes;

public class Conversacion {
    private int anuncioId;
    private String tituloAnuncio;
    private String imagenUrl;
    private String ultimoMensaje;
    private String fechaUltimoMensaje;
    private int mensajesNoLeidos;
    private String nombreContacto;
    private String tipoAnuncio;
    private String otroUsuarioId;

    public Conversacion(int anuncioId, String tituloAnuncio, String imagenUrl, String ultimoMensaje, String fechaUltimoMensaje, int mensajesNoLeidos, String nombreContacto, String tipoAnuncio, String otroUsuarioId) {
        this.anuncioId = anuncioId;
        this.tituloAnuncio = tituloAnuncio;
        this.imagenUrl = imagenUrl;
        this.ultimoMensaje = ultimoMensaje;
        this.fechaUltimoMensaje = fechaUltimoMensaje;
        this.mensajesNoLeidos = mensajesNoLeidos;
        this.nombreContacto = nombreContacto;
        this.tipoAnuncio = tipoAnuncio;
        this.otroUsuarioId = otroUsuarioId;
    }

    public int getAnuncioId() {
        return anuncioId;
    }

    public void setAnuncioId(int anuncioId) {
        this.anuncioId = anuncioId;
    }

    public String getTituloAnuncio() {
        return tituloAnuncio;
    }

    public void setTituloAnuncio(String tituloAnuncio) {
        this.tituloAnuncio = tituloAnuncio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public void setUltimoMensaje(String ultimoMensaje) {
        this.ultimoMensaje = ultimoMensaje;
    }

    public String getFechaUltimoMensaje() {
        return fechaUltimoMensaje;
    }

    public void setFechaUltimoMensaje(String fechaUltimoMensaje) {
        this.fechaUltimoMensaje = fechaUltimoMensaje;
    }

    public int getMensajesNoLeidos() {
        return mensajesNoLeidos;
    }

    public void setMensajesNoLeidos(int mensajesNoLeidos) {
        this.mensajesNoLeidos = mensajesNoLeidos;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getTipoAnuncio() {
        return tipoAnuncio;
    }

    public void setTipoAnuncio(String tipoAnuncio) {
        this.tipoAnuncio = tipoAnuncio;
    }

    public String getOtroUsuarioId() {
        return otroUsuarioId;
    }

    public void setOtroUsuarioId(String otroUsuarioId) {
        this.otroUsuarioId = otroUsuarioId;
    }
}
