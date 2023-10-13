package music;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import javax.swing.*;


public class MusicClient extends JFrame implements Runnable {
	private static int WIDTH = 500;
	private static int HEIGHT = 100;
	private static String host = "localhost";
	private static int port = 9898;
	private ObjectInputStream in;
	private DataOutputStream out;
	private Socket socket;
	private String current_music;
	private AudioClip clip;
	private JComboBox music_selector;
	private JButton play;
	private JButton loop;
	private JButton stop;
	private boolean isConnect;
	public MusicClient() {
		super("Jasmine Music Player");
		this.setSize(MusicClient.WIDTH, MusicClient.HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		createMenu();
		//Image image = new ImageIcon("Cloud.jpg").getImage();
		JPanel control = new JPanel();
		this.add(control);
		music_selector = new JComboBox();
		music_selector.addItem("No music before login");
		control.add(music_selector);
		control.setBackground(new java.awt.Color(97, 219, 215));
		play = new JButton("Play");
		loop = new JButton("Loop");
		stop = new JButton("Stop");
		play.addActionListener(e -> {
			if(isConnect == false) {
				JOptionPane.showMessageDialog(null, "Please start the server and login :)");
			}
			else {
				clip.play();
			}
		});
		loop.addActionListener(e -> {
			if(isConnect == false) {
				JOptionPane.showMessageDialog(null, "Please start the server and login :)");
			}
			else {
				clip.loop();
			}
		});
		stop.addActionListener(e -> {
			if(isConnect == false) {
				JOptionPane.showMessageDialog(null, "Please start the server and login :)");
			}
			else {
				clip.stop();
			}
		});
		control.add(play);
		control.add(loop);
		control.add(stop);
		this.setVisible(true);
		isConnect = false;
		current_music = "For A Better Way.wav";
		File file = new File(current_music);
		URI uri = file.toURI();
		URL url = null;
		try {
			url = uri.toURL();
			clip = Applet.newAudioClip(getClass().getResource(current_music));
		} 
		catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Control ♫");
		JMenuItem loginItem = new JMenuItem("Login");
		JMenuItem registerItem = new JMenuItem("Register");
		menu.add(loginItem);
		menu.add(registerItem);
		loginItem.addActionListener(e -> {
			JFrame login = new JFrame("Login");
			login.setSize(MusicClient.WIDTH, 100);
			login.setLayout(new BorderLayout());
			JPanel login_info = new JPanel();
			login.add(login_info);
			JLabel title = new JLabel("Hello ♫ ");
			title.setForeground(new java.awt.Color(201, 0, 51));
			JLabel user_title = new JLabel("User Name:");
			JTextField user = new JTextField(10);
			JLabel pwd_title = new JLabel("Password:");
			JPasswordField password = new JPasswordField(10);
			JButton submit = new JButton("Login");
			login_info.add(title);
			login_info.add(user_title);
			login_info.add(user);
			login_info.add(pwd_title);
			login_info.add(password);
			login_info.add(submit);
			login.setVisible(true);
			submit.addActionListener(submit_e -> {
				String user_name = user.getText();
				String user_password = new String(password.getPassword());
				DatabaseClient db_client = new DatabaseClient();
				boolean login_status = db_client.login(user_name, user_password);
				if(login_status) {
					login.setVisible(false);
					login.dispose();
					try {
						socket = new Socket(host,port);
						isConnect = true;
						music_selector.removeItem("No music before login");
						music_selector.addItem("‪Please choose a music");
						music_selector.addItem("For A Better Way.wav");
						music_selector.addItem("Nightwish Amaranth.wav");
						music_selector.addItem("BoyGirl.wav");
						music_selector.addItem("Alone.wav");
						music_selector.addItem("Sun On Sunday.wav");
						music_selector.addItem("Lark In The Clear Air.wav");
						ReadThread rth = new ReadThread(socket,this);
						rth.start();
						WriteThread wth = new WriteThread(socket,this);
						wth.start();
						menu.remove(loginItem);
						menu.remove(registerItem);
					} 
					catch (UnknownHostException e1) {
						JOptionPane.showMessageDialog(null, "Please start the server before login :)");
						System.out.println("Server not found: " + e1.getMessage());
						e1.printStackTrace();
					}
					catch (IOException e2) {
						JOptionPane.showMessageDialog(null, "Please start the server before login :)");
						System.out.println("I/O Error: " + e2.getMessage());
						e2.printStackTrace();
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "User name or password incorrect :)");
				}
			});
		});

		registerItem.addActionListener(e -> {
			JFrame register = new JFrame("Register");
			register.setSize(MusicClient.WIDTH, 100);
			this.setLayout(new BorderLayout());
			JPanel reg_info = new JPanel();
			register.add(reg_info);
			JLabel title = new JLabel("Welcome ♫");
			title.setForeground(new java.awt.Color(201, 0, 51));
			JLabel user_title = new JLabel("User Name:");
			JTextField user = new JTextField(10);
			JLabel pwd_title = new JLabel("Password:");
			JPasswordField password = new JPasswordField(10);
			JButton submit = new JButton("Sign up");
			reg_info.add(title);
			reg_info.add(user_title);
			reg_info.add(user);
			reg_info.add(pwd_title);
			reg_info.add(password);
			reg_info.add(submit);
			register.setVisible(true);
			submit.addActionListener(reg_e -> {
				String user_name = user.getText();
				String user_password = new String(password.getPassword());
				DatabaseClient db_client = new DatabaseClient();
				boolean reg_status = db_client.register(user_name, user_password);
				if(reg_status == true) {
					register.setVisible(false);
					register.dispose();
					JOptionPane.showMessageDialog(null, "Congradualtions! Sign up success :)");
				}
				else {
					JOptionPane.showMessageDialog(null, "Sorry, sign up fail. Please try again :)");
				}
			});
		});
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener((e) -> System.exit(0));
		menu.add(exitItem);
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
	}
	
	public class ReadThread extends Thread {
		Socket socket;
		MusicClient client;
		ObjectInputStream in;
		public ReadThread(Socket s, MusicClient c) {
			socket = s;
			client = c;
			try {
				in = new ObjectInputStream(socket.getInputStream());
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void run() {
			while(true) {
				try {
					URL url = (URL)in.readObject();
					clip = Applet.newAudioClip(url);
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public class WriteThread extends Thread {
		Socket socket;
		MusicClient client;
		DataOutputStream out;

		public WriteThread(Socket s, MusicClient c) throws IOException {
			socket = s;
			client = c;
			out = new DataOutputStream(socket.getOutputStream());
		}

		public void run() {
			client.music_selector.addActionListener(new ActionListener() {
		        @Override
		        public void actionPerformed (ActionEvent e) {
		        	if(clip != null) {
		        		clip.stop();
		        	}
		        	current_music = music_selector.getSelectedItem().toString();
		        	// write to server require music
	            	try {
	            		byte[] data = current_music.getBytes("UTF-8");
						out.writeInt(data.length);
						out.write(data);
					} 
	            	catch (IOException e1) {
	            		System.out.println("Fail to connect server!");
						e1.printStackTrace();
					}
		        }
		    });
		}
	}
	
	public static void main(String[] args) {
		MusicClient musicClient = new MusicClient();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
