module org.example.cafepos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens org.example.cafepos to javafx.fxml;
    exports org.example.cafepos;
}