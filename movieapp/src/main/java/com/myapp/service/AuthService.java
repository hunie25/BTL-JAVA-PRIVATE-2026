package com.myapp.service;

import com.myapp.dao.UserDAO;
import com.myapp.exception.RegisterException;
import com.myapp.model.User;

import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final UserDAO userDAO = new UserDAO();
    private final OtpService otpService = new OtpService();

    public User login(String username, String password) throws Exception {
        User user = userDAO.findByUsername(username);

        if (user == null) {
            throw new Exception("Tên đăng nhập không tồn tại!");
        }
        if (!user.getPassword().equals(password)) {
            throw new Exception("Mật khẩu không chính xác!");
        }
        if (!user.isVerified()) {
            throw new Exception("Tài khoản chưa được kích hoạt OTP!");
        }

        return user;
    }

    public void register(String username, String email, String password, String confirmPassword)
            throws RegisterException {

        if (username == null || username.isBlank()
                || email == null || email.isBlank()
                || password == null || confirmPassword == null) {
            throw new RegisterException("Vui lòng nhập đầy đủ thông tin");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RegisterException("Email không hợp lệ");
        }

        if (userDAO.existsByUsername(username)) {
            throw new RegisterException("Tên đăng nhập đã tồn tại");
        }

        if (userDAO.existsByEmail(email)) {
            throw new RegisterException("Email này đã được đăng ký");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RegisterException("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
        }

        if (!password.equals(confirmPassword)) {
            throw new RegisterException("Mật khẩu nhập lại không khớp");
        }

        User newUser = new User(0, username, password, email, true);
        userDAO.insert(newUser);
        try {
            new EmailService().sendWelcome(email, username);
        } catch (Exception e) {
            System.out.println("Không gửi được email chào mừng, nhưng tài khoản đã tạo thành công.");
        }
    }
    public void completeRegistration(String username, String email, String password)
            throws RegisterException {

        try {
            User user = new User(username, password, email);
            user.setVerified(true);
            userDAO.insert(user);
            new EmailService().sendWelcome(email, username);
        } catch (Exception e) {
            throw new RegisterException("Không thể hoàn thành đăng ký: " + e.getMessage());
        }
    }
    public void activateAccount(String email) throws Exception {
        System.out.println("DEBUG: Đang kích hoạt cho email: " + email);
        boolean success = userDAO.updateVerificationStatus(email, true);

        if (!success) {
            throw new Exception("Lỗi: Không tìm thấy email " + email + " trong hệ thống.");
        }
        try {
            new EmailService().sendWelcome(email, "Người dùng");
        } catch (Exception e) {
            System.out.println("Gửi email chào mừng thất bại nhưng tài khoản vẫn được kích hoạt.");
        }
    }
    public boolean isEmailExists(String email) {
        try {
            return userDAO.existsByEmail(email);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void resetPassword(String email, String newPassword) throws Exception {
        if (newPassword == null || newPassword.isBlank()) {
            throw new Exception("Mật khẩu mới không được để trống!");
        }

        boolean success = userDAO.updatePassword(email, newPassword);
        if (!success) {
            throw new Exception("Lỗi: Không thể cập nhật mật khẩu. Email không tồn tại.");
        }
    }
}
