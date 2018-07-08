package gui.panels;

import gui.components.ListPanelFactory;
import state.Friend;
import state.User;
import state.Room;

import javax.swing.*;
import java.util.Collection;

public class MainPanel extends JPanel {

    private JPanel friendsPane;
    private JPanel roomsPane;

    private void updateFriends(Collection<Friend> friends) {
        friendsPane = ListPanelFactory.newFriendsPane(friends);
        refresh();
    }

    private void updateRooms(Collection<Room> room) {
        roomsPane = ListPanelFactory.newRoomsPane(room);
        refresh();
    }
    
    private void refresh() {
        this.removeAll();
        this.add(friendsPane);
        this.add(roomsPane);
        this.revalidate();
        this.repaint();
    
    }

    public MainPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        friendsPane = ListPanelFactory.newFriendsPane(User.friends());
        roomsPane = ListPanelFactory.newRoomsPane(User.rooms());

        this.add(friendsPane);
        this.add(roomsPane);

        User.addFriendsListener(this::updateFriends);
        User.addChatsListener(this::updateRooms);
    }

}
