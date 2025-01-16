import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// BancoSincronizado.java

// Clase principal que ejecuta el programa
public class BancoSincronizado {

    public static void main(String[] args) {
        // Se crea una instancia de la clase Banco
        Banco b = new Banco();

        // Se crean 100 hilos que simulan transferencias entre cuentas
        for (int i = 0; i < 100; i++) {
            // Cada hilo se asocia con una tarea de transferencia
            EjecucionTransferencias r = new EjecucionTransferencias(b, i, 2000);

            // Se crea un hilo para la tarea y se inicia
            Thread t = new Thread(r);
            t.start();
        }
    }
}

// Clase que representa el banco y maneja las transferencias de dinero
class Banco {

    // Constructor que inicializa las cuentas con un saldo inicial de 2000
    public Banco() {
        cuentas = new double[100]; // 100 cuentas en total
        for (int i = 0; i < cuentas.length; i++) {
            cuentas[i] = 2000;
        }
    }

    // Método para realizar transferencias entre cuentas
    public void transferencia(int cuentaOrigen, int cuentaDestino, double cantidad) {
        cierreBanco.lock(); // Bloquea el recurso para evitar acceso concurrente

        try {
            // Verifica que la cuenta origen tenga suficiente saldo
            if (cuentas[cuentaOrigen] < cantidad) {
                return;
            }

            // Muestra información del hilo actual que realiza la transferencia
            System.out.println(Thread.currentThread());

            // Realiza la transferencia: resta de la cuenta origen y suma a la cuenta destino
            cuentas[cuentaOrigen] -= cantidad;
            System.out.printf("%10.2f de %d para %d", cantidad, cuentaOrigen, cuentaDestino);
            cuentas[cuentaDestino] += cantidad;

            // Imprime el saldo total después de la transferencia
            System.out.printf("Saldo total: %10.2f%n", getSaldoTotal());
        } finally {
            cierreBanco.unlock(); // Libera el recurso bloqueado
        }
    }

    // Método para calcular el saldo total de todas las cuentas
    public double getSaldoTotal() {
        cierreBanco.lock(); // Bloquea el recurso durante el cálculo
        try {
            double suma_cuentas = 0;
            for (double a : cuentas) {
                suma_cuentas += a;
            }
            return suma_cuentas;
        } finally {
            cierreBanco.unlock(); // Libera el recurso bloqueado
        }
    }

    private final double[] cuentas; // Arreglo que almacena los saldos de las cuentas

    private Lock cierreBanco = new ReentrantLock(); // Lock para sincronizar el acceso
}

// Clase que implementa Runnable para definir tareas de transferencia
class EjecucionTransferencias implements Runnable {

    public EjecucionTransferencias(Banco b, int de, double max) {
        banco = b; // Instancia del banco compartido
        deLaCuenta = de; // Cuenta origen para las transferencias
        cantidadMax = max; // Cantidad máxima para transferencias
    }

    @Override
    public void run() {

        try {
            while (true) {
                // Genera una cuenta destino aleatoria
                int paraLaCuenta = (int) (100 * Math.random());

                // Genera una cantidad aleatoria para transferir
                double cantidad = cantidadMax * Math.random();

                // Realiza la transferencia llamando al método del banco
                banco.transferencia(deLaCuenta, paraLaCuenta, cantidad);

                // Pausa el hilo durante 1 segundo
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace(); // Manejo de excepción si el hilo es interrumpido
        }
    }

    private Banco banco; // Instancia del banco compartido
    private int deLaCuenta; // Cuenta origen
    private double cantidadMax; // Cantidad máxima para transferencias
}