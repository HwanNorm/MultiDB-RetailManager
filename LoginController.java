import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginController implements ActionListener {
    private LoginScreen loginScreen;
    private DataAdapter dataAdapter;

    public LoginController(LoginScreen loginScreen, DataAdapter dataAdapter) {
        this.loginScreen = loginScreen;
        this.dataAdapter = dataAdapter;
        this.loginScreen.getBtnLogin().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginScreen.getBtnLogin()) {
            String username = loginScreen.getTxtUserName().getText().trim();
            String password = loginScreen.getTxtPassword().getText().trim();

            System.out.println("Login with username = " + username + " and password = " + password);
            Users user = dataAdapter.loadUser(username, password);

            if (user == null) {
                JOptionPane.showMessageDialog(null, "Invalid username or password!");
            } else {
                Application.getInstance().setCurrentUser(user);
                this.loginScreen.setVisible(false);

                // Direct to appropriate view based on user role
                switch (user.getRole()) {
                    case MANAGER:
                        Application.getInstance().getManagerView().setVisible(true);
                        break;
                    case CASHIER:
                        Application.getInstance().getCashierView().setVisible(true);
                        break;
                    case CUSTOMER:
                        Application.getInstance().getMainScreen().setVisible(true);
                        break;
                }
            }
        }
    }
}