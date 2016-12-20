package org.fit.proxy.jdbc;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyStatementBatcher {
    private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());

    private final ProxyConnection connection;
    private final  StatementConstructorFactory constructorFactory;
    private List<CallableStatement> statements = new LinkedList<>();

    public ProxyStatementBatcher(ProxyConnection connection, StatementConstructorFactory constructorFactory) {
        this.connection = connection;
        this.constructorFactory = constructorFactory;
    }

    public void addBatch(String sql) throws SQLException {
        CallableStatement statement = constructorFactory.createBatchStatement(connection, sql);
        statements.add(statement);
    }

    public void clearBatch() {
        safeClose();

        statements.clear();
    }

    public int[] executeBatch() throws SQLException {
        int[] res = new int[statements.size()];
        int i = 0;

        for (CallableStatement statement : statements) {
            res[i++] = statement.executeUpdate();
        }

        return res;
    }

    public void safeClose() {
        for (CallableStatement statement : statements) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.log(Level.WARNING, "Problem closing batch statement.", e);
            }
        }
    }

}
