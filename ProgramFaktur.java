import java.sql.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgramFaktur {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/supermarket";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "nailah2425";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("+-----------------------------------------------------+");
        System.out.println("                        Log in                        ");
        System.out.println("+-----------------------------------------------------+");
        System.out.print("Username : ");
        String username = scanner.nextLine();
        System.out.print("Password : ");
        String password = scanner.nextLine();
        String captcha = "12345";
        System.out.print("Captcha  : ");
        String userCaptcha = scanner.nextLine();

        if (!username.equals("admin") || !password.equals("admin") || !userCaptcha.equals(captcha)) {
            System.out.println("Login gagal, silakan ulangi.");
            scanner.close();
            return;
        }

        System.out.println("Login berhasil!");
        System.out.println("+-----------------------------------------------------+");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Koneksi database berhasil.");

            // Membuat tabel jika belum ada
            buatTabelBarang(connection);
            buatTabelTransaksi(connection);

            while (true) {
                System.out.println("\nPilih operasi CRUD:");
                System.out.println("1. Create");
                System.out.println("2. Read");
                System.out.println("3. Update");
                System.out.println("4. Delete");
                System.out.println("5. Exit");
                System.out.print("Pilihan: ");

                int pilihan = scanner.nextInt();
                scanner.nextLine();

                switch (pilihan) {
                    case 1: // Create (Tambah Transaksi)
                        System.out.println("+-----------------------------------------------------+");
                        System.out.print("No. Faktur : ");
                        String noFaktur = scanner.nextLine();
                        System.out.print("Kode Barang: ");
                        String kodeBarang = scanner.nextLine();
                        System.out.print("Nama Barang: ");
                        String namaBarang = scanner.nextLine();
                        System.out.print("Harga Barang: ");
                        double hargaBarang = scanner.nextDouble();
                        System.out.print("Jumlah Beli : ");
                        int jumlahBeli = scanner.nextInt();
                        scanner.nextLine(); // Clear buffer
                        System.out.print("Kasir       : ");
                        String namaKasir = scanner.nextLine();
                        double totalHarga = hargaBarang * jumlahBeli;
                        String tanggalWaktu = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        tambahTransaksi(connection, noFaktur, kodeBarang, namaBarang, hargaBarang, jumlahBeli, totalHarga, namaKasir, tanggalWaktu);
                        System.out.println("Data berhasil disimpan ke database.");
                        break;
                    case 2: // Read (Lihat Barang)
                        lihatBarang(connection);
                        break;
                    case 3: // Update
                        System.out.println("Fitur Update belum tersedia.");
                        break;
                    case 4: // Delete
                        System.out.println("+-----------------------------------------------------+");
                        System.out.print("Masukkan No. Faktur yang akan dihapus: ");
                        String fakturHapus = scanner.nextLine();
                        hapusTransaksi(connection, fakturHapus);
                        break;
                    case 5:
                        System.out.println("Keluar dari program. Terima kasih!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Pilihan tidak valid. Silakan coba lagi.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Koneksi database gagal: " + e.getMessage());
        }
    }

    private static void buatTabelBarang(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS barang ("
                   + "kode_barang VARCHAR(20) PRIMARY KEY,"
                   + "nama_barang VARCHAR(100) NOT NULL,"
                   + "harga_barang DOUBLE PRECISION NOT NULL)";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            System.out.println("Tabel 'barang' berhasil dibuat atau sudah ada.");
        }
    }

    private static void buatTabelTransaksi(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS transaksi ("
                   + "no_faktur VARCHAR(20) PRIMARY KEY,"
                   + "kode_barang VARCHAR(20),"
                   + "nama_barang VARCHAR(50),"
                   + "harga_barang DOUBLE PRECISION,"
                   + "jumlah_beli INT,"
                   + "total_harga DOUBLE PRECISION,"
                   + "nama_kasir VARCHAR(50),"
                   + "tanggal_waktu TIMESTAMP)";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            System.out.println("Tabel 'transaksi' berhasil dibuat atau sudah ada.");
        }
    }

    private static void lihatBarang(Connection connection) throws SQLException {
        String sql = "SELECT * FROM barang";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            System.out.println("+-------------------------------------------+");
            System.out.println("Kode Barang | Nama Barang | Harga Barang");
            System.out.println("+-------------------------------------------+");
            while (resultSet.next()) {
                System.out.printf("%s | %s | %.2f\n",
                        resultSet.getString("kode_barang"),
                        resultSet.getString("nama_barang"),
                        resultSet.getDouble("harga_barang"));
            }
            System.out.println("+-------------------------------------------+");
        }
    }

    private static void tambahTransaksi(Connection connection, String noFaktur, String kodeBarang, String namaBarang, double hargaBarang, int jumlahBeli, double totalHarga, String namaKasir, String tanggalWaktu) throws SQLException {
        String sql = "INSERT INTO transaksi (no_faktur, kode_barang, nama_barang, harga_barang, jumlah_beli, total_harga, nama_kasir, tanggal_waktu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, noFaktur);
            statement.setString(2, kodeBarang);
            statement.setString(3, namaBarang);
            statement.setDouble(4, hargaBarang);
            statement.setInt(5, jumlahBeli);
            statement.setDouble(6, totalHarga);
            statement.setString(7, namaKasir);
            statement.setTimestamp(8, Timestamp.valueOf(tanggalWaktu));
            statement.executeUpdate();
        }
    }

    private static void hapusTransaksi(Connection connection, String noFaktur) throws SQLException {
        String sql = "DELETE FROM transaksi WHERE no_faktur = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, noFaktur);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Data dengan No. Faktur " + noFaktur + " berhasil dihapus.");
            } else {
                System.out.println("Data dengan No. Faktur " + noFaktur + " tidak ditemukan.");
            }
        }
    }
}
