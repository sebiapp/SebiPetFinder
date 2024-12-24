package com.riberadeltajo.sebipetfinder.Mensajes;

public class Mensaje {
    private int id;
    private int emisorId;
    private int receptorId;
    private String mensaje;
    private String fechaEnvio;
    private boolean leido;

    public Mensaje(int id, int emisorId, int receptorId, String mensaje, String fechaEnvio, boolean leido) {
        this.id = id;
        this.emisorId = emisorId;
        this.receptorId = receptorId;
        this.mensaje = mensaje;
        this.fechaEnvio = fechaEnvio;
        this.leido = leido;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmisorId() {
        return emisorId;
    }

    public void setEmisorId(int emisorId) {
        this.emisorId = emisorId;
    }

    public int getReceptorId() {
        return receptorId;
    }

    public void setReceptorId(int receptorId) {
        this.receptorId = receptorId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }
}