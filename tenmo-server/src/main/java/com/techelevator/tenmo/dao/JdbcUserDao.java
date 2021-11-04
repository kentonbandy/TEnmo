package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserDao implements UserDao {

    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");
    private JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int findIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE username ILIKE ?;";
        Integer id = jdbcTemplate.queryForObject(sql, Integer.class, username);
        if (id != null) {
            return id;
        } else {
            return -1;
    }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash FROM users;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
        }
        return users;
    }

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        String sql = "SELECT user_id, username, password_hash FROM users WHERE username ILIKE ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        if (rowSet.next()){
            return mapRowToUser(rowSet);
            }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password) {

        // create user
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING user_id;";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        Integer newUserId;
        try {
            newUserId = jdbcTemplate.queryForObject(sql, Integer.class, username, password_hash);
        } catch (DataAccessException e) {
            return false;
                }

        // create account
        sql = "INSERT INTO accounts (user_id, balance) values(?, ?);";
        try {
            jdbcTemplate.update(sql, newUserId, STARTING_BALANCE);
        } catch (DataAccessException e) {
            return false;
        }

        return true;
    }

    public List<Transfer> getTransfersForUser(String user) {
        int accountId = getUserAccountId(user);
        String sql = "select transfer_id, username, amount from transfers join accounts on account_from = account_id " +
                "join users using(user_id) where account_to = ?;";
        SqlRowSet fromRowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        sql = "select transfer_id, username, amount from transfers join accounts on account_to = account_id " +
                "join users using(user_id) where account_from = ?;";
        SqlRowSet toRowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        List<Transfer> transfers = new ArrayList<>();
        while (fromRowSet.next()) {
            Transfer t = new Transfer();
            t.setId(fromRowSet.getInt("transfer_id"));
            t.setFrom(fromRowSet.getString("username"));
            t.setTo(user);
            t.setAmount(fromRowSet.getDouble("amount"));
            transfers.add(t);
        }
        while (toRowSet.next()) {
            Transfer t = new Transfer();
            t.setId(toRowSet.getInt("transfer_id"));
            t.setTo(toRowSet.getString("username"));
            t.setFrom(user);
            t.setAmount(toRowSet.getDouble("amount"));
            transfers.add(t);
        }
        return transfers;
    }

    @ResponseStatus(HttpStatus.CREATED)
    public void createTransfer(Transfer transfer) throws InsufficientFundsException {
        // set variables from transfer object
        int typeId = getTransferTypeId("Send");
        int statusId = getTransferStatusId("Approved");
        int to = getUserAccountId(transfer.getTo());
        int from = getUserAccountId(transfer.getFrom());
        double fromBalance = getBalanceByUsername(transfer.getFrom());
        double amount = transfer.getAmount();

        // verification
        if (amount > fromBalance) {
            throw new InsufficientFundsException();
        }

        // perform database changes
        String sql = "insert into transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "values (?,?,?,?,?);";
        jdbcTemplate.update(sql, typeId, statusId, from, to, amount);
        sql = "update accounts set balance = balance - ? where account_id = ?;";
        jdbcTemplate.update(sql, amount, from);
        sql = "update accounts set balance = balance + ? where account_id = ?;";
        jdbcTemplate.update(sql, amount, to);
    }

    private int getTransferStatusId(String status) {
        String sql = "select transfer_status_id from transfer_statuses where transfer_status_desc = ?;";
        return jdbcTemplate.queryForObject(sql,Integer.class, status);
    }

    private int getTransferTypeId(String type) {
        String sql = "select transfer_type_id from transfer_types where transfer_type_desc = ?;";
            return jdbcTemplate.queryForObject(sql,Integer.class, type);
    }

    private int getUserAccountId(String username) {
        String sql = "select account_id from accounts join users using(user_id) where username = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, username);
    }

    @Override
    public double getBalanceByUsername(String username) {
        String sql = "select balance from accounts join users using(user_id) where username = ?;";
        Double balance = jdbcTemplate.queryForObject(sql, Double.class, username);
        return balance == null ? 0 : balance;
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }
}
