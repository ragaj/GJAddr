package cz.vutbr.fit.gja.gjaddr.gui;

import cz.vutbr.fit.gja.gjaddr.persistancelayer.Contact;
import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
import cz.vutbr.fit.gja.gjaddr.persistancelayer.Group;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.slf4j.LoggerFactory;

/**
 * Main application window.
 *
 * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
 */
public class MainWindow extends JFrame implements ActionListener, DocumentListener {

  static final long serialVersionUID = 0;
  /**
   * Database instance.
   */
  private final Database db = Database.getInstance();
  /**
   * Close menu item.
   */
  private JMenuItem menuItemClose;
  /**
   * Search field.
   */
  private JTextField searchField;
  /**
   * Contacts panel.
   */
  private ContactsPanel contactsPanel;
  /**
   * User detail panel
   */
  private DetailPanel detailPanel;
  /**
   * Status bar.
   */
  private StatusBar statusBar;
  /**
   * Root group name constant.
   */
  static final String ROOT_GROUP = "My contacts";
  /**
   * Constant for default tooltip string for search field.
   */
  private final String SEARCH_BUTTON_TOOLTIP = "filter contacts…";
  /**
   * Holds all possible user actions.
   */
  public UserActions actions = new UserActions(this);

  DetailPanel getDetailPanel() {
    return detailPanel;
  }

  /**
   * Creates the main window.
   */
  public MainWindow() {
    super("GJAddr");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      System.err.println(e);
    }

    setIconImage(new ImageIcon(getClass().getResource("/res/icon.png")).getImage());
    setJMenuBar(this.createMenu());
    final JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    toolbar.add(actions.actionNewContact);
    toolbar.add(actions.actionEditContact);
    toolbar.add(actions.actionDeleteContact);
    toolbar.addSeparator();
    toolbar.add(actions.actionImport);
    toolbar.add(actions.actionExport);
    toolbar.addSeparator();
    toolbar.add(actions.actionPreferences);
    toolbar.addSeparator();

    final JLabel findLabel = new JLabel(new ImageIcon(getClass().getResource("/res/find.png")));
    findLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    toolbar.add(findLabel);

    JPanel wrapper = new JPanel(new GridLayout());
    searchField = new JTextField();
    searchField.setForeground(Color.GRAY);
    searchField.setText(SEARCH_BUTTON_TOOLTIP);

    Font font = new Font(searchField.getFont().getFontName(), Font.BOLD, 11);
    searchField.setFont(font);
    searchField.setToolTipText("Contacts filter");

    searchField.getDocument().addDocumentListener(this);
    searchField.addFocusListener(new SearchFieldListener());
    wrapper.add(searchField);
    
    toolbar.add(wrapper);
    add(toolbar, BorderLayout.NORTH);

    add(new GroupsPanel(this, new GroupSelectionListener()), BorderLayout.WEST);
    contactsPanel = new ContactsPanel(this, new ContactSelectionListener());
    contactsPanel.setMinimumSize(new Dimension(300, 300));
    detailPanel = new DetailPanel();
    detailPanel.setMinimumSize(new Dimension(350, 300));
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contactsPanel, detailPanel);
    splitPane.setBorder(null);
    splitPane.setResizeWeight(1);
    splitPane.setContinuousLayout(true);
    splitPane.setPreferredSize(new Dimension(800, 500));
    add(splitPane);

    this.statusBar = new StatusBar();
    add(this.statusBar, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(null);

    // open window with notifications
    List<Contact> contactsWithEvent = this.db.getContactsWithEvent();
    if (contactsWithEvent.size() > 0) {
      LoggerFactory.getLogger(this.getClass()).info("Opening notifications window.");
      new NotificationsWindow(contactsWithEvent);
    }
  }

  /**
   * Create and return application menu.
   *
   * @return MenuBar
   */
  private JMenuBar createMenu() {
    final JMenuBar menuBar = new JMenuBar();
    final JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    menuBar.add(fileMenu);

    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);
    menuBar.add(helpMenu);
    fileMenu.add(this.actions.actionNewContact);
    fileMenu.add(this.actions.actionNewGroup);
    fileMenu.addSeparator();
    fileMenu.add(this.actions.actionImport);
    fileMenu.add(this.actions.actionExport);
    fileMenu.addSeparator();
    fileMenu.add(this.actions.actionPreferences);
    fileMenu.addSeparator();

    menuItemClose = new JMenuItem("Quit", KeyEvent.VK_Q);
    menuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
    menuItemClose.setIcon(new ImageIcon(getClass().getResource("/res/quit.png")));
    menuItemClose.addActionListener(this);
    fileMenu.add(this.menuItemClose);

    helpMenu.add(this.actions.actionHelp);
    helpMenu.add(this.actions.actionAbout);

    return menuBar;
  }

  /**
   * Currently not user - only for complete interface implementation.
   */
  @Override
  public void changedUpdate(DocumentEvent e) {
  }

  /**
   * Use for filtering contacts table.
   */
  @Override
  public void removeUpdate(DocumentEvent e) {
    this.filterContacts();
  }

  /**
   * Use for filtering contacts table.
   */
  @Override
  public void insertUpdate(DocumentEvent e) {
    this.filterContacts();
  }

  /**
   * Filter contacts.
   */
  private void filterContacts() {
    String text = searchField.getText();
    if (!text.equals(SEARCH_BUTTON_TOOLTIP)) {
      contactsPanel.filter(text);
    }
  }

  /**
   * Method binding functionality to close window.
   *
   * @param e
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == menuItemClose) {
      dispose();
    }
  }

  /**
   * Enable or disable context menu and toolbar action for group.
   */
  void handleGroupActionsVisibility() {
    if (isSelectRootGroup()) {
      this.actions.actionDeleteGroup.setEnabled(false);
      this.actions.actionRenameGroup.setEnabled(false);
      this.actions.actionManageGroup.setEnabled(false);
    } else {
      this.actions.actionDeleteGroup.setEnabled(true);
      this.actions.actionRenameGroup.setEnabled(true);
      this.actions.actionManageGroup.setEnabled(true);
    }
  }

  /**
   * Enable or disable context menu and toolbar action for contact.
   */
  void handleContactActionsVisibility() {
    if (ContactsPanel.getSelectedContact() == null) {
      this.actions.actionDeleteContact.setEnabled(false);
      this.actions.actionEditContact.setEnabled(false);
      this.actions.actionManageContactGroups.setEnabled(false);
    } else {
      this.actions.actionDeleteContact.setEnabled(true);
      this.actions.actionEditContact.setEnabled(true);
      this.actions.actionManageContactGroups.setEnabled(true);
    }
  }

  /**
   * Check if is selected group root group.
   *
   * @return
   */
  private boolean isSelectRootGroup() {
    for (Group g : GroupsPanel.getSelectedGroups()) {
      if (g.getName().equals(ROOT_GROUP)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Listener class for groups list selection
   */
  private class GroupSelectionListener implements ListSelectionListener {

    @Override
    public void valueChanged(ListSelectionEvent e) {
      if (!e.getValueIsAdjusting()) {	//React only on final choice
        ContactsPanel.fillTable(false, false);
        handleGroupActionsVisibility();
      }
    }
  }

  /**
   * Listener class for contacts table selection
   */
  private class ContactSelectionListener implements ListSelectionListener {

    @Override
    public void valueChanged(ListSelectionEvent e) {
      //React only on final choice
      if (!e.getValueIsAdjusting()) {
        detailPanel.show(ContactsPanel.getSelectedContact());
        detailPanel.setVisible(true);
        handleContactActionsVisibility();
      }
    }
  }

  /**
   * Search field listener, implement focus actions for tooltip.
   */
  private class SearchFieldListener implements FocusListener {

    @Override
    public void focusGained(FocusEvent e) {
      searchField.setForeground(Color.BLACK);
      String text = searchField.getText();

      if (text.equals(SEARCH_BUTTON_TOOLTIP)) {
        searchField.setText("");
      }
    }

    @Override
    public void focusLost(FocusEvent e) {
      searchField.setForeground(Color.GRAY);
      String text = searchField.getText();

      if (text.equals(SEARCH_BUTTON_TOOLTIP) || text.isEmpty()) {
        searchField.setText(SEARCH_BUTTON_TOOLTIP);
      }
    }
  }
}
