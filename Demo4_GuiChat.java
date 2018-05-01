package com.heima.socket;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Demo4_GuiChat extends Frame{

	private TextField tf;
	private Button send;
	private Button log;
	private Button clear;
	private Button shake;
	private TextArea viewText;
	private TextArea sendText;
	private DatagramSocket socket;
	private BufferedWriter bw;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public Demo4_GuiChat() throws IOException{
		init();
		southPanel();
		centerPanel();
		event();
	}
	public void event(){
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				try {
					bw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				socket.close();
				System.exit(0);
			}
		});
		send.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					send();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		});	
		log.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					logFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		clear.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
					viewText.setText("");
			}
		});
		shake.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					send(new byte[]{-1},tf.getText());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		sendText.addKeyListener(new KeyAdapter(){//发送区里按Ctrl+enter，发送消息事件
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()){//isControlDown() 是ctrl键是否按下
					try {
						send();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
	}
	private void shake() throws InterruptedException {
		// TODO Auto-generated method stub
		int x = this.getLocation().x;//获取横坐标位置
		int y = this.getLocation().y;//获取纵坐标 
		for(int i=0;i<5;i++){
			this.setLocation(x+20, y+20);
			Thread.sleep(20);
			this.setLocation(x+20, y-20);
			Thread.sleep(20);
			this.setLocation(x-20, y+20);
			Thread.sleep(20);
			this.setLocation(x-20, y-20);
			Thread.sleep(20);
			this.setLocation(x, y);
			Thread.sleep(20);
		}
	}
	private void logFile() throws IOException {
		// TODO Auto-generated method stub
		bw.flush();
		FileInputStream fis = new FileInputStream("config.txt");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();//在内存中创建缓冲区
		int len;
		byte[] arr = new byte[8192];
		while((len = fis.read(arr))!=-1){
			baos.write(arr,0,len);
		}
		String str = baos.toString();
		System.out.println("123");
		
		viewText.setText(str);
		fis.close();
	}
	private void send(byte[] arr,String ip) throws IOException{
		DatagramPacket packet = new DatagramPacket(arr,arr.length,InetAddress.getByName(ip),9999);
		socket.send(packet);//	发送数据
	}
	private void send() throws IOException{
		String message = sendText.getText();//获取发送区的内容
		String ip = tf.getText();
		ip = ip.trim().length()==0?"255.255.255.255":ip;
		send(message.getBytes(),ip);
		
		String time= getCurrentTime();//获取当前时间
		String str = time+"我对"+(ip.equals("255.255.255.255") ? "所有人" : ip)+"说:\r\n"+message+"\r\n";
		viewText.append(str);//将信息添加到显示区
		bw.write(str);
		sendText.setText("");
		
	}
	private String getCurrentTime(){
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");	
		return sdf.format(d);
		
	}
	public void centerPanel(){
		Panel center = new Panel();//创建中间的Panel
		viewText = new TextArea();
		sendText = new TextArea(5,1);
		
		center.setLayout(new  BorderLayout());//设置边界布局管理器
		center.add(sendText,BorderLayout.SOUTH);//发送文本区域放在南边
		center.add(viewText,BorderLayout.CENTER);//显示文本区域放中间
		viewText.setEditable(false);//设置不可以编辑
		viewText.setBackground(Color.white);//设置背景颜色
		sendText.setFont(new Font("xxx",Font.PLAIN,18));
		viewText.setFont(new Font("xxx",Font.PLAIN,18));
		this.add(center,BorderLayout.CENTER);
		
	}
	public void southPanel(){
		Panel south = new Panel();
		tf = new TextField(15);
		tf.setText("127.0.0.1");
		send = new Button("发 送");
		log = new Button("记 录");
		clear = new Button("清 屏");
		shake = new Button("震 动");
		south.add(tf);
		south.add(send);
		south.add(log);
		south.add(clear);
		south.add(shake);

		this.add(south,BorderLayout.SOUTH);
	}
	private void init() throws IOException {
		this.setLocation(400,60);
		this.setSize(400,600);
		socket = new DatagramSocket();
		bw = new BufferedWriter(new FileWriter("config.txt",true));
		new Receive().start();
		this.setVisible(true);
	}
	private class Receive extends Thread{//接受和发送需要同时执行，所以定义为多线程
		public  void run(){
			try {
				DatagramSocket socket = new DatagramSocket(9999);
				DatagramPacket packet = new DatagramPacket(new byte[1024],1024);
				
				while(true){
					socket.receive(packet);//接受数据
					byte[] arr = packet.getData();//获取字节数据
					int len = packet.getLength();//获取有效长度
					if(arr[0]==-1&&len==1){//如果发过来的数组，第一个存储的值是-1，并且数长度是1
						shake();
						continue;//震动不需要下面的执行
					}
					
					String message = new String(arr,0,len);//转为字符串
					
					String time =  getCurrentTime();//获取当前时间
					String ip = packet.getAddress().getHostAddress();//获取ip
					String str = time+" "+ip+" 对我说:\r\n"+message+"\r\n\r\n";//alt+shift+l抽取局部变量
					bw.write(str);
					viewText.append(str);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
 		
		
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		new  Demo4_GuiChat();
		
	}

}
