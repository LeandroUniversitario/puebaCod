
package logica;

public class Recurso {

    private String id;
    private String tipo;
    private String descripcion;
    private double tarifaHora;
    private String estado;
    private String ubicacion;

    public Recurso(String id, String tipo, String descripcion,
            double tarifaHora, String estado, String ubicacion) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.tarifaHora = tarifaHora;
        this.estado = estado;
        this.ubicacion = ubicacion;
    }

    public String getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getTarifaHora() {
        return tarifaHora;
    }

    public String getEstado() {
        return estado;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setTarifaHora(double tarifaHora) {
        this.tarifaHora = tarifaHora;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
    
    
    /* 🔑 MUY IMPORTANTE
       Esto es lo que se muestra en el JComboBox */
    @Override
    public String toString() {
        return tipo + " - " + id;
    }
}
