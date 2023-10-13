package music;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import java.sql.*;

public class MusicServer extends JFrame implements Runnable {
	private static int WIDTH = 400;
	private static int HEIGHT = 300;
	private static int port = 9898; 
	JTextArea area;
	public ArrayList<MusicThread> threadSet = new ArrayList<MusicThread>();
	
	public MusicServer() {
		super("Music Server");
		this.setSize(MusicServer.WIDTH, MusicServer.HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createMenu();
		area = new JTextArea();
		this.add(area);
		this.setVisible(true);
	}
	
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener((e) -> System.exit(0));
		menu.add(exitItem);
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
	}
	
	public class MusicThread extends Thread {
		Socket socket;
		MusicServer server;
		int client_id;
		DataInputStream in;
		ObjectOutputStream out;
	    Connection conn=null;
	    PreparedStatement ps=null;
	    ResultSet rs=null;
	    int result = 0;
	    String dbDriver = "com.mysql.cj.jdbc.Driver";
	    String URL = "jdbc:mysql://b0zonon3rfanzpocrf8u-mysql.services.clever-cloud.com/b0zonon3rfanzpocrf8u";
	    String user = "udivthawfsn70lpy";
	    String pwd = "TMZqJdL1b6EtGZ9TW8Vu";
		
		public MusicThread(Socket clientSocket, MusicServer s, int id) {
			socket = clientSocket;
			server = s;
			client_id = id;
	        try {
	        	this.in = new DataInputStream(socket.getInputStream());
				this.out = new ObjectOutputStream(socket.getOutputStream());
			} 
	        catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	    public void run() {
	    	try {
	    		while(true) {
					int length = in.readInt();
					byte[] data = new byte[length];
					in.readFully(data);
					String current_music = new String(data,"UTF-8");
					// If song exists in local then use it
					// If song doesn't exist in local then go to database require resource
					URL url = null;
					try {
						url = new URL("file:" + current_music);
					}
					catch (MalformedURLException e) {
						throw new IllegalArgumentException("could not play '" + current_music + "' in your region.", e);
					}
					File myMusicFile = new File(url.getPath());
					if(!myMusicFile.exists()) { 
					    try {
					        Class.forName(dbDriver);
					        conn = DriverManager.getConnection(URL,user,pwd);
					    }
					    catch (Exception e){
					        e.printStackTrace();
					    }
			            String sql = "select * from songs where name=?";
			            try {
							ps = conn.prepareStatement(sql);
				            ps.setString(1,current_music);
				            rs = ps.executeQuery();
				            if (rs.next()){
				                InputStream in_data = rs.getBinaryStream("file");
				                OutputStream out_data = new FileOutputStream(current_music);
				                byte[] temp = new byte[1024];
				                int len = -1;
				                while ((len = in_data.read(temp))!=-1){
				                	out_data.write(temp);
				                }
				                in_data.close();
				                out_data.close();
				                System.out.println("Fetch music from database success!");
				            }
						} 
			            catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			            finally {
			                // release resource
			                if (rs!=null){
			                    try {
			                        rs.close();
			                    } catch (SQLException e) {
			                        e.printStackTrace();
			                    }
			                }
			                if (ps!=null){
			                    try {
			                        ps.close();
			                    } catch (SQLException e) {
			                        e.printStackTrace();
			                    }
			                }
			                if (conn!=null){
			                    try {
			                        conn.close();
			                    } catch (SQLException e) {
			                        e.printStackTrace();
			                    }
			                }
							out.writeObject(url);
							out.flush();
			            }
					}
					else {
						out.writeObject(url);
						out.flush();
					}
	    		}
			} 
	    	catch (IOException e) {
	    		System.out.println("Error in MusicTread: " + e.getMessage());
				e.printStackTrace();
			}
	    }
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat ("E MM dd hh:mm:ss zzz yyyy");
		area.append("Music Player server started at "+ ft.format(dNow) +'\n');
		
		int client_no = 1;
		while(true) {
			try {
				Socket socket = serverSocket.accept();
				area.append("Starting thread for client " + client_no + " at " + ft.format(dNow) + '\n');
				area.append("Client " + client_no+"'s host name is " + socket.getInetAddress().getHostName() + '\n');
				area.append("Client " + client_no+"'s IP Address is " + socket.getInetAddress().getHostAddress() + '\n');
				MusicThread newClient = new MusicThread(socket,this,client_no);
				threadSet.add(newClient);
				newClient.start();
				client_no++;
			} 
			catch (IOException e) {
				System.out.println("Error in the server: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) {
		MusicServer musicServer = new MusicServer();
		musicServer.run();
	}
	
}
