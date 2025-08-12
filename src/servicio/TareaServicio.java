/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicio;

import dao.TareaDAO;
import dominio.Tarea;
import dominio.Prioridad;
import dominio.TareaException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TareaServicio {
    private TareaDAO tareaDAO;
    private ArrayList<Tarea> tareasEnMemoria;     // Estructura dinámica 1
    private Stack<Integer> tareasEliminadas;      // Estructura dinámica 2
    private static final int LIMITE_DESHACER = 10;
    
    public TareaServicio() {
        this.tareaDAO = new TareaDAO();
        this.tareasEnMemoria = new ArrayList<>();
        this.tareasEliminadas = new Stack<>();
        cargarTareasDesdeDB();
    }
    
    // Cargar tareas desde BD a memoria
    private void cargarTareasDesdeDB() {
        try {
            tareasEnMemoria.clear();
            List<Tarea> tareasDB = tareaDAO.listarActivas();
            tareasEnMemoria.addAll(tareasDB);
            System.out.println("Cargadas " + tareasEnMemoria.size() + " tareas desde BD");
        } catch (TareaException e) {
            System.err.println("Error al cargar tareas: " + e.getMessage());
        }
    }
    
    // Crear nueva tarea
    public void crearTarea(String titulo, Prioridad prioridad, boolean especial, LocalDate fecha) 
            throws TareaException {
        try {
            Tarea nuevaTarea = new Tarea(titulo, prioridad, especial, fecha);
            tareaDAO.crear(nuevaTarea);
            tareasEnMemoria.add(nuevaTarea);
            System.out.println("Tarea creada: " + nuevaTarea.getTitulo());
        } catch (TareaException e) {
            throw e;
        }
    }
    
    // Alternar estado
    public void alternarEstado(int id) throws TareaException {
        Tarea tarea = buscarTareaEnMemoria(id);
        if (tarea != null) {
            boolean nuevoEstado = !tarea.isEstado();
            tareaDAO.actualizarEstado(id, nuevoEstado);
            tarea.setEstado(nuevoEstado);
            System.out.println("Estado cambiado a: " + tarea.getEstadoTexto());
        } else {
            throw TareaException.tareaNoEncontrada(id);
        }
    }
    
    // Eliminar tarea (soft delete)
    public void eliminarTarea(int id) throws TareaException {
        Tarea tarea = buscarTareaEnMemoria(id);
        if (tarea != null) {
            tareaDAO.eliminar(id);
            tareasEnMemoria.removeIf(t -> t.getId() == id);
            
            // Agregar a pila para deshacer (limitar tamaño)
            tareasEliminadas.push(id);
            if (tareasEliminadas.size() > LIMITE_DESHACER) {
                tareasEliminadas.remove(0); // Remover el más antiguo
            }
            
            System.out.println("Tarea eliminada: " + tarea.getTitulo());
        } else {
            throw TareaException.tareaNoEncontrada(id);
        }
    }
    
    // Deshacer última eliminación
    public void deshacerEliminacion() throws TareaException {
        if (tareasEliminadas.isEmpty()) {
            throw new TareaException("No hay eliminaciones que deshacer");
        }
        
        int idTarea = tareasEliminadas.pop();
        tareaDAO.restaurar(idTarea);
        
        // Buscar tarea restaurada y agregarla a memoria
        Tarea tareaRestaurada = tareaDAO.buscarPorId(idTarea);
        if (tareaRestaurada != null) {
            tareasEnMemoria.add(tareaRestaurada);
            System.out.println("Tarea restaurada: " + tareaRestaurada.getTitulo());
        }
    }
    
    // Obtener todas las tareas activas
    public List<Tarea> obtenerTareasActivas() {
        return new ArrayList<>(tareasEnMemoria);
    }
    
    // Buscar tarea en memoria
    private Tarea buscarTareaEnMemoria(int id) {
        return tareasEnMemoria.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }
    
    // Verificar si hay eliminaciones para deshacer
    public boolean hayEliminacionesParaDeshacer() {
        return !tareasEliminadas.isEmpty();
    }
    
    // Obtener cantidad de tareas por estado
    public int contarTareasPendientes() {
        return (int) tareasEnMemoria.stream()
                .filter(t -> !t.isEstado())
                .count();
    }
    
    public int contarTareasCompletadas() {
        return (int) tareasEnMemoria.stream()
                .filter(Tarea::isEstado)
                .count();
    }
    
    // Refrescar datos desde BD
    public void refrescarDatos() {
        cargarTareasDesdeDB();
    }
    
    // Cerrar recursos
    public void cerrar() {
        tareaDAO.cerrarConexion();
    }
}