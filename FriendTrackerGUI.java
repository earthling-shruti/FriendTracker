package org.semweb.assign5;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.IOException;

/* FriendTrackerGUI.java requires no other files. */
public class FriendTrackerGUI extends JPanel
                      implements ListSelectionListener {
    private JList list;
    private DefaultListModel listModel;

    private JList events;
    private DefaultListModel eventModel;

    private JList profile;
    private DefaultListModel profileModel;

    private static final String addFriendString = "Add";
    private static final String removeFriendString = "Remove";
    private JButton removeButton;
    private JTextField friendName;
    private static String []friendslist;
    private static String []eventslist;
    private static String []profilelist;

    public FriendTrackerGUI() {
        super(new BorderLayout());

        listModel = new DefaultListModel();
        eventModel = new DefaultListModel();
        profileModel = new DefaultListModel();

        int i = 0;
        while(friendslist[i] != null){
        	listModel.addElement(friendslist[i]);
        	i++;
        }
        i= 0;
        while(eventslist[i]!=null){
        	eventModel.addElement(eventslist[i]);
        	i++;
        }
        i=0;
        while(profilelist[i]!=null){
        	profileModel.addElement(profilelist[i]);
        	i++;
        }

        //Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(20);

        events = new JList(eventModel);
        events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        events.setSelectedIndex(0);
        events.setVisibleRowCount(20);

        profile = new JList(profileModel);
        profile.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profile.setSelectedIndex(0);
        profile.setVisibleRowCount(6);

        JScrollPane listScrollPane = new JScrollPane(list);
        JScrollPane eventScrollPane = new JScrollPane(events);
        JScrollPane profileScrollPane = new JScrollPane(profile);

        JButton addButton = new JButton(addFriendString);
        AddButtonListener addFriendListener = new AddButtonListener(addButton);
        addButton.setActionCommand(addFriendString);
        addButton.addActionListener(addFriendListener);
        addButton.setEnabled(false);

        removeButton = new JButton(removeFriendString);
        removeButton.setActionCommand(removeFriendString);
        removeButton.addActionListener(new RemoveButtonListener());

        friendName = new JTextField(10);
        friendName.addActionListener(addFriendListener);
        friendName.getDocument().addDocumentListener(addFriendListener);
        String name = listModel.getElementAt(
                              list.getSelectedIndex()).toString();

        //Create a panel that uses BoxLayout.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,
                                           BoxLayout.LINE_AXIS));
        buttonPane.add(removeButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(friendName);
        buttonPane.add(addButton);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(listScrollPane, BorderLayout.CENTER);
        add(eventScrollPane, BorderLayout.EAST);
        add(profileScrollPane, BorderLayout.PAGE_START);
        add(buttonPane, BorderLayout.PAGE_END);
    }

    class RemoveButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            listModel.remove(index);

            int size = listModel.getSize();

            if (size == 0) { //Nobody's left, disable firing.
                removeButton.setEnabled(false);

            } else { //Select an index.
                if (index == listModel.getSize()) {
                    //removed item in last position
                    index--;
                }

                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }
        }
    }

    //This listener is shared by the text field and the addFriend button.
    class AddButtonListener implements ActionListener, DocumentListener {
        private boolean alreadyEnabled = false;
        private JButton button;

        public AddButtonListener(JButton button) {
            this.button = button;
        }

        //Required by ActionListener.
        public void actionPerformed(ActionEvent e) {
            String name = friendName.getText();

            //User didn't type in a unique name...
            if (name.equals("") || alreadyInList(name)) {
                Toolkit.getDefaultToolkit().beep();
                friendName.requestFocusInWindow();
                friendName.selectAll();
                return;
            }

            int index = list.getSelectedIndex(); //get selected index
            if (index == -1) { //no selection, so insert at beginning
                index = 0;
            } else {           //add after the selected item
                index++;
            }

            listModel.insertElementAt(friendName.getText(), index);

            //Reset the text field.
            friendName.requestFocusInWindow();
            friendName.setText("");

            //Select the new item and make it visible.
            list.setSelectedIndex(index);
            list.ensureIndexIsVisible(index);
        }

        protected boolean alreadyInList(String name) {
            return listModel.contains(name);
        }

        //Required by DocumentListener.
        public void insertUpdate(DocumentEvent e) {
            enableButton();
        }

        //Required by DocumentListener.
        public void removeUpdate(DocumentEvent e) {
            handleEmptyTextField(e);
        }

        //Required by DocumentListener.
        public void changedUpdate(DocumentEvent e) {
            if (!handleEmptyTextField(e)) {
                enableButton();
            }
        }

        private void enableButton() {
            if (!alreadyEnabled) {
                button.setEnabled(true);
            }
        }

        private boolean handleEmptyTextField(DocumentEvent e) {
            if (e.getDocument().getLength() <= 0) {
                button.setEnabled(false);
                alreadyEnabled = false;
                return true;
            }
            return false;
        }
    }

    //This method is required by ListSelectionListener.
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {

            if (list.getSelectedIndex() == -1) {
            //No selection, disable removeFriend button.
                removeButton.setEnabled(false);

            } else {
            //Selection, enable the removeFriend button.
                removeButton.setEnabled(true);
            }
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("FriendTrackerGUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new FriendTrackerGUI();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
    	FriendTracker hello = new FriendTracker();
		hello.collectFbGmailEventsData();
		friendslist = hello.getFriends();
		eventslist = hello.getEvents();
		profilelist = hello.getMyProfileInfo();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
