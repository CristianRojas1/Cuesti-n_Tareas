/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

// =================================================================
// PAQUETE: dao
// =================================================================

// TareaDAO.java
package com.tuapellido.taskmanager.dao;

import com.tuapellido.taskmanager.dominio.Tarea;
import com.tuapellido.taskmanager.dominio.Prioridad;
import com.tuapellido.taskmanager.dominio.TareaException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TareaDAO {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;trustServerCertificate=true";
    private static final String USUARIO = "sa";
    private static final String PASSWORD = "tu_password";
    
    // Singleton para conexión
    private static Connection conexion;
    
    public TareaDAO() {
        inicializarBaseDatos();
    }
    
    // Obtener conexión
    private Connection getConnection() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
        }
        return conexion;
    }
    
    // Crear tabla si no existe
    private void inicializarBaseDatos() {
        String sqlCrearTabla = """
            IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tareas' AND xtype='U')
            CREATE TABLE tareas (
                id INT IDENTITY(1,1) PRIMARY KEY,
                titulo NVARCHAR(255) NOT NULL,
                prioridad INT NOT NULL CHECK (prioridad BETWEEN 1 AND 3),
                estado BIT NOT NULL DEFAULT 0,
                especial BIT NOT NULL DEFAULT 0,
                fecha DATE NULL,
                fecha_creacion DATE NOT NULL DEFAULT GETDATE(),
                activa BIT NOT NULL DEFAULT 1
            )
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlCrearTabla)) {
            stmt.execute();
            System.out.println("Base de datos inicializada correctamente");
        } catch (SQLException e) {
            System.err.println("Error al inicializar base de datos: " + e.getMessage());
        }
    }
    
    // Crear nueva tarea
    public void crear(Tarea tarea) throws TareaException {
        String sql = "INSERT INTO tareas (titulo, prioridad, estado, especial, fecha) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, tarea.getTitulo());
            stmt.setInt(2, tarea.getPrioridad().getValor());
            stmt.setBoolean(3, tarea.isEstado());
            stmt.setBoolean(4, tarea.isEspecial());
            
            if (tarea.getFecha() != null) {
                stmt.setDate(5, Date.valueOf(tarea.getFecha()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    tarea.setId(keys.getInt(1));
                }
            }
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("crear tarea", e);
        }
    }
    
    // Listar tareas activas
    public List<Tarea> listarActivas() throws TareaException {
        String sql = "SELECT * FROM tareas WHERE activa = 1 ORDER BY id";
        List<Tarea> tareas = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Tarea tarea = new Tarea();
                tarea.setId(rs.getInt("id"));
                tarea.setTitulo(rs.getString("titulo"));
                tarea.setPrioridad(Prioridad.porValor(rs.getInt("prioridad")));
                tarea.setEstado(rs.getBoolean("estado"));
                tarea.setEspecial(rs.getBoolean("especial"));
                
                Date fecha = rs.getDate("fecha");
                if (fecha != null) {
                    tarea.setFecha(fecha.toLocalDate());
                }
                
                Date fechaCreacion = rs.getDate("fecha_creacion");
                if (fechaCreacion != null) {
                    tarea.setFechaCreacion(fechaCreacion.toLocalDate());
                }
                
                tarea.setActiva(rs.getBoolean("activa"));
                tareas.add(tarea);
            }
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("listar tareas", e);
        }
        
        return tareas;
    }
    
    // Actualizar estado
    public void actualizarEstado(int id, boolean estado) throws TareaException {
        String sql = "UPDATE tareas SET estado = ? WHERE id = ? AND activa = 1";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, estado);
            stmt.setInt(2, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw TareaException.tareaNoEncontrada(id);
            }
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("actualizar estado", e);
        }
    }
    
    // Eliminar (soft delete)
    public void eliminar(int id) throws TareaException {
        String sql = "UPDATE tareas SET activa = 0 WHERE id = ? AND activa = 1";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw TareaException.tareaNoEncontrada(id);
            }
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("eliminar tarea", e);
        }
    }
    
    // Restaurar tarea eliminada
    public void restaurar(int id) throws TareaException {
        String sql = "UPDATE tareas SET activa = 1 WHERE id = ? AND activa = 0";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new TareaException("No se puede restaurar la tarea con ID: " + id);
            }
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("restaurar tarea", e);
        }
    }
    
    // Buscar por ID
    public Tarea buscarPorId(int id) throws TareaException {
        String sql = "SELECT * FROM tareas WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Tarea tarea = new Tarea();
                tarea.setId(rs.getInt("id"));
                tarea.setTitulo(rs.getString("titulo"));
                tarea.setPrioridad(Prioridad.porValor(rs.getInt("prioridad")));
                tarea.setEstado(rs.getBoolean("estado"));
                tarea.setEspecial(rs.getBoolean("especial"));
                
                Date fecha = rs.getDate("fecha");
                if (fecha != null) {
                    tarea.setFecha(fecha.toLocalDate());
                }
                
                Date fechaCreacion = rs.getDate("fecha_creacion");
                if (fechaCreacion != null) {
                    tarea.setFechaCreacion(fechaCreacion.toLocalDate());
                }
                
                tarea.setActiva(rs.getBoolean("activa"));
                return tarea;
            }
            
            return null;
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("buscar tarea", e);
        }
    }
    
    // Cerrar conexión
    public void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}