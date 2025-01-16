import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
        saldoSuficiente = cierreBanco.newCondition(); // Inicializa la condición
        for (int i = 0; i < cuentas.length; i++) {
            cuentas[i] = 2000;
        }
    }

    // Método para realizar transferencias entre cuentas
    public void transferencia(int cuentaOrigen, int cuentaDestino, double cantidad) throws InterruptedException {
        cierreBanco.lock(); // Bloquea el recurso para evitar acceso concurrente
        try {
            // Espera mientras la cuenta origen no tiene suficiente saldo
            while (cuentas[cuentaOrigen] < cantidad) {
                saldoSuficiente.await(); // Libera el lock y espera hasta que haya saldo suficiente
            }

            // Imprime información del hilo actual
            System.out.println(Thread.currentThread());

            // Realiza la transferencia
            cuentas[cuentaOrigen] -= cantidad; // Descuenta de la cuenta origen
            System.out.printf("%10.2f de %d para %d", cantidad, cuentaOrigen, cuentaDestino);
            cuentas[cuentaDestino] += cantidad; // Añade a la cuenta destino

            // Muestra el saldo total después de la transferencia
            System.out.printf("Saldo total: %10.2f%n", getSaldoTotal());

            // Notifica a otros hilos que el saldo ha cambiado
            saldoSuficiente.signalAll();
        } finally {
            cierreBanco.unlock(); // Libera el lock
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
            cierreBanco.unlock(); // Libera el recurso
        }
    }

    private final double[] cuentas; // Arreglo para los saldos de las cuentas
    private Lock cierreBanco = new ReentrantLock(); // Lock para sincronización
    private Condition saldoSuficiente; // Condición para esperar por saldo suficiente
}

// Clase que implementa Runnable para definir tareas de transferencia
class EjecucionTransferencias implements Runnable {

    public EjecucionTransferencias(Banco b, int de, double max) {
        banco = b; // Instancia del banco compartido
        deLaCuenta = de; // Cuenta origen
        cantidadMax = max; // Monto máximo a transferir
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Selecciona una cuenta destino aleatoria
                int paraLaCuenta = (int) (100 * Math.random());

                // Genera un monto aleatorio para transferir
                double cantidad = cantidadMax * Math.random();

                // Realiza la transferencia
                banco.transferencia(deLaCuenta, paraLaCuenta, cantidad);

                // Pausa el hilo durante 1 segundo
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace(); // Manejo de interrupciones
        }
    }

    private Banco banco; // Referencia al banco compartido
    private int deLaCuenta; // Cuenta origen
    private double cantidadMax; // Cantidad máxima de transferencia
}