/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import dominio.Tarea;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TareaTableModel extends AbstractTableModel {
    private List<Tarea> tareas;
    private String[] columnas = {"ID", "Título", "Prioridad", "Estado", "Especial", "Fecha"};
    
    public TareaTableModel() {
        this.tareas = new ArrayList<>();
    }
    
    public TareaTableModel(List<Tarea> tareas) {
        this.tareas = tareas != null ? tareas : new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return tareas.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnas.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnas[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= tareas.size()) {
            return null;
        }
        
        Tarea tarea = tareas.get(rowIndex);
        
        switch (columnIndex) {
            case 0: return tarea.getId();
            case 1: return tarea.getTitulo();
            case 2: return tarea.getPrioridad().getDescripcion();
            case 3: return tarea.getEstadoTexto();
            case 4: return tarea.getEspecialTexto();
            case 5: return tarea.getFechaTexto();
            default: return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Integer.class;
            case 1: return String.class;
            case 2: return String.class;
            case 3: return String.class;
            case 4: return String.class;
            case 5: return String.class;
            default: return Object.class;
        }
    }
    
    // Actualizar datos de la tabla
    public void actualizarDatos(List<Tarea> nuevasTareas) {
        this.tareas = nuevasTareas != null ? nuevasTareas : new ArrayList<>();
        fireTableDataChanged();
    }
    
    // Agregar tarea
    public void agregarTarea(Tarea tarea) {
        tareas.add(tarea);
        int fila = tareas.size() - 1;
        fireTableRowsInserted(fila, fila);
    }
    
    // Remover tarea
    public void removerTarea(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < tareas.size()) {
            tareas.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }
    
    // Obtener tarea por fila
    public Tarea getTarea(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < tareas.size()) {
            return tareas.get(rowIndex);
        }
        return null;
    }
    
    // Obtener todas las tareas
    public List<Tarea> getTareas() {
        return new ArrayList<>(tareas);
    }
    
    // Limpiar tabla
    public void limpiar() {
        tareas.clear();
        fireTableDataChanged();
    }
}