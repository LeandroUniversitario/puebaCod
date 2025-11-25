

package logica;

public class Recurso {
    private String id;
    private String tipo;
    private double tarifaHora;

    public Recurso(String id, String tipo, double tarifaHora) {
        this.id = id;
        this.tipo = tipo;
        this.tarifaHora = tarifaHora;
    }

    public String getId() { return id; }
    public String getTipo() { return tipo; }
    public double getTarifaHora() { return tarifaHora; }

    // Esto define lo que se muestra en el JComboBox
    @Override
    public String toString() {
        return tipo;
    }
}
