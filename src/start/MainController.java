package start;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
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
    @FXML
    private Button minimum;

    // 自定义变量
    private User loggedUser = null; //指向当前登录的用户
    private Stage primaryStage = null;
    private RecoderModel model = null; // 模型里面包含了数据控制，用来后台数据和前台数据的桥接
    private String work_name, system_name, work_content;
    private double work_acount;
    private VerifyUser verifyUser;

    public MainController(Stage primaryStage, RecoderModel model) {
        this.primaryStage = primaryStage;
        this.model = model;
    }

    @FXML
    public void print_db() {
        //输出数据库信息
        List<WorkRecord> drafts = model.getIsDraft(1);
        List<WorkRecord> submits = model.getIsDraft(0);
        if (drafts != null) {
            System.out.println("下面是草稿的数据 : ");
            System.out.println(drafts.toString());
        }
        if (submits != null) {
            System.out.println("下面是提交的数据 : ");
            System.out.println(submits.toString());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //从数据库读取并更新界面
        List<WorkRecord> drafts = model.getIsDraft(1);
        List<WorkRecord> submits = model.getIsDraft(0);
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

        //print_db();
    }

    @FXML
    void save_draft(ActionEvent event) {
        if (!verifyTextArea()) {
            return;
        }
        if (loggedUser == null) {
            showLoginStage();
            return;
        }
        WorkRecord workrecord = new WorkRecord(loggedUser.getName(), work_name, system_name, work_acount, work_content, new Date(), 1);
        model.addNewRecord(workrecord);
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
        //数据库添加一个记录
        model.addNewRecord(workrecord);

        submit_list.getChildren().add(new Submit_WorkRecord(workrecord, this));
        clearAllTextArea();
        //远程提交数据，异步处理，如果出错，重新从数据提取提交
        SubmitToRemote submitToremote = new SubmitToRemote(workrecord);
        submitToremote.run();
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

            verifyUser = new VerifyUser(new User(name, password)); // 新建验证对象，异步处理
            userName_textfield.clear();
            userPassword_passwordfield.clear();
            Thread verify = new Thread(verifyUser);
            //verify.start();

            if (verifyUser.isCorrect()) {
                this.loggedUser = new User(name, password);  //或者爬取用户图片，设置成ImageView
                this.login_btn.setText(name);
                loginStage.close();
                //这里需要独立线程处理，否则会卡住
                //以前这里新建线程   创建数据表，这个动作在程序启动时已经做好了
            } else {
                Alert login_error_alert = new Alert(AlertType.ERROR);
                login_error_alert.setHeaderText("签到错误");
                login_error_alert.setContentText("请输入正确的用户名和密码");
                login_error_alert.setTitle("错误");
                login_error_alert.showAndWait();
            }
        });
        
        ///////////////////////////////////////////////////////////////////////////////////
        Stage LoginStage = new Stage();
        Parent LoginParent = null;
        try {
            LoginParent = FXMLLoader.load(getClass().getResource("/ui/login.fxml")); //    /library/assistant/ui/login/login.fxml
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

        Scene LoginScene = new Scene(LoginParent);

        LoginStage.setScene(scene);
        LoginStage.show();
        LoginStage.setTitle("LoginRecordSystem");
        
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

    public void showContent(WorkRecord workrecord) {
        this.work_1_textarea.setText(workrecord.getWork_name());
        this.work_2_textarea.setText(workrecord.getSystem_name());

        Double temp_double = workrecord.getWork_acount();
        DecimalFormat df = new DecimalFormat("#,##0.0000");
        this.work_3_textarea.setText(df.format(temp_double));

        this.work_4_textarea.setText(workrecord.getWork_content());
    }

    public void deleteSelected(WorkRecord workrecord, HBox delHBox) {
        //界面更新
        if (workrecord.isDraft == 1) {
            draft_list.getChildren().remove(delHBox);
        } else if (workrecord.isDraft == 0) {
            submit_list.getChildren().remove(delHBox);
        }

        model.deleteRecord(workrecord);
    }

    public boolean clearAllTextArea() {
        work_1_textarea.clear();
        work_2_textarea.clear();
        work_3_textarea.clear();
        work_4_textarea.clear();

        return true;
    }

    private Timer notificationTimer = null;
    @FXML
    void minimum() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        primaryStage.hide();
        
        java.awt.Toolkit.getDefaultToolkit();
        final PopupMenu popup = new PopupMenu();
        
        URL url = this.getClass().getResource("..\\assets\\bulb.gif");
        Image image = new ImageIcon(url).getImage();
        final TrayIcon trayIcon = new TrayIcon(image);
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem aboutItem = new MenuItem("About");
        MenuItem displayItem = new MenuItem("Display");
        MenuItem exitItem = new MenuItem("Exit");

        //Add components to pop-up menu
        popup.add(aboutItem);
        popup.add(displayItem);
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.addActionListener((event) -> {
            Platform.runLater(() -> {
                if(notificationTimer != null){
                    notificationTimer.cancel();
                    notificationTimer = null;
                }
                primaryStage.show();
                tray.remove(trayIcon);
            });
        });
        aboutItem.addActionListener((event) -> {
            Platform.runLater(() -> {
                Alert about_alert = new Alert(AlertType.INFORMATION);
                about_alert.setContentText("日志管理系统");
                about_alert.showAndWait();
            });
        });
        displayItem.addActionListener((java.awt.event.ActionEvent e) -> {
            Platform.runLater(() -> {
                if (notificationTimer != null) {
                    notificationTimer.cancel();
                    notificationTimer = null;
                }
                primaryStage.show();
                tray.remove(trayIcon);
            });
        });
        exitItem.addActionListener((java.awt.event.ActionEvent e) -> {
            try {
                model.close();
            } catch (Exception ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            notificationTimer.cancel();
            notificationTimer = null;
            tray.remove(trayIcon);
            Platform.exit();
            System.exit(0);
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

        //执行后台定是程序
        if(notificationTimer == null){
            notificationTimer = new Timer();
        }
        notificationTimer.schedule( new TimerTask() {  //这个不能重复启动，后面再修改
            @Override
            public void run() {
                javax.swing.SwingUtilities.invokeLater(()
                        -> trayIcon.displayMessage(
                                "hello",
                                "The time is now " + new Date().toString(),
                                java.awt.TrayIcon.MessageType.INFO
                        )
                );
            }
        }, 0, 10000 );
    }
}
