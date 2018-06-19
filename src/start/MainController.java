package start;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.Draft_WorkRecord;
import ui.Submit_WorkRecord;
import user.SubmitToRemote;
import user.User;
import user.VerifyUser;
import user.WorkRecord;

public class MainController implements Initializable {

    @FXML // fx:id="work_1_textarea"
    private TextArea work_1_textarea; // Value injected by FXMLLoader

    @FXML // fx:id="work_2_textarea"
    private TextArea work_2_textarea; // Value injected by FXMLLoader

    @FXML // fx:id="work_4_textarea"
    private TextArea work_4_textarea; // Value injected by FXMLLoader

    @FXML // fx:id="submit_list"
    private VBox submit_list; // Value injected by FXMLLoader

    @FXML // fx:id="draft_list"
    private VBox draft_list; // Value injected by FXMLLoader

    @FXML // fx:id="open_web_btn"
    private Button open_web_btn; // Value injected by FXMLLoader

    @FXML // fx:id="save_draft_btn"
    private Button save_draft_btn; // Value injected by FXMLLoader

    @FXML // fx:id="login_btn"
    private Button login_btn; // Value injected by FXMLLoader

    @FXML // fx:id="work_3_textarea"
    private TextArea work_3_textarea; // Value injected by FXMLLoader

    @FXML // fx:id="submit_btn"
    private Button submit_btn; // Value injected by FXMLLoader
    @FXML
    private Button test_db;
    
    // 自定义变量
    private User loggedUser = null; // 指向当前登录的用户
    private Stage primaryStage = null;
    private RecoderModel model = null; // 模型里面包含了数据控制，用来后台数据和前台数据的桥接
    private String work_name, system_name, work_content;
    private double work_acount;

    public MainController(Stage primaryStage, RecoderModel model) {
        this.primaryStage = primaryStage;
        this.model = model;
    }
    @FXML
    public void print_db(){
        //输出数据库信息
        List<WorkRecord> drafts = model.getIsDraft(true);
        List<WorkRecord> submits = model.getIsDraft(false);
        System.out.println(drafts.toString());
        System.out.println(submits.toString());
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(loggedUser == null){
            showLoginStage();
        }
        //从数据库读取并更新界面 -------------------------- 可以被动载入
        List<WorkRecord> drafts = model.getIsDraft(true);
        List<WorkRecord> submits = model.getIsDraft(false);
        //ObservableList<WorkRecord> drafts_ui = FXCollections.observableList(drafts);
        //ObservableList<WorkRecord> submits_ui = FXCollections.observableList(submits);
        if (drafts != null) {
            for (WorkRecord workrecord : drafts) {
                draft_list.getChildren().add(new Draft_WorkRecord(workrecord, this));
            }
        }
        if (submits != null) {
            for (WorkRecord workrecord : submits) {
                submit_list.getChildren().add(new Submit_WorkRecord(workrecord, this));
            }
        }
    }
    
    @FXML
    void save_draft(ActionEvent event) {
        if (!verifyTextArea()) {
            return;
        }
        if(loggedUser == null){
            showLoginStage();
            return;
        }
        WorkRecord workrecord = new WorkRecord(loggedUser.getName(), work_name, system_name, work_acount, work_content, new Date(), 1);
        System.out.println("main controller : save draft : " + workrecord.toString());
        //添加到数据库
        model.addNewRecord(workrecord);
        //界面变化
        draft_list.getChildren().add(new Draft_WorkRecord(workrecord, this));

        clearAllTextArea();
    }
    
    @FXML
    void submit(ActionEvent event) {
        if (!verifyTextArea()) {
            return;
        }
        if (loggedUser == null) {
            showLoginStage();
            return;
        }
        WorkRecord workrecord = new WorkRecord(loggedUser.getName(), work_name, system_name, work_acount, work_content, new Date(), 0);
        SubmitToRemote submitToremote = new SubmitToRemote(workrecord);
        //数据库添加一个记录
        model.addNewRecord(workrecord);
        
        submit_list.getChildren().add(new Submit_WorkRecord(workrecord, this));
        clearAllTextArea();
    }

    @FXML
    void login(ActionEvent event) {
        if (loggedUser != null) { // 重复点击，表示登出
            logout(event);
            return;
        }
        showLoginStage();
    }

    public void logout(ActionEvent event) {
        Alert logout_alert = new Alert(AlertType.WARNING);
        logout_alert.setHeaderText("注意");
        logout_alert.setContentText("即将登出");
        logout_alert.setTitle("登出 ：  " + loggedUser.getName());
        logout_alert.showAndWait();
        
        this.login_btn.setText("登陆");
        this.loggedUser = null;
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    model.close();
                } catch (Exception ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @FXML
    void open_web(ActionEvent event) {
        Stage webview = new Stage();
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load("http://121.12.250.200:8089/");  //http://121.12.250.200:8089/

        Scene scene = new Scene(browser);
        webview.setScene(scene);
        webview.show();
    }

    public void showLoginStage() {
        Stage loginStage = new Stage();
        loginStage.initModality(Modality.WINDOW_MODAL);
        loginStage.initOwner(primaryStage);

        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        Label prompt_label = new Label("请输入用户名和密码");
        TextField userName_textfield = new TextField();
        userName_textfield.setPromptText("输入用户名");
        PasswordField userPassword_passwordfield = new PasswordField();
        userPassword_passwordfield.setPromptText("输入密码");

        Button signin_btn = new Button();
        signin_btn.setText("签到");

        root.getChildren().addAll(prompt_label, userName_textfield, userPassword_passwordfield, signin_btn);
        Scene scene = new Scene(root, 250, 150);
        loginStage.setScene(scene);
        loginStage.show();

        signin_btn.setOnAction(event -> {
            String name = userName_textfield.getText().trim();
            String password = userPassword_passwordfield.getText().trim();

            VerifyUser verifyUser = new VerifyUser(new User(name, password)); // 新建验证对象，异步处理
            userName_textfield.clear();
            userPassword_passwordfield.clear();
            Thread verify = new Thread(verifyUser);
            //verify.start();

            if (verifyUser.isCorrect()) {
                this.loggedUser = new User(name, password);  //或者爬取用户图片，设置成ImageView
                this.login_btn.setText(name);
                loginStage.close();

                //这里需要独立线程处理，否则会卡住
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            model.setup();
                        } catch (Exception ex) {
                            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            } else {
                Alert login_error_alert = new Alert(AlertType.ERROR);
                login_error_alert.setHeaderText("签到错误");
                login_error_alert.setContentText("请输入正确的用户名和密码");
                login_error_alert.setTitle("错误");
                login_error_alert.showAndWait();
            }
        });
    }
    
    //判断界面上的内容是否填写完整
    public boolean verifyTextArea() {
        work_name = work_1_textarea.getText();
        system_name = work_2_textarea.getText();
        work_content = work_4_textarea.getText();

        String work_acount_temp = work_3_textarea.getText();
        if (work_name.isEmpty() || system_name.isEmpty() || work_content.isEmpty() || work_acount_temp.isEmpty()) {  //检测是否有空值
            Alert verify_alert = new Alert(AlertType.ERROR);
            verify_alert.setHeaderText("输入信息有误");
            verify_alert.setContentText("请确保所有内容已经填写");
            verify_alert.setTitle("验证输入内容");
            verify_alert.showAndWait();
            return false;
        }

        Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d+)?$");  //检测工作量是否是数字类型
        if (pattern.matcher(work_acount_temp).matches()) {
            work_acount = Double.parseDouble(work_acount_temp);
        } else {
            Alert verify_alert = new Alert(AlertType.ERROR);
            verify_alert.setHeaderText("工作量输入错误");
            verify_alert.setContentText("请在工作量输入框输入数字");
            verify_alert.setTitle("验证输入内容");
            verify_alert.showAndWait();
            return false;
        }
        return true;
    }
    
    public void showContent(WorkRecord workrecord){
        this.work_1_textarea.setText(workrecord.getWork_name());
        this.work_2_textarea.setText(workrecord.getSystem_name());
        this.work_3_textarea.setText(String.format( "%20.5", String.valueOf(workrecord.getWork_acount()) ));
        this.work_4_textarea.setText(workrecord.getWork_content());
    }
    
    public void deleteSelected(WorkRecord workrecord, HBox delHBox){
        //model.deleteRecord(workrecord);
        //界面更新
        if(workrecord.isDraft == 1){
            draft_list.getChildren().remove(delHBox);
        }else if(workrecord.isDraft == 0){
            submit_list.getChildren().remove(delHBox);
        }
    }
    
    public void showAll(){  //测试数据库数据
        List<WorkRecord> list = model.getIsDraft(true);
        list.addAll(model.getIsDraft(false));
        System.out.println(list);
    }
    public boolean clearAllTextArea() {
        work_1_textarea.clear();
        work_2_textarea.clear();
        work_3_textarea.clear();
        work_4_textarea.clear();

        return true;
    }

}
