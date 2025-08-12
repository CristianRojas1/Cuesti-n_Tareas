/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dominio;

public class TareaException extends Exception {
    
    public TareaException(String mensaje) {
        super(mensaje);
    }
    
    public TareaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    
    // Métodos específicos para diferentes tipos de errores
    public static TareaException tituloVacio() {
        return new TareaException("El título de la tarea no puede estar vacío");
    }
    
    public static TareaException prioridadInvalida() {
        return new TareaException("La prioridad debe estar entre 1 y 3");
    }
    
    public static TareaException tareaNoEncontrada(int id) {
        return new TareaException("No se encontró la tarea con ID: " + id);
    }
    
    public static TareaException errorBaseDatos(String operacion, Throwable causa) {
        return new TareaException("Error en base de datos durante: " + operacion, causa);
    }
}