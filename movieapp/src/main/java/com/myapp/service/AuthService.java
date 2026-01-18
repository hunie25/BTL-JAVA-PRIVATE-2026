package com.myapp.service;

import com.myapp.dao.UserDAO;
import com.myapp.exception.RegisterException;
import com.myapp.model.User;

import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    private final UserDAO userDAO = new UserDAO();

    public boolean login(String username, String password) {
        User user = userDAO.findByUsername(username);

        if (user == null) {
            return false;
        }

        return user.getPassword().equals(password);
    }

    public void register(String username, String password, String confirmPassword)
            throws RegisterException {

        if (username == null || username.isBlank()
                || password == null || confirmPassword == null) {
            throw new RegisterException("Vui lòng nhập đầy đủ thông tin");
        }

        if (userDAO.existsByUsername(username)) {
            throw new RegisterException("Tên đăng nhập đã tồn tại");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RegisterException("Mật khẩu không đủ mạnh");
        }

        if (!password.equals(confirmPassword)) {
            throw new RegisterException("Mật khẩu nhập lại không khớp");
        }

        User user = new User(username, password); // nên hash
        userDAO.insert(user);
    }

}
