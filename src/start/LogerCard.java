package start;


import database.DerbyWorkRecordDAO;
import database.SqliteWorkRecodDAO;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class LogerCard extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/Main.fxml"));
        //RecoderModel model = new RecoderModel(new DerbyWorkRecordDAO());
        RecoderModel model = new RecoderModel(new SqliteWorkRecodDAO());
        MainController maincontroller = new MainController(primaryStage, model);  //设置控制器
        loader.setController(maincontroller);
        
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("LogCard");
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(event -> {
            try {
                model.close();
            } catch (Exception ex) {
                Alert close_database_fault = new Alert(Alert.AlertType.WARNING);
                close_database_fault.setContentText("数据库关闭异常");
                close_database_fault.show();
                Logger.getLogger(LogerCard.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
