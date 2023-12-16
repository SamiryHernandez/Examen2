package com.uth.examen.clases;

import java.io.Serializable;
import java.sql.Blob;
import java.util.Date;

public class Entrevistas implements Serializable {
    private String idOrden;
    private String descripcion;
    private String periodista;
    private String fecha;
    private String imagen;
    private String audio;

    public Entrevistas() {
    }

    public Entrevistas(String idOrden, String descripcion, String periodista, String fecha, String imagen, String audio) {
        this.idOrden = idOrden;
        this.descripcion = descripcion;
        this.periodista = periodista;
        this.fecha = fecha;
        this.imagen = imagen;
        this.audio = audio;
    }

    public String getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(String idOrden) {
        this.idOrden = idOrden;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPeriodista() {
        return periodista;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}
