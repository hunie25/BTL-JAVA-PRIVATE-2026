package com.myapp.service;

import com.myapp.dao.UserDAO;
import com.myapp.model.User;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public boolean login(String username, String password) {
        User user = userDAO.findByUsername(username);

        if (user == null) {
            return false;
        }

        // Tạm thời so plain text (sau sẽ hash)
        return user.getPassword().equals(password);
    }
}
