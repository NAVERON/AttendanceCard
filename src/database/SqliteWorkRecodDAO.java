/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import user.WorkRecord;

/**
 *
 * @author ERON
 */
public class SqliteWorkRecodDAO implements WorkRecordDAO{

    private Connection connection = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private List<WorkRecord> RECORDS = new LinkedList<WorkRecord>();

    private String protocol = "jdbc:sqlite:";
    private String dbName = "literecords.db";
    private String createString = "CREATE TABLE IF NOT EXIST workrecords ("
            + "id VARCHAR(30),"
            + "owner VARCHAR(30),"
            + "work_name VARCHAR(100),"
            + "system_name VARCHAR(100),"
            + "work_acount DOUBLE,"
            + "work_content VARCHAR(500),"
            + "record_time VARCHAR(30),"
            + "isDraft INT"
            + ")";

    @Override
    public void setup() {
        try {
            connection = DriverManager.getConnection(protocol + dbName);
            statement = connection.createStatement();
            statement.setQueryTimeout(10);  // set timeout to 30 sec.

            statement.executeUpdate(createString);
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DerbyWorkRecordDAO.class.getName()).log(Level.SEVERE, null, ex);

            Alert create_database_fault = new Alert(Alert.AlertType.WARNING);
            create_database_fault.setContentText("数据库创建失败");
            create_database_fault.showAndWait();
        }
    }

    @Override
    public void connect() {
        try {
            connection = DriverManager.getConnection(protocol + dbName);
        } catch (SQLException ex) {
            Logger.getLogger(DerbyWorkRecordDAO.class.getName()).log(Level.SEVERE, null, ex);

            Alert connect_database_fault = new Alert(Alert.AlertType.WARNING);
            connect_database_fault.setContentText("数据库连接失败");
            connect_database_fault.show();
        }
    }

    @Override
    public void close() {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DerbyWorkRecordDAO.class.getName()).log(Level.SEVERE, null, ex);

            Alert close_database_fault = new Alert(Alert.AlertType.INFORMATION);
            close_database_fault.setContentText("数据库正在关闭");
            close_database_fault.show();
        }
    }
    
    @Override
    public long insertWorkrecord(WorkRecord workrecord) {
        try {
            long acount = 0L;
            connection = DriverManager.getConnection(protocol + dbName);

            String insert = "INSERT INTO workrecords (id, owner, work_name, system_name, work_acount, work_content, record_time, isDraft) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(insert);
            preparedStatement.setString(1, workrecord.getId());
            preparedStatement.setString(2, workrecord.getOwner());
            preparedStatement.setString(3, workrecord.getWork_name());
            preparedStatement.setString(4, workrecord.getSystem_name());
            preparedStatement.setDouble(5, workrecord.getWork_acount());
            preparedStatement.setString(6, workrecord.getWork_content());
            preparedStatement.setString(7, workrecord.getRecord_time());
            preparedStatement.setInt(8, workrecord.isDraft);
            acount = preparedStatement.executeUpdate();

            connection.commit();
            preparedStatement.close();

            return acount;
        } catch (SQLException ex) {
            Logger.getLogger(DerbyWorkRecordDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1L;
    }

    @Override
    public boolean updateWorkrecord(WorkRecord workrecord) {
        try {
            connection = DriverManager.getConnection(protocol + dbName);
            String update = "UPDATE workrecords SET "
                    + "owner = ?, "
                    + "work_name = ?, "
                    + "system_name = ?, "
                    + "work_acount = ?, "
                    + "work_content = ?, "
                    + "record_time = ?, "
                    + "isDraft = ? "
                    + "WHERE id = ?";
            preparedStatement = connection.prepareStatement(update);
            preparedStatement.setString(1, workrecord.getOwner());
            preparedStatement.setString(2, workrecord.getWork_name());
            preparedStatement.setString(3, workrecord.getSystem_name());
            preparedStatement.setDouble(4, workrecord.getWork_acount());
            preparedStatement.setString(5, workrecord.getWork_content());
            preparedStatement.setString(6, workrecord.getRecord_time());
            preparedStatement.setInt(7, workrecord.isDraft);
            preparedStatement.setString(8, workrecord.getId());
            preparedStatement.executeUpdate();
            
            connection.commit();
            preparedStatement.close();
        } catch (SQLException ex) {
        }
        return false;
    }

    @Override
    public boolean deleteWorkrecord(WorkRecord workrecord) {
        try {
            connection = DriverManager.getConnection(protocol + dbName);
            
            String delete = "DELETE FROM workrecords WHERE id = ?";
            preparedStatement = connection.prepareStatement(delete);
            preparedStatement.setString(1, workrecord.getId());
            preparedStatement.executeUpdate();
            
            connection.commit();
            preparedStatement.close();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DerbyWorkRecordDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public List<WorkRecord> findWorkrecordById(String id) {
        try {
            connection = DriverManager.getConnection(protocol + dbName);

            String findById = "SELECT * FROM workrecords WHERE id = ?";
            preparedStatement = connection.prepareStatement(findById);
            preparedStatement.setString(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
                java.util.Date now = sdf.parse(rs.getString("record_time"));
                WorkRecord workrecord = new WorkRecord(
                        rs.getString("owner"),
                        rs.getString("work_name"),
                        rs.getString("system_name"),
                        rs.getDouble("work_acount"),
                        rs.getString("work_content"),
                        now,
                        rs.getInt("isDraft")
                );
                RECORDS.add(workrecord);
            }
            connection.commit();
            preparedStatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DerbyWorkRecordDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SqliteWorkRecodDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return RECORDS;
    }

    @Override
    public List<WorkRecord> findWorkrecordisDraft(boolean isDraft) {
        int is;
        if ( isDraft ) {
            is = 1;
        } else {
            is = 0;
        }
        try {
            String findByisDraft = "SELECT * FROM workrecords WHERE isDraft = ?";
            preparedStatement = connection.prepareStatement(findByisDraft);
            preparedStatement.setInt(1, is);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
                java.util.Date now = sdf.parse(rs.getString("record_time"));
                WorkRecord workrecord = new WorkRecord(
                        rs.getString("owner"),
                        rs.getString("work_name"),
                        rs.getString("system_name"),
                        rs.getDouble("work_acount"),
                        rs.getString("work_content"),
                        now,
                        rs.getInt("isDraft")
                );
                RECORDS.add(workrecord);
            }
            connection.commit();
            preparedStatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DerbyWorkRecordDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SqliteWorkRecodDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return RECORDS;
    }

    
}



