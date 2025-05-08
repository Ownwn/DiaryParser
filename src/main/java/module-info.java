module com.example.diaryparser {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.diaryparser to javafx.fxml;
    exports com.example.diaryparser;
}