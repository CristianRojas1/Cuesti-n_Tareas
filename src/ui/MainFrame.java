/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ui;


import dominio.Tarea;
import dominio.Prioridad;
import dominio.TareaException;
import servicio.TareaServicio;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MainFrame extends javax.swing.JFrame {
    
    // Servicio principal
    private TareaServicio tareaServicio;
    private TareaTableModel modeloTabla;
    
    // Formato de fecha
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MainFrame() {
        initComponents();
        inicializarComponentes();
        configurarEventos();
        cargarDatosIniciales();
    }
    
    private void inicializarComponentes() {
        try {
            // Inicializar servicio
            tareaServicio = new TareaServicio();
            
            // Configurar ventana principal
            setTitle("Gestor de Tareas - TaskManagerApp");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            
            // Configurar ComboBox de prioridades
            cmbPrioridad.removeAllItems();
            for (Prioridad prioridad : Prioridad.values()) {
                cmbPrioridad.addItem(prioridad.getDescripcion());
            }
            cmbPrioridad.setSelectedIndex(1); // Media por defecto
            
            // Configurar modelo de tabla
            modeloTabla = new TareaTableModel();
            tablaTereas.setModel(modeloTabla);
            
            // Configurar selección de tabla
            tablaTereas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // Configurar campo de fecha con placeholder
            jTextField1.setToolTipText("Formato: dd/MM/yyyy (opcional)");
            
            // Configurar label de estado inicial
            actualizarEstadoLabel();
            
            // Configurar botón deshacer inicial
            btnDeshacer.setEnabled(tareaServicio.hayEliminacionesParaDeshacer());
            
        } catch (Exception e) {
            mostrarError("Error al inicializar la aplicación", e);
        }
    }
    
    private void configurarEventos() {
        // Evento: Agregar tarea
        btnAgregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarTarea();
            }
        });
        
        // Evento: Alternar estado
        btnAlternarEstado.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alternarEstadoTarea();
            }
        });
        
        // Evento: Eliminar tarea
        btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarTarea();
            }
        });
        
        // Evento: Deshacer eliminación
        btnDeshacer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deshacerEliminacion();
            }
        });
        
        // Evento: Refrescar datos
        btnRefrescar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refrescarDatos();
            }
        });
        
        // Evento: Enter en campo título para agregar rápido
        txtTitulo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarTarea();
            }
        });

        // Evento: Cerrar ventana
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                cerrarAplicacion();
            }
        });
    }
    
    private void cargarDatosIniciales() {
        refrescarDatos();
    }
    
    private void agregarTarea() {
        try {
            // Validar título
            String titulo = txtTitulo.getText().trim();
            if (titulo.isEmpty()) {
                mostrarAdvertencia("El título es obligatorio");
                txtTitulo.requestFocus();
                return;
            }
            
            // Obtener prioridad
            Prioridad prioridad = obtenerPrioridadSeleccionada();
            
            // Obtener marca especial
            boolean especial = chkEspecial.isSelected();
            
            // Obtener fecha (opcional)
            LocalDate fecha = parsearFecha(jTextField1.getText().trim());
            
            // Crear tarea
            tareaServicio.crearTarea(titulo, prioridad, especial, fecha);
            
            // Limpiar formulario
            limpiarFormulario();
            
            // Actualizar tabla y estado
            refrescarDatos();
            
            // Mostrar confirmación
            mostrarInformacion("Tarea agregada exitosamente: " + titulo);
            
        } catch (TareaException e) {
            mostrarError("Error al crear tarea", e);
        } catch (Exception e) {
            mostrarError("Error inesperado al crear tarea", e);
        }
    }
    
    private void alternarEstadoTarea() {
        try {
            int filaSeleccionada = tablaTereas.getSelectedRow();
            if (filaSeleccionada == -1) {
                mostrarAdvertencia("Seleccione una tarea para cambiar su estado");
                return;
            }
            
            Tarea tarea = modeloTabla.getTarea(filaSeleccionada);
            if (tarea != null) {
                tareaServicio.alternarEstado(tarea.getId());
                refrescarDatos();
                
                String nuevoEstado = tarea.isEstado() ? "Pendiente" : "Hecho";
                mostrarInformacion("Estado cambiado a: " + nuevoEstado);
            }
            
        } catch (TareaException e) {
            mostrarError("Error al cambiar estado", e);
        } catch (Exception e) {
            mostrarError("Error inesperado al cambiar estado", e);
        }
    }
    
    private void eliminarTarea() {
        try {
            int filaSeleccionada = tablaTereas.getSelectedRow();
            if (filaSeleccionada == -1) {
                mostrarAdvertencia("Seleccione una tarea para eliminar");
                return;
            }
            
            Tarea tarea = modeloTabla.getTarea(filaSeleccionada);
            if (tarea != null) {
                // Confirmar eliminación
                int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de eliminar la tarea: " + tarea.getTitulo() + "?",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (opcion == JOptionPane.YES_OPTION) {
                    tareaServicio.eliminarTarea(tarea.getId());
                    refrescarDatos();
                    
                    // Actualizar botón deshacer
                    btnDeshacer.setEnabled(tareaServicio.hayEliminacionesParaDeshacer());
                    
                    mostrarInformacion("Tarea eliminada: " + tarea.getTitulo());
                }
            }
            
        } catch (TareaException e) {
            mostrarError("Error al eliminar tarea", e);
        } catch (Exception e) {
            mostrarError("Error inesperado al eliminar tarea", e);
        }
    }
    
    private void deshacerEliminacion() {
        try {
            if (!tareaServicio.hayEliminacionesParaDeshacer()) {
                mostrarAdvertencia("No hay eliminaciones que deshacer");
                return;
            }
            
            tareaServicio.deshacerEliminacion();
            refrescarDatos();
            
            // Actualizar botón deshacer
            btnDeshacer.setEnabled(tareaServicio.hayEliminacionesParaDeshacer());
            
            mostrarInformacion("Eliminación deshecha exitosamente");
            
        } catch (TareaException e) {
            mostrarError("Error al deshacer eliminación", e);
        } catch (Exception e) {
            mostrarError("Error inesperado al deshacer eliminación", e);
        }
    }
    
    private void refrescarDatos() {
        try {
            tareaServicio.refrescarDatos();
            java.util.List<Tarea> tareas = tareaServicio.obtenerTareasActivas();
            modeloTabla.actualizarDatos(tareas);
            
            actualizarEstadoLabel();
            btnDeshacer.setEnabled(tareaServicio.hayEliminacionesParaDeshacer());
            
        } catch (Exception e) {
            mostrarError("Error al refrescar datos", e);
        }
    }
    
    private void limpiarFormulario() {
        txtTitulo.setText("");
        cmbPrioridad.setSelectedIndex(1); // Media
        chkEspecial.setSelected(false);
        jTextField1.setText("");
        txtTitulo.requestFocus();
    }
    
    private Prioridad obtenerPrioridadSeleccionada() {
        int indice = cmbPrioridad.getSelectedIndex();
        switch (indice) {
            case 0: return Prioridad.ALTA;
            case 1: return Prioridad.MEDIA;
            case 2: return Prioridad.BAJA;
            default: return Prioridad.MEDIA;
        }
    }
    
    private LocalDate parsearFecha(String textoFecha) throws TareaException {
        if (textoFecha.isEmpty()) {
            return null; // Fecha opcional
        }
        
        try {
            return LocalDate.parse(textoFecha, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new TareaException("Formato de fecha inválido. Use: dd/MM/yyyy");
        }
    }
    
    private void actualizarEstadoLabel() {
        int pendientes = tareaServicio.contarTareasPendientes();
        int completadas = tareaServicio.contarTareasCompletadas();
        lblEstado.setText("P:" + pendientes + " | C:" + completadas);
    }
    
    private void mostrarError(String mensaje, Exception e) {
        String textoCompleto = mensaje + "\n" + e.getMessage();
        JOptionPane.showMessageDialog(this, textoCompleto, "Error", JOptionPane.ERROR_MESSAGE);
        System.err.println("ERROR: " + mensaje + " - " + e.getMessage());
        e.printStackTrace();
    }
    
    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
    
    private void mostrarInformacion(String mensaje) {
        lblEstado.setText(mensaje);
        // También mostrar en consola para registro
        System.out.println("INFO: " + mensaje);
        
        // Timer para limpiar el mensaje después de 3 segundos
        Timer timer = new Timer(3000, e -> actualizarEstadoLabel());
        timer.setRepeats(false);
        timer.start();
    }
    
    private void cerrarAplicacion() {
        try {
            tareaServicio.cerrar();
            System.out.println("Aplicación cerrada correctamente");
        } catch (Exception e) {
            System.err.println("Error al cerrar aplicación: " + e.getMessage());
        }
        System.exit(0);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelCentral = new javax.swing.JPanel();
        panelPrincipal = new javax.swing.JPanel();
        panelFormulario = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        txtTitulo = new javax.swing.JTextField();
        lblPrioridad = new javax.swing.JLabel();
        cmbPrioridad = new javax.swing.JComboBox<>();
        lblFecha = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        chkEspecial = new javax.swing.JCheckBox();
        btnAgregar = new javax.swing.JButton();
        panelControles = new javax.swing.JPanel();
        btnAlternarEstado = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnDeshacer = new javax.swing.JButton();
        btnRefrescar = new javax.swing.JButton();
        lblEstado = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaTereas = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout panelCentralLayout = new javax.swing.GroupLayout(panelCentral);
        panelCentral.setLayout(panelCentralLayout);
        panelCentralLayout.setHorizontalGroup(
            panelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 95, Short.MAX_VALUE)
        );
        panelCentralLayout.setVerticalGroup(
            panelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 78, Short.MAX_VALUE)
        );

        lblTitulo.setText("Titulo");

        lblPrioridad.setText("Prioridad");

        cmbPrioridad.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblFecha.setText("Fecha");

        chkEspecial.setText("Especial");

        btnAgregar.setText("Agregar");

        javax.swing.GroupLayout panelFormularioLayout = new javax.swing.GroupLayout(panelFormulario);
        panelFormulario.setLayout(panelFormularioLayout);
        panelFormularioLayout.setHorizontalGroup(
            panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormularioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblPrioridad, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbPrioridad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkEspecial)
                    .addComponent(btnAgregar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelFormularioLayout.setVerticalGroup(
            panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormularioLayout.createSequentialGroup()
                .addGroup(panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPrioridad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbPrioridad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkEspecial)
                .addGap(0, 18, Short.MAX_VALUE))
        );

        btnAlternarEstado.setText("AlternarEstado");

        btnEliminar.setText("Eliminar");

        btnDeshacer.setText("Deshacer");

        btnRefrescar.setText("Refrescar");

        lblEstado.setText("Estado");

        javax.swing.GroupLayout panelControlesLayout = new javax.swing.GroupLayout(panelControles);
        panelControles.setLayout(panelControlesLayout);
        panelControlesLayout.setHorizontalGroup(
            panelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelControlesLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(btnAlternarEstado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeshacer, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefrescar, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );
        panelControlesLayout.setVerticalGroup(
            panelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelControlesLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(panelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAlternarEstado)
                    .addComponent(btnEliminar)
                    .addComponent(btnDeshacer)
                    .addComponent(btnRefrescar)
                    .addComponent(lblEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        tablaTereas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tablaTereas);

        javax.swing.GroupLayout panelPrincipalLayout = new javax.swing.GroupLayout(panelPrincipal);
        panelPrincipal.setLayout(panelPrincipalLayout);
        panelPrincipalLayout.setHorizontalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPrincipalLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelPrincipalLayout.createSequentialGroup()
                                .addComponent(panelControles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(panelFormulario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        panelPrincipalLayout.setVerticalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addComponent(panelFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelControles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelCentral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(panelCentral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnAlternarEstado;
    private javax.swing.JButton btnDeshacer;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnRefrescar;
    private javax.swing.JCheckBox chkEspecial;
    private javax.swing.JComboBox<String> cmbPrioridad;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblPrioridad;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelCentral;
    private javax.swing.JPanel panelControles;
    private javax.swing.JPanel panelFormulario;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JTable tablaTereas;
    private javax.swing.JTextField txtTitulo;
    // End of variables declaration//GEN-END:variables
}
