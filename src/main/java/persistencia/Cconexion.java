

package persistencia;


import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;


public class Cconexion {
    


    Connection conexion = null;

    String contraseña = "rutt";
    String usuario = "usersql";
    String ip = "localhost";
    String bd = "AlquilerVehiculos";
    String puerto = "1433";

    String cadena = "jdbc:sqlserver://" + ip + ":" + puerto + "/" + bd+ ";encrypt=false;trustServerCertificate=true";

    public Connection conectar() {
        try {
           String cadena = "jdbc:sqlserver://localhost:1433;databaseName=AlquilerVehiculos;encrypt=false;trustServerCertificate=true;";
            conexion = DriverManager.getConnection(cadena,usuario,contraseña);
            JOptionPane.showMessageDialog(null,"se conecto a la base de datos");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "no se logro conectar a la base de datos, errores: " + e.toString());
        }
        return  conexion;
    }
}


