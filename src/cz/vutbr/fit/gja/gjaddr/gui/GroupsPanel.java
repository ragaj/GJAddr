package cz.vutbr.fit.gja.gjaddr.gui;

import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
import cz.vutbr.fit.gja.gjaddr.persistancelayer.Group;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;

/**
 * Panel with groups
 *
 * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
 */
class GroupsPanel extends JPanel implements ActionListener, KeyListener {
	static final long serialVersionUID = 0;
	private static final Database db = new Database();
	private static final DefaultListModel listModel = new DefaultListModel();
	private static final JList list = new JList(listModel);

	/**
	 * Constructor
	 *
	 * @param listSelectionListener Listener to handle actions outside goups panel
	 */
	public GroupsPanel(ListSelectionListener listSelectionListener) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel("Groups");
		label.setAlignmentX(CENTER_ALIGNMENT);
		add(label);
		fillList();
		list.setSelectedIndex(0);
		list.addListSelectionListener(listSelectionListener);
		list.addKeyListener(this);
		JScrollPane listScrollPane = new JScrollPane(list);
		add(listScrollPane);
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
		JButton add = new JButton(new ImageIcon(getClass().getResource("/res/plus_g.png"), "+"));
		add.setActionCommand("addGroup");
		add.addActionListener(this);
		buttons.add(add);
		JButton remove = new JButton(new ImageIcon(getClass().getResource("/res/minus_g.png"), "-"));
		remove.setActionCommand("removeGroup");
		remove.addActionListener(this);
		buttons.add(remove);
		add(buttons);
	}

	/**
	 * Fills list with groups from db
	 */
	private void fillList() {
		listModel.clear();
		listModel.addElement(new Group("My Contacts"));
		for (Group g : db.getAllGroups()) {
			listModel.addElement(g);
		}
		list.setSelectedIndex(0);
	}

	/**
	 * Listener for actions (add/remove groups)
	 *
	 * @param e Action
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("addGroup".equals(e.getActionCommand())) {
			addGroup();
		} else if ("removeGroup".equals(e.getActionCommand())) {
			removeGroups();
		}
		fillList();
	}

	/**
	 * Key press listener
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			removeGroups();
			fillList();
		}
	}

	/**
	 * Key release listener
	 */
	@Override
	public void keyReleased(KeyEvent e) {}

	/**
	 * Key typed listener
	 */
	@Override
	public void keyTyped(KeyEvent e) {}

	/**
	 * Function for adding group
	 */
	private void addGroup() {
		//System.out.println("addGroup");
		String name = (String) JOptionPane.showInputDialog(
			this,
			"Group name:",
			"Add group",
			JOptionPane.QUESTION_MESSAGE,
			new ImageIcon(getClass().getResource("/res/plus_g.png"), "+"),
			null,
			""
		);
		if (name != null && !name.isEmpty()) {
			//System.out.println(name);
			db.addNewGroup(name);
		}
	}

	/**
	 * Function for removing group
	 */
	private void removeGroups() {
		Group[] groups = Arrays.copyOf(list.getSelectedValues(), list.getSelectedValues().length, Group[].class);
		//System.out.println("Remove groups: " + Arrays.toString(groups));
		int delete = JOptionPane.showConfirmDialog(
			this,
			"Delete groups " + Arrays.toString(groups) + "?",
			"Delete groups",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			new ImageIcon(getClass().getResource("/res/minus_g.png"), "-")
		);
		//System.out.println(delete);
		if (delete == 0) {
			db.removeGroups(Arrays.asList(groups));
		}
	}
}
