package SQL;

import java.sql.*;

public class Connect {

    private static final String url = "jdbc:mysql://localhost:3306/chat?useUnicode=true&characterEncoding=utf8";
    private static final String user = "root";
    private static final String password = "12345";

    // JDBC variables for opening and managing connection
    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;

    public Connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url,user,password);
            stmt = con.createStatement();
        }catch (SQLException ex){} catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveMessage(String userName, String message){
        if (!isUserExists(userName))
            addUser(userName);
        addMessage(message);
        int messageId = getMessageByUserName(message);
        int userId = getUserByUserName(userName);
        try {
            String query = "INSERT INTO `chat`.`node`(`User_idUser`,`Message_idMessage`,`time`)" +
                    "VALUES("+userId+","+ messageId+",CURTIME())";
            stmt.executeUpdate(query);
        }catch (Exception ex){}
    }

    public void closeConnection(){
        try { con.close(); } catch(SQLException se) {}
        try { stmt.close(); } catch(SQLException se) {}
        try { rs.close(); } catch(SQLException se) {}
    }

    public String getStory(String from, String userName){
        String res = "";
        String query = "select user.Name, message.Message, node.time " +
                "from node " +
                "inner join user on user.idUser = node.User_idUser " +
                "inner join message on message.idMessage = node.Message_idMessage " +
                "where node.time > '"+from +"'  AND user.Name = '"+userName+"' " +
                "order by node.time";
        try {
            rs = stmt.executeQuery(query);
            while (rs.next()){
                res += rs.getString("Name") +
                        "(" + rs.getTime("time") + ") : " +
                        rs.getString("Message") + "\n";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    private void addMessage(String message){
        try {
            stmt.execute("insert into message(Message) value('"+message+"')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addUser(String userName){
        try {
            stmt.execute("insert into user(Name) value('"+userName+"')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isUserExists(String userName){
        int i = 0;
        try {
            rs = stmt.executeQuery("select * from user where user.Name = '" + userName +"'");
            while (rs.next()) {
                i++;
            }
        }catch (SQLException ex){}

        return i == 1;
    }

    private int getUserByUserName(String user){
        int id = -1;
        try {
            rs = stmt.executeQuery("select user.idUser from user where user.Name = '"+user+"'");
            while (rs.next())
                id = rs.getInt("idUser");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    private int getMessageByUserName(String message){
        int id = -1;
        try {
            rs = stmt.executeQuery("select Message.idMessage from Message where Message.Message = '"+message+"'");
            while (rs.next())
                id = rs.getInt("idMessage");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public void delete(String userName){
        int userId = getUserByUserName(userName);
        String query = "delete from node where node.User_idUser = " + userId;
        try {
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
