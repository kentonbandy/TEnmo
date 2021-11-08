package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.*;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
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

    @Override
    public List<TransferHistory> getTransfersForUser(String user) {
        int accountId = getUserAccountIdByUsername(user);
        String sql = "select transfer_id, username, amount from transfers join accounts on account_from = account_id " +
                "join users using(user_id) " +
                "join transfer_statuses using(transfer_status_id) " +
                "where account_to = ? and transfer_status_desc = 'Approved';";
        SqlRowSet fromRowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        sql = "select transfer_id, username, amount from transfers join accounts on account_to = account_id " +
                "join users using(user_id) " +
                "join transfer_statuses using(transfer_status_id) " +
                "where account_from = ? and transfer_status_desc = 'Approved';";
        SqlRowSet toRowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        List<TransferHistory> transfers = new ArrayList<>();
        while (fromRowSet.next()) {
            TransferHistory t = new TransferHistory();
            t.setTransferId(fromRowSet.getInt("transfer_id"));
            t.setFrom(true);
            t.setUsername(fromRowSet.getString("username"));
            t.setAmount(fromRowSet.getDouble("amount"));
            transfers.add(t);
        }
        while (toRowSet.next()) {
            TransferHistory t = new TransferHistory();
            t.setTransferId(toRowSet.getInt("transfer_id"));
            t.setUsername(toRowSet.getString("username"));
            t.setFrom(false);
            t.setAmount(toRowSet.getDouble("amount"));
            transfers.add(t);
        }
        return transfers;
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "select user_id, username from users";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
        List<User> users = new ArrayList<>();
        while (rowSet.next()) {
            User user = new User();
            user.setId(rowSet.getLong("user_id"));
            user.setUsername(rowSet.getString("username"));
            users.add(user);
        }
        return users;
    }

    @Override
    public TransferDetails getTransferDetails(int id) throws NoSuchTransferIdException, NoSuchUserException {

        validateTransferId(id);

        String sql = "select transfer_id, account_from, account_to, transfer_type_desc, transfer_status_desc, amount " +
                "from transfers join transfer_types using(transfer_type_id) join transfer_statuses " +
                "using(transfer_status_id) where transfer_id = ?;";
        TransferDetails transfer = new TransferDetails();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (rowSet.next()) {
            transfer.setTransferId(rowSet.getInt("transfer_id"));
            transfer.setFrom(getUsernameFromAccountId(rowSet.getInt("account_from")));
            transfer.setTo(getUsernameFromAccountId(rowSet.getInt("account_to")));
            transfer.setType(rowSet.getString("transfer_type_desc"));
            transfer.setStatus(rowSet.getString("transfer_status_desc"));
            transfer.setAmount(rowSet.getDouble("amount"));
        }
        return transfer;
    }

    @Override
    public int createRequest(TransferPayment request, String username) throws NoSuchUserException, IllegalAccessError {

        // verification
        if (getUserId(username) != request.getToUserId()) throw new IllegalAccessError();
        validateUserId(request.getFromUserId());

        // database insert
        double amount = request.getAmount();
        int fromAccountId = getUserAccountIdByUserId(request.getFromUserId());
        int toAccountId = getUserAccountIdByUserId(request.getToUserId());

        String sql = "insert into transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "values (?,?,?,?,?) returning transfer_id;";
        int transferId = jdbcTemplate.queryForObject(sql, Integer.class,
                getTransferTypeId("Request"),
                getTransferStatusId("Pending"),
                fromAccountId,
                toAccountId,
                amount);

        return transferId;
    }

    @Override
    public List<TransferHistory> getPendingTransfersForUser(String username) {
        String sql = "select transfer_id, to_user, amount from transfers " +
                "join (select account_id as account_to, username as to_user from users join accounts using(user_id)) as to_table using(account_to) " +
                "join (select account_id as account_from, username as from_user from users join accounts using(user_id)) as from_table using(account_from) " +
                "join transfer_statuses using(transfer_status_id) " +
                "where from_user = ? and transfer_status_desc = 'Pending';";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        List<TransferHistory> pending = new ArrayList<>();
        while (rowSet.next()) {
            TransferHistory t = new TransferHistory();
            t.setTransferId(rowSet.getInt("transfer_id"));
            t.setUsername(rowSet.getString("to_user"));
            t.setAmount(rowSet.getDouble("amount"));
            t.setFrom(false);
            pending.add(t);
        }
        return pending;
    }

    @Override
    public int requestResponse(String username, int transferId, boolean isApproved)
            throws NoSuchTransferIdException, InsufficientFundsException, NotPendingException {

        validateTransferId(transferId);
        validateIsPending(transferId);

        if (!isApproved) return rejectTransfer(transferId);
        else {
            double userBalance = getBalanceByUsername(username);
            if (userBalance < getAmountByTransferId(transferId)) throw new InsufficientFundsException();

            String beginTransaction = "begin transaction";
            String updateTransfers = "update transfers set transfer_status_id = " +
                    "(select transfer_status_id from transfer_statuses where transfer_status_desc = 'Approved') " +
                    "where transfer_id = ?;";
            String updateFrom = "update accounts set balance = balance - (select amount from transfers where transfer_id = ?) " +
                    "where account_id = (select account_from from transfers where transfer_id = ?);";
            String updateTo = "update accounts set balance = balance + (select amount from transfers where transfer_id = ?) " +
                    "where account_id = (select account_to from transfers where transfer_id = ?);";
            String commitTransaction = "commit;";

            jdbcTemplate.execute(beginTransaction);
            jdbcTemplate.update(updateTransfers, transferId);
            jdbcTemplate.update(updateFrom, transferId, transferId);
            jdbcTemplate.update(updateTo, transferId, transferId);
            jdbcTemplate.execute(commitTransaction);
        }

        return transferId;
    }

    private int rejectTransfer(int id) {
        String sql = "update transfers set transfer_status_id = " +
                "(select transfer_status_id from transfer_statuses where transfer_status_desc = 'Rejected') " +
                "where transfer_id = ?;";
        jdbcTemplate.update(sql, id);
        return id;
    }


    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public int createTransfer(TransferPayment transfer, String username) throws InsufficientFundsException, IllegalAccessError, NoSuchUserException {

        if (getUserId(username) != transfer.getFromUserId()) throw new IllegalAccessError();
        validateUserId(transfer.getToUserId());
        double balance = getBalanceByUserId(transfer.getFromUserId());
        if (balance < transfer.getAmount()) throw new InsufficientFundsException();

        double amount = transfer.getAmount();
        int from = getUserAccountIdByUserId(transfer.getFromUserId());
        int to = getUserAccountIdByUserId(transfer.getToUserId());
        String sql = "begin transaction;";
        jdbcTemplate.execute(sql);
        sql = "insert into transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "values (?,?,?,?,?) returning transfer_id;";
        int transferId = jdbcTemplate.queryForObject(sql, Integer.class,
                getTransferTypeId("Send"),
                getTransferStatusId("Approved"),
                from,
                to,
                amount);
        sql = "update accounts set balance = balance - ? where account_id = ?;";
        jdbcTemplate.update(sql, amount, from);
        sql = "update accounts set balance = balance + ? where account_id = ?;";
        jdbcTemplate.update(sql, amount, to);
        sql = "commit;";
        jdbcTemplate.execute(sql);

        return transferId;
    }

    private int getTransferStatusId(String status) {
        String sql = "select transfer_status_id from transfer_statuses where transfer_status_desc = ?;";
        return jdbcTemplate.queryForObject(sql,Integer.class, status);
    }

    private int getTransferTypeId(String type) {
        String sql = "select transfer_type_id from transfer_types where transfer_type_desc = ?;";
            return jdbcTemplate.queryForObject(sql,Integer.class, type);
    }

    private int getUserAccountIdByUsername(String username) {
        String sql = "select account_id from accounts join users using(user_id) where username = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, username);
    }

    private int getUserAccountIdByUserId(int id) {
        String sql = "select account_id from accounts where user_id = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, id);
    }

    private String getUsernameFromAccountId(int id) throws NoSuchUserException {
        String sql = "select username from users join accounts using(user_id) where account_id = ?;";
        String username = jdbcTemplate.queryForObject(sql, String.class, id);
        if (username == null || username.isEmpty()) throw new NoSuchUserException();
        return username;
    }

    private int getUserId(String username) throws NoSuchUserException {
        String sql = "select user_id from users where username = ?;";
        Integer userId = jdbcTemplate.queryForObject(sql, Integer.class, username);
        if (userId == null) throw new NoSuchUserException();
        return userId;
    }

    @Override
    public double getBalanceByUsername(String username) {
        String sql = "select balance from accounts join users using(user_id) where username = ?;";
        Double balance = jdbcTemplate.queryForObject(sql, Double.class, username);
        return balance == null ? 0 : balance;
    }

    @Override
    public double getBalanceByUserId(int id) {
        String sql = "select balance from accounts where user_id = ?;";
        return jdbcTemplate.queryForObject(sql, Double.class, id);
    }

    @Override
    public double getBalanceByAccountId(int id) {
        String sql = "select balance from accounts where account_id = ?";
        return jdbcTemplate.queryForObject(sql, Double.class, id);
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

    private double getAmountByTransferId(int id) {
        String sql = "select amount from transfers where transfer_id = ?;";
        return jdbcTemplate.queryForObject(sql, Double.class, id);
    }

    private void validateTransferId(int id) throws NoSuchTransferIdException {
        String sql = "select count(*) from transfers join transfer_statuses using(transfer_status_id) " +
                "where transfer_id = ?;";
        Integer i = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (i == null || i < 1) throw new NoSuchTransferIdException();
    }

    private void validateUserId(int id) throws NoSuchUserException {
        String sql = "select count(*) from users where user_id = ?";
        Integer i = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (i == null || i < 1) throw new NoSuchUserException();
    }

    private void validateIsPending(int id) throws NotPendingException {
        String sql = "select count(*) from transfers join transfer_statuses using(transfer_status_id) " +
                "where transfer_id = ? and transfer_status_desc = 'Pending';";
        Integer i = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (i == null || i < 1) throw new NotPendingException();
    }

}
