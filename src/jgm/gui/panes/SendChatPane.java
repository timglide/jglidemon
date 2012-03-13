package jgm.gui.panes;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jgm.glider.ChatType;
import jgm.glider.Command;
import jgm.glider.ConnectionAdapter;
import jgm.glider.Status;
import jgm.gui.GUI;

public class SendChatPane extends Pane implements ActionListener {
	static Logger log = Logger.getLogger(SendChatPane.class.getName());
	
	private JComboBox type;
	private JTextField to;
	private JTextField keys;
	private JButton send;
	private JButton reset;
	
	public SendChatPane(GUI gui) {
		super(gui);
		
		type = new JComboBox(ChatType.values());
		type.addActionListener(this);
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		add(type, c);
		
		to = new JTextField();
		to.setToolTipText("The person to send a whisper to");
		c.gridx++; c.weightx = 0.15;
		add(to, c);
		
		keys = new JTextField();
		keys.setToolTipText("<html>For Raw, type a full slash command just like you would in-game,<br>" +
							"for others the slash command will be added automatically,<br>" +
							"DO NOT try to send slash command unless using Raw");
		keys.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		add(keys, c);
				
		JPanel btns = new JPanel(new GridLayout(1, 0));
		
		send = new JButton("Send");
		send.addActionListener(this);
		btns.add(send);
		
		reset = new JButton("Reset");
		reset.addActionListener(this);
		btns.add(reset);
		
		c.gridx++; c.weightx = 0.0;
		add(btns, c);
		
/*		c.gridy++;
		add(new JLabel(
			"<html>Whisper and Say will both add the slash command and a carriage return.<br>" +
			"You must add everything for Raw, | = CR, #VK# = VK</html>",
			JLabel.CENTER
		), c);
*/
		
		setEnabled(false);
		
		gui.sm.connector.addListener(new ConnectionAdapter() {
			public void onConnect() {
				setEnabled(true);
			}
			
			public void onDisconnecting() {
				setEnabled(false);
			}
		});
	}

	public ChatType getChatType() {
		return (ChatType) type.getSelectedItem();
	}
	
	public void setChatType(ChatType chatType) {
		type.setSelectedItem(chatType);
	}
	
	public void setWhisperTarget(String name) {
		type.setSelectedItem(ChatType.WHISPER);
		to.setText(name);
		keys.setText("");
		keys.requestFocusInWindow();
	}
	
	public void resetFields() {
//		to.setText("");
		keys.setText("");
	}
	
	@Override
	public void setEnabled(boolean b) {
		type.setEnabled(b);
		to.setEnabled(b);
		keys.setEnabled(b);
		send.setEnabled(b);
		reset.setEnabled(b);
	}
	
	@Override
	public boolean isEnabled() {
		return type.isEnabled();
	}
	
	@Override
	public void update(Status s) {
		if (s.attached != isEnabled()) {
			setEnabled(s.attached);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == type) {
			ChatType selected =
				(ChatType) type.getSelectedItem();
			gui.sm.set("window.chattype", selected.name());
			boolean test = selected.equals(ChatType.WHISPER);
			to.setVisible(test);
			this.revalidate();
		} else if (source == send || source == keys) {
			if (!isEnabled()) return;
			
			String keysText = keys.getText().trim();
			
			if (keysText.isEmpty()) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
						
			ChatType t = (ChatType) type.getSelectedItem();
			
			if (ChatType.RAW != t && keysText.startsWith("/")) {
				log.fine("Preventing non-raw chat that started with a slash");
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			
			if (ChatType.RAW == t && !keysText.startsWith("/")) {
				log.fine("Preventing raw chat that didn't start with a slash");
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			
			setEnabled(false);
			StringBuilder sb = new StringBuilder();
			
			switch (t) {
				case RAW:
					break;
				
				default:
					sb.append("#13#"); // press enter first
			}
			
			sb.append(t.getSlashCommand());
			
			if (t == ChatType.WHISPER) {
				if (to.getText().trim().equals("")) {
					setEnabled(true);
					return;
				}
				
				sb.append(to.getText());
				sb.append(' ');
			}
			
			sb.append(keysText);
			
			switch (t) {
				case RAW: break;
				default:
					sb.append("#13#");
			}

			String keys = sb.toString();
			log.fine("Queuing keys: " + keys);
			gui.sm.cmd.add(Command.getChatCommand(keys));
			
			resetFields();
			setEnabled(true);
			this.keys.requestFocusInWindow();
		} else if (source == reset) {
			resetFields();
			this.keys.requestFocusInWindow();
		}
	}
}
