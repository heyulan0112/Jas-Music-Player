package music;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class DatabaseClient {
    Connection conn = null;
    PreparedStatement ps = null;
    int result = 0;
    String dbDriver = "com.mysql.cj.jdbc.Driver";
    String URL = "jdbc:mysql://b0zonon3rfanzpocrf8u-mysql.services.clever-cloud.com/b0zonon3rfanzpocrf8u";
    String user = "udivthawfsn70lpy";
    String pwd = "TMZqJdL1b6EtGZ9TW8Vu";
	public DatabaseClient() {
	    try {
	        Class.forName(dbDriver);
	        conn=DriverManager.getConnection(URL,user,pwd);
	    }
	    catch (Exception e){
	        e.printStackTrace();
	    }
	}
	
	public boolean register(String username,String password) {
		String sql = "insert into user(id,name,password) values (?,?,?)";
		String sql_rows = "select count(*) from user;";
        try {
        	ps = conn.prepareStatement(sql_rows);
        	ResultSet rs = ps.executeQuery();
        	int curr_id = 0;
        	if (rs.next()){
        		curr_id = rs.getInt("count(*)");
        	}
			ps = conn.prepareStatement(sql);
            ps.setInt(1,curr_id);
            ps.setString(2,username);
            ps.setString(3,password);
            result = ps.executeUpdate();
	        System.out.println("Add user to database success!");
	        return true;
		}  
        catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        return false;
	}
	
	public boolean login(String username,String password){
		String sql = "select * from user where name = ? and password = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();
        	if (rs.next()){
	            System.out.println("Search user in database success!");
	            System.out.println(rs.getString("name"));
	            System.out.println(rs.getString("password"));
	            return true;
        	}
        	else {
	            System.out.println("Search user in database error!");
	            return false;
        	}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return false;
	}
	
	public void insertMusic(int id, String name, String path) {
        String sql="insert into songs(id,name,file) values (?,?,?)";
        try {
			ps = conn.prepareStatement(sql);
	        ps.setInt(1,id);
	        ps.setString(2, name);
	        File file =new File(path);
	        InputStream in;
			try {
				in = new FileInputStream(file);
		        ps.setBinaryStream(3,in,(long)file.length());
		        result = ps.executeUpdate();
		        try {
					in.close();
				} 
		        catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} 
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        if (result>0){
	            System.out.println("Write audio file to database success!");
	        }
	        else {
	            System.out.println("Write audio file to database error!");
	        }
		} 
        catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
