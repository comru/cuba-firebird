package db.firebird

import java.sql.SQLException

class FirebirdDbProperties {
    String caption = "Firebird"
    String id = "firebird"
    String user = "sysdba"
    String password = "masterkey"
    String driver = "org.firebirdsql.jdbc.FBDriver"
    String urlPrefix = "jdbc:firebirdsql://"
    String jdbcDriverDependency = "org.firebirdsql.jdbc:jaybird:2.2.5"

    boolean needDependencyToWrapQuote = false
    boolean deleteTsNeededIndex = true
    int loginTimeout = -1
    String prevDriverVarPattern = "def\\s+firebird\\s*=\\s*'.*?'"
    String prevJdbcDepPattern = "jdbc\\s*?\\(\\s*?firebird\\s*?\\)"
    String prevTestRuntimePattern = "testRuntime\\s*?\\(\\s*?firebird\\s*?\\)"


    String getMasterUrl(String dbName, String connectionParams) {
        return 'jdbc:firebirdsql://'
    }

    String convertDbIdentifierCase(String identifier) {
        return identifier
    }

    boolean checkDbExistException(SQLException e) {
        return 'HY000'.equals(e.SQLState) && e.errorCode == 335544344
    }

    String getDefaultSchema() {
        return null
    }

    String getCurrDriverVar() {
        return "def firebird = '${jdbcDriverDependency}'"
    }

    String getCurrJdbcDep() {
        return "jdbc(firebird)"
    }

    String getCurrTestRuntime() {
        return "testRuntime(firebird)"
    }

    boolean isSuitableUrl(String url) {
        return url?.startsWith('jdbc:firebirdsql:')
    }
}