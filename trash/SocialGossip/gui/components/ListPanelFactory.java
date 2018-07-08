package gui.components;

import gui.panels.CreateRoomPanel;
import gui.panels.LookupPanel;
import state.Chat;
import state.Friend;
import state.User;
import state.Room;
import connections.Json;
import gui.constants.Dimensions;
import gui.Utils;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Factory for the friends and rooms panels.
 * Creates the 2 panels in the Main Panel.
 */
public class ListPanelFactory {

    private static final MouseListener startChatListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent mouseEvent) {
            JList jlist = (JList) mouseEvent.getSource();
            if (mouseEvent.getClickCount() == 2) {
                int index = jlist.locationToIndex(mouseEvent.getPoint());
                if (index >= 0) {
                    String name = jlist.getModel().getElementAt(index).toString();
                    Chat chat = User.getFriend(name);
                    if (chat == null) chat = User.getRoom(name);
                    if (chat == null) return;
                    chat.createWindow();
                }
            }
        }
    };

    // Double click listener on other room to join
    private static final MouseListener addRoomListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent mouseEvent) {
            JList jlist = (JList) mouseEvent.getSource();
            if (mouseEvent.getClickCount() == 2) {
                int index = jlist.locationToIndex(mouseEvent.getPoint());
                if (index >= 0) {
                    String name = jlist.getModel().getElementAt(index).toString();
                    if (Json.addMe(name))
                        User.setRoomStatus(name, true);
                }
            }
        }
    };

    /**
     * Helper: assemble a panel with the elements passed as parameters.
     * @param firstPanel the upper panel: online friends or joined chats
     * @param secondPanel the lowest panel: offline friends or other chats (created by other users and not joined)
     * @param button the bottom button: search an user or create a new room
     * @return the assembled JPanel
     */
    private static JPanel preparePanel(JPanel firstPanel, JPanel secondPanel, JButton button) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(Dimensions.PADDING_BORDER);
        panel.add(firstPanel);
        panel.add(secondPanel);
        panel.add(button);
        return panel;
    }

    /**
     * Build the left panel in MainPanel
     * @param friends the friends collection that will be parsed and divided into 2 list: online and offline
     * @return the JPanel with the online and offline lists with the mouse listener and the search user button
     */
    public static JPanel newFriendsPane(Collection<Friend> friends) {
        List<String> online = new ArrayList<>();
        List<String> offline = new ArrayList<>();
        for (Friend friend : friends) {
            if(friend.isOnline()) online.add(friend.getUsername());
            else offline.add(friend.getUsername());
        }
        online.sort(String::compareToIgnoreCase);
        offline.sort(String::compareToIgnoreCase);

        ListPanel onlinePanel = new ListPanel(
                "Online friends: double-click to open chat",
                online.toArray(new String[online.size()]),
                startChatListener
        );

        ListPanel offlinePanel = new ListPanel(
                "Offline friends",
                offline.toArray(new String[offline.size()]),
                null
        );

        Action buttonAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel lookupPanel = new LookupPanel();
                Utils.createFixedWindow("Search for friend", lookupPanel, false, false);
            }
        };

        JButton button = new JButton("Add friend");
        button.addActionListener(buttonAction);

        return preparePanel(onlinePanel, offlinePanel, button);
    }

    /**
     * Build the right panel in MainPanel
     * @param rooms the rooms collection that will be parsed and divided into 2 list: my rooms and other rooms
     * @return the JPanel with the my rooms and other rooms lists with the mouse listeners and the new room button
     */
    public static JPanel newRoomsPane(Collection<Room> rooms) {
        List<String> subscriptions = new ArrayList<>();
        List<String> others = new ArrayList<>();
        for (Room room : rooms) {
            if(room.isSubscribed()) subscriptions.add(room.getName());
            else others.add(room.getName());
        }
        subscriptions.sort(String::compareToIgnoreCase);
        others.sort(String::compareToIgnoreCase);

        ListPanel subscriptionsPanel = new ListPanel(
                "My rooms: double-click to open chat",
                subscriptions.toArray(new String[subscriptions.size()]),
                startChatListener
        );

        ListPanel othersPanel = new ListPanel(
                "Other rooms",
                others.toArray(new String[others.size()]),
                addRoomListener
        );

        Action buttonAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel createRoomPanel = new CreateRoomPanel();
                Utils.createFixedWindow("Create new room", createRoomPanel, false, false);
            }
        };

        JButton button = new JButton("Create room");
        button.addActionListener(buttonAction);

        return preparePanel(subscriptionsPanel, othersPanel, button);
    }
}