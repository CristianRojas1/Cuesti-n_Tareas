/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dominio.Tarea;
import dominio.Prioridad;
import dominio.TareaException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TareaDAO {

    // IMPORTANTE: Cambia estos valores por los de tu configuración
    private static final String SERVIDOR = "localhost";
    private static final String PUERTO = "1433";
    private static final String BASE_DATOS = "TaskManagerDB";
    private static final String USUARIO = "CristianRA";
    private static final String PASSWORD = "123456"; // CAMBIA ESTA CONTRASEÑA
    
    // URL de conexión
    private static final String URL = "jdbc:sqlserver://" + SERVIDOR + ":" + PUERTO + 
        ";databaseName=" + BASE_DATOS + ";trustServerCertificate=true;encrypt=false";
    
    // Singleton para conexión
    private static Connection conexion;
    
    public TareaDAO() {
        System.out.println("Inicializando TareaDAO...");
        System.out.println("URL de conexión: " + URL);
        System.out.println("Usuario: " + USUARIO);
        verificarDriver();
        inicializarBaseDatos();
    }
    
    // Verificar que el driver JDBC esté disponible
    private void verificarDriver() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("Driver JDBC SQL Server cargado correctamente");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: Driver JDBC SQL Server no encontrado");
            System.err.println("Asegúrate de tener el archivo mssql-jdbc-X.X.X.jre8.jar en tu classpath");
            throw new RuntimeException("Driver JDBC no disponible", e);
        }
    }
    
    // Obtener conexión
    private Connection getConnection() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                System.out.println("Estableciendo conexión a la base de datos...");
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                System.out.println("Conexión establecida exitosamente");
            } catch (SQLException e) {
                System.err.println("Error al conectar con la base de datos:");
                System.err.println("URL: " + URL);
                System.err.println("Usuario: " + USUARIO);
                System.err.println("Error: " + e.getMessage());
                
                // Sugerencias de solución
                System.err.println("\n=== POSIBLES SOLUCIONES ===");
                System.err.println("1. Verifica que SQL Server esté ejecutándose");
                System.err.println("2. Cambia la contraseña en TareaDAO.java línea 21");
                System.err.println("3. Verifica el usuario y permisos de SQL Server");
                System.err.println("4. Comprueba que el puerto 1433 esté abierto");
                System.err.println("5. Ejecuta este comando SQL como administrador:");
                System.err.println("   ALTER LOGIN sa ENABLE;");
                System.err.println("   ALTER LOGIN sa WITH PASSWORD = 'tu_password';");
                
                throw e;
            }
        }
        return conexion;
    }
    
    // Inicializar base de datos y tabla
    private void inicializarBaseDatos() {
        crearBaseDatos();
        crearTablaTaskeas();
    }
    
    private void crearBaseDatos() {
        String urlMaster = "jdbc:sqlserver://" + SERVIDOR + ":" + PUERTO + 
            ";databaseName=master;trustServerCertificate=true;encrypt=false";
        
        String sqlCrearBD = "IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = '" + 
            BASE_DATOS + "') CREATE DATABASE " + BASE_DATOS;
        
        Connection connMaster = null;
        PreparedStatement stmt = null;
        
        try {
            connMaster = DriverManager.getConnection(urlMaster, USUARIO, PASSWORD);
            stmt = connMaster.prepareStatement(sqlCrearBD);
            stmt.execute();
            System.out.println("Base de datos verificada/creada: " + BASE_DATOS);
        } catch (SQLException e) {
            System.err.println("Error al crear base de datos: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connMaster != null) connMaster.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
    
    private void crearTablaTaskeas() {
        String sqlCrearTabla = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tareas') " +
            "BEGIN " +
            "CREATE TABLE tareas ( " +
            "id INT IDENTITY(1,1) PRIMARY KEY, " +
            "titulo NVARCHAR(255) NOT NULL, " +
            "prioridad INT NOT NULL CHECK (prioridad BETWEEN 1 AND 3), " +
            "estado BIT NOT NULL DEFAULT 0, " +
            "especial BIT NOT NULL DEFAULT 0, " +
            "fecha DATE NULL, " +
            "fecha_creacion DATE NOT NULL DEFAULT GETDATE(), " +
            "activa BIT NOT NULL DEFAULT 1 " +
            "); " +
            "INSERT INTO tareas (titulo, prioridad, estado, especial, fecha) VALUES " +
            "('Completar práctica Java', 1, 0, 1, '2025-08-15'), " +
            "('Estudiar para examen', 1, 0, 0, '2025-08-13'), " +
            "('Hacer ejercicios', 2, 1, 0, NULL), " +
            "('Revisar documentación', 2, 0, 0, '2025-08-14'); " +
            "END";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sqlCrearTabla);
            stmt.execute();
            System.out.println("Tabla tareas verificada/creada correctamente");
        } catch (SQLException e) {
            System.err.println("Error al crear tabla tareas: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                // No cerrar conn aquí porque es singleton
            } catch (SQLException e) {
                System.err.println("Error al cerrar statement: " + e.getMessage());
            }
        }
    }
    
    // Crear nueva tarea
    public void crear(Tarea tarea) throws TareaException {
        String sql = "INSERT INTO tareas (titulo, prioridad, estado, especial, fecha) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet keys = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
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
                keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    tarea.setId(keys.getInt(1));
                }
                System.out.println("Tarea creada en BD con ID: " + tarea.getId());
            }
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("crear tarea", e);
        } finally {
            try {
                if (keys != null) keys.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
    
    // Listar tareas activas
    public List<Tarea> listarActivas() throws TareaException {
        String sql = "SELECT * FROM tareas WHERE activa = 1 ORDER BY id";
        List<Tarea> tareas = new ArrayList<Tarea>();
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
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
            
            System.out.println("Cargadas " + tareas.size() + " tareas desde BD");
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("listar tareas", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
        
        return tareas;
    }
    
    // Actualizar estado
    public void actualizarEstado(int id, boolean estado) throws TareaException {
        String sql = "UPDATE tareas SET estado = ? WHERE id = ? AND activa = 1";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setBoolean(1, estado);
            stmt.setInt(2, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw TareaException.tareaNoEncontrada(id);
            }
            
            System.out.println("Estado actualizado para tarea ID: " + id);
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("actualizar estado", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar statement: " + e.getMessage());
            }
        }
    }
    
    // Eliminar (soft delete)
    public void eliminar(int id) throws TareaException {
        String sql = "UPDATE tareas SET activa = 0 WHERE id = ? AND activa = 1";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw TareaException.tareaNoEncontrada(id);
            }
            
            System.out.println("Tarea eliminada (soft delete) ID: " + id);
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("eliminar tarea", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar statement: " + e.getMessage());
            }
        }
    }
    
    // Restaurar tarea eliminada
    public void restaurar(int id) throws TareaException {
        String sql = "UPDATE tareas SET activa = 1 WHERE id = ? AND activa = 0";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new TareaException("No se puede restaurar la tarea con ID: " + id);
            }
            
            System.out.println("Tarea restaurada ID: " + id);
            
        } catch (SQLException e) {
            throw TareaException.errorBaseDatos("restaurar tarea", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar statement: " + e.getMessage());
            }
        }
    }
    
    // Buscar por ID
    public Tarea buscarPorId(int id) throws TareaException {
        String sql = "SELECT * FROM tareas WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
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
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
    
    // Método para probar la conexión
    public boolean probarConexion() {
        Connection conn = null;
        try {
            conn = getConnection();
            System.out.println("✓ Conexión a base de datos exitosa");
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Error de conexión: " + e.getMessage());
            return false;
        }
        // No cerrar conn aquí porque es singleton
    }
    
    // Cerrar conexión
    public void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}