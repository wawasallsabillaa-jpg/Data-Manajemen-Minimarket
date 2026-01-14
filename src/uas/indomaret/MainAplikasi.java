package uas.indomaret;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MainAplikasi extends Application {

    private TextField txtNama = new TextField();
    private TextField txtHarga = new TextField();
    private TextField txtStok = new TextField();
    private TextField txtSearch = new TextField(); 
    private ComboBox<String> cbKategori = new ComboBox<>();
    private Map<String, Integer> categoryMap = new HashMap<>();

    private TableView<Produk> table = new TableView<>();
    private ObservableList<Produk> dataProduk = FXCollections.observableArrayList();
    
    private Label lblSummary = new Label("Total Jenis: 0 | Total Stok: 0");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistem Informasi Gudang Indomaret - UAS");

        // --- 1. HEADER (LOGO & JUDUL) ---
        ImageView logoView = new ImageView();
        try {
        	Image img = new Image(getClass().getResourceAsStream("/uas/indomaret/Logo.jpg"));
      
            logoView.setImage(img);
            logoView.setFitWidth(80);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            
        }

        Label lblJudul = new Label("MANAJEMEN STOK MINIMARKET");
        lblJudul.setStyle("-fx-font-size: 18px;" + "-fx-font-weight: bold;" + "-fx-text-fill: white;");
        
        HBox header = new HBox(20, logoView, lblJudul);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));

        header.setStyle(
            "-fx-background-color: #d35400;" 
        );

        // --- 2. FITUR SEARCH ---
        txtSearch.setPromptText("Cari nama produk di sini...");
        txtSearch.setPrefWidth(300);
        HBox searchBox = new HBox(10, new Label("Cari:"), txtSearch);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // --- 3. FORM INPUT ---
        GridPane form = new GridPane();
        form.setHgap(15); form.setVgap(8);
        form.setPadding(new Insets(15));
        form.setStyle("-fx-background-color: white;" + "-fx-border-color: #f39c12;" + "-fx-border-radius: 8;" + "-fx-background-radius: 8;");
        
        form.add(new Label("Nama Produk:"), 0, 0); form.add(txtNama, 1, 0);
        form.add(new Label("Harga (Rp):"), 0, 1);  form.add(txtHarga, 1, 1);
        form.add(new Label("Stok Barang:"), 0, 2); form.add(txtStok, 1, 2);
        form.add(new Label("Kategori:"), 0, 3);    form.add(cbKategori, 1, 3);
        cbKategori.setPromptText("-- Pilih Kategori --");
        cbKategori.setMinWidth(180);

        // --- 4. TOMBOL (DENGAN WARNA TEMA) ---
        Button btnAdd = new Button("TAMBAH");
        btnAdd.setStyle("-fx-background-color: #f39c12;" + "-fx-text-fill: white;" + "-fx-font-weight: bold;");
        	
        Button btnEdit = new Button("UPDATE");
        btnEdit.setStyle("-fx-background-color: #ffffff;" + "-fx-text-fill: #f39c12;" + "-fx-border-color: #f39c12;" + "-fx-font-weight: bold;");
        
        Button btnDel = new Button("HAPUS");
        btnDel.setStyle( "-fx-background-color: #d35400;" + "-fx-text-fill: white;" + "-fx-font-weight: bold;");
        
        Button btnClear = new Button("RESET");

        HBox btnBox = new HBox(15, btnAdd, btnEdit, btnDel, btnClear);
        btnBox.setPadding(new Insets(10, 0, 10, 0));

        // --- 5. SETUP TABEL ---
        TableColumn<Produk, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Produk, String> colNama = new TableColumn<>("Nama Produk");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNama.setPrefWidth(180);

        TableColumn<Produk, Double> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));

        TableColumn<Produk, Integer> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        table.setStyle( "-fx-background-color: white;" + "-fx-border-color: #f39c12;");

        // Fitur: Angka Merah jika stok tipis (< 10)
        colStok.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item.toString());
                    if (item < 10) {
                        setTextFill(Color.RED); setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(Color.BLACK); setStyle("");
                    }
                }
            }
        });

        TableColumn<Produk, String> colKategori = new TableColumn<>("Kategori");
        colKategori.setCellValueFactory(new PropertyValueFactory<>("namaKategori"));
        colKategori.setPrefWidth(120);

        table.getColumns().addAll(colId, colNama, colHarga, colStok, colKategori);
        table.setMaxHeight(200);

        // --- 6. LOGIKA SEARCH (REAL-TIME) ---
        FilteredList<Produk> filteredData = new FilteredList<>(dataProduk, p -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(produk -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return produk.getNama().toLowerCase().contains(lowerCaseFilter);
            });
        });
        table.setItems(filteredData);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtNama.setText(newVal.getNama());
                txtHarga.setText(String.valueOf(newVal.getHarga()));
                txtStok.setText(String.valueOf(newVal.getStok()));
                cbKategori.setValue(newVal.getNamaKategori());
            }
        });
        btnAdd.setOnAction(e -> tambahProduk());
        btnEdit.setOnAction(e -> updateProduk());
        btnDel.setOnAction(e -> hapusProduk());
        btnClear.setOnAction(e -> bersihkanForm());
        
        txtSearch.setStyle(
        	    "-fx-border-color: #f39c12;" +
        	    "-fx-background-radius: 5;" +
        	    "-fx-border-radius: 5;"
        	);

     // --- 7. LAYOUT BACKGROUND ---
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        try {
          
        	Image bgImage = new Image(getClass().getResourceAsStream("/uas/indomaret/background.jpeg"));
        		
            BackgroundImage background = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT, 
                BackgroundRepeat.NO_REPEAT, 
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true) 
            );
            
            root.setBackground(new Background(background));
        } catch (Exception e) {
            System.out.println("Gagal memuat background: " + e.getMessage());
        }
        form.setStyle("-fx-background-color: rgba(244, 244, 244, 0.85); -fx-border-color: #ddd; -fx-border-radius: 5;");
        table.setStyle("-fx-opacity: 0.95;");
        lblJudul.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        lblSummary.setStyle("-fx-font-weight: bold;" + "-fx-text-fill: #d35400;");
        	
        root.getChildren().addAll(
            header, 
            new Separator(), 
            new Label("Pencarian & Inventori"), searchBox, table, 
            lblSummary,
            new Separator(), 
            new Label("Form Input Data"), form, btnBox
        );

        muatKategori();
        muatData();
        
        Scene scene = new Scene(root, 750, 650);
        primaryStage.setScene(scene);
        primaryStage.show();}
    
    private void muatKategori() {
        cbKategori.getItems().clear();
        categoryMap.clear();
        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM kategori")) {
            while (rs.next()) {
                String nama = rs.getString("nama_kategori");
                int id = rs.getInt("id_kategori");
                cbKategori.getItems().add(nama);
                categoryMap.put(nama, id);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void muatData() {
        dataProduk.clear();
        String sql = "SELECT p.*, k.nama_kategori FROM produk p JOIN kategori k ON p.id_kategori = k.id_kategori";
        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dataProduk.add(new Produk(
                        rs.getInt("id_produk"),
                        rs.getString("nama_produk"),
                        rs.getDouble("harga"),
                        rs.getInt("stok"),
                        rs.getInt("id_kategori"),
                        rs.getString("nama_kategori")
                ));
            }
            hitungSummary();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void hitungSummary() {
        int totalJenis = dataProduk.size();
        int totalStok = dataProduk.stream().mapToInt(Produk::getStok).sum();
        lblSummary.setText("Total Jenis Produk: " + totalJenis + " | Total Seluruh Stok di Gudang: " + totalStok);
        lblSummary.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
    }

    private void tambahProduk() {
        if (cbKategori.getValue() == null || txtNama.getText().isEmpty()) return;
        String sql = "INSERT INTO produk (nama_produk, harga, stok, id_kategori) VALUES (?, ?, ?, ?)";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtNama.getText());
            pstmt.setDouble(2, Double.parseDouble(txtHarga.getText()));
            pstmt.setInt(3, Integer.parseInt(txtStok.getText()));
            pstmt.setInt(4, categoryMap.get(cbKategori.getValue()));
            pstmt.executeUpdate();
            muatData();
            bersihkanForm();
        } catch (Exception e) { showAlert("Error", "Gagal input: " + e.getMessage()); }
    }

    private void updateProduk() {
        Produk s = table.getSelectionModel().getSelectedItem();
        if (s == null) return;
        String sql = "UPDATE produk SET nama_produk=?, harga=?, stok=?, id_kategori=? WHERE id_produk=?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtNama.getText());
            pstmt.setDouble(2, Double.parseDouble(txtHarga.getText()));
            pstmt.setInt(3, Integer.parseInt(txtStok.getText()));
            pstmt.setInt(4, categoryMap.get(cbKategori.getValue()));
            pstmt.setInt(5, s.getId());
            pstmt.executeUpdate();
            muatData();
            bersihkanForm();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void hapusProduk() {
        Produk s = table.getSelectionModel().getSelectedItem();
        if (s == null) return;
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM produk WHERE id_produk=?")) {
            pstmt.setInt(1, s.getId());
            pstmt.executeUpdate();
            muatData();
            bersihkanForm();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void bersihkanForm() {
        txtNama.clear(); txtHarga.clear(); txtStok.clear(); txtSearch.clear();
        cbKategori.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setContentText(m); a.showAndWait();
    }
}