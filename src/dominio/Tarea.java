/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dominio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Tarea {
    private int id;
    private String titulo;
    private Prioridad prioridad;
    private boolean estado; // true=Hecho, false=Pendiente
    private boolean especial; // Marca especial (★)
    private LocalDate fecha;
    private LocalDate fechaCreacion;
    private boolean activa; // Para soft delete
    
    // Constructor completo
    public Tarea(int id, String titulo, Prioridad prioridad, boolean estado, 
                 boolean especial, LocalDate fecha, LocalDate fechaCreacion, boolean activa) {
        this.id = id;
        this.titulo = titulo;
        this.prioridad = prioridad;
        this.estado = estado;
        this.especial = especial;
        this.fecha = fecha;
        this.fechaCreacion = fechaCreacion;
        this.activa = activa;
    }
    
    // Constructor para nueva tarea
    public Tarea(String titulo, Prioridad prioridad, boolean especial, LocalDate fecha) 
            throws TareaException {
        validarTitulo(titulo);
        validarPrioridad(prioridad);
        
        this.titulo = titulo;
        this.prioridad = prioridad;
        this.especial = especial;
        this.fecha = fecha;
        this.estado = false; // Pendiente por defecto
        this.fechaCreacion = LocalDate.now();
        this.activa = true;
    }
    
    // Constructor vacío
    public Tarea() {
        this.estado = false;
        this.activa = true;
        this.fechaCreacion = LocalDate.now();
    }
    
    // Validaciones
    private void validarTitulo(String titulo) throws TareaException {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new TareaException("El título no puede estar vacío");
        }
        if (titulo.length() > 255) {
            throw new TareaException("El título no puede exceder 255 caracteres");
        }
    }
    
    private void validarPrioridad(Prioridad prioridad) throws TareaException {
        if (prioridad == null) {
            throw new TareaException("La prioridad es obligatoria");
        }
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) throws TareaException {
        validarTitulo(titulo);
        this.titulo = titulo;
    }
    
    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) throws TareaException {
        validarPrioridad(prioridad);
        this.prioridad = prioridad;
    }
    
    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
    
    public boolean isEspecial() { return especial; }
    public void setEspecial(boolean especial) { this.especial = especial; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    
    // Métodos útiles
    public String getEstadoTexto() {
        return estado ? "Hecho" : "Pendiente";
    }
    
    public String getEspecialTexto() {
        return especial ? "★" : "";
    }
    
    public String getFechaTexto() {
        return fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }
    
    @Override
    public String toString() {
        return "Tarea{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", prioridad=" + prioridad.getDescripcion() +
                ", estado=" + getEstadoTexto() +
                ", especial=" + especial +
                ", fecha=" + getFechaTexto() +
                '}';
    }
}