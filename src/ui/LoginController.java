package ui;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
//import library.assistant.ui.settings.Preferences;
//import library.assistant.util.LibraryAssistantUtil;
import org.apache.commons.codec.digest.DigestUtils;

public class LoginController implements Initializable {

    //private final static Logger LOGGER = LogManager.getLogger(LoginController.class.getName());
    
    @FXML
    private JFXTextField username;
    @FXML
    private JFXPasswordField password;
    
    //Preferences preference;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {  //初始化配置文件
        //preference = Preferences.getPreferences();
    }
    
    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String uname = username.getText();
        String pword = DigestUtils.shaHex(password.getText());  //对密码进行编码

        if (uname.equals( "admin" ) && pword.equals( "root" )) {
            closeStage();
            //loadMain();
            System.out.println("登陆成功");
            //LOGGER.log(Level.INFO, "User successfully logged in {}", uname);
        }
        else {
            username.getStyleClass().add("wrong-credentials");
            password.getStyleClass().add("wrong-credentials");
        }
    }
    
    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        // System.exit(0);
        System.out.println("取消登陆");
        return;
    }
    
    private void closeStage() {
        ((Stage) username.getScene().getWindow()).close();
    }

    void loadMain() {    //登陆成功后的动作
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/library/assistant/ui/main/main.fxml"));
            Stage stage = new Stage(StageStyle.DECORATED);  //StageStyle.UNDECORATED
            stage.setTitle("Library Assistant");
            stage.setScene(new Scene(parent));
            stage.show();
            //LibraryAssistantUtil.setStageIcon(stage);
        }
        catch (IOException ex) {
            //LOGGER.log(Level.ERROR, "{}", ex);
        }
    }

}
