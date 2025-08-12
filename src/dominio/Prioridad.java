/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dominio;

public class Prioridad {
    public static final Prioridad ALTA = new Prioridad(1, "Alta");
    public static final Prioridad MEDIA = new Prioridad(2, "Media");
    public static final Prioridad BAJA = new Prioridad(3, "Baja");
    
    private int valor;
    private String descripcion;
    
    public Prioridad(int valor, String descripcion) {
        this.valor = valor;
        this.descripcion = descripcion;
    }
    
    public Prioridad() {} // Constructor vacío
    
    // Getters y Setters
    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    // Método para obtener todas las prioridades
    public static Prioridad[] values() {
        return new Prioridad[]{ALTA, MEDIA, BAJA};
    }
    
    // Método para obtener prioridad por valor
    public static Prioridad porValor(int valor) {
        switch (valor) {
            case 1: return ALTA;
            case 2: return MEDIA;
            case 3: return BAJA;
            default: return MEDIA; // Por defecto
        }
    }
    
    @Override
    public String toString() {
        return descripcion;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Prioridad prioridad = (Prioridad) obj;
        return valor == prioridad.valor;
    }
}