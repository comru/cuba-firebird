package db.firebird

import java.sql.DatabaseMetaData

class FirebirdDdlGenerator {
    final Map<String, String> javaTypes = new HashMap<>()
    final Map<String, String> temporalTypes = new HashMap<>()
    final Map<String, List<String>> convertibleTypes = new HashMap<>()
    final Map<String, String> defaultValues = new HashMap<>()
    final List<List<String>> mappingTypesSynonyms = new ArrayList<>()
    final String timeStampType = 'timestamp'
    final Set<String> reservedKeywords = new HashSet<>()
    def api

    FirebirdDdlGenerator() {
        javaTypes.put('Boolean', 'char')
        javaTypes.put('byte[]', 'blob')
        javaTypes.put('Character', 'char')
        javaTypes.put('Date', 'timestamp')
        javaTypes.put('BigDecimal', 'decimal')
        javaTypes.put('Double', 'double precision')
        javaTypes.put('Integer', 'integer')
        javaTypes.put('Long', 'bigint')
        javaTypes.put('String', 'varchar')
        javaTypes.put('UUID', 'varchar(32)')

        mappingTypesSynonyms.add(['char', 'character'])
        mappingTypesSynonyms.add(['blob'])
        mappingTypesSynonyms.add(['timestamp'])
        mappingTypesSynonyms.add(['time'])
        mappingTypesSynonyms.add(['date'])
        mappingTypesSynonyms.add(['decimal', 'numeric'])
        mappingTypesSynonyms.add(['double precision', 'float'])
        mappingTypesSynonyms.add(['integer', 'int', 'smallint'])
        mappingTypesSynonyms.add(['bigint'])
        mappingTypesSynonyms.add(['varchar', 'char varying', 'character varying'])

        temporalTypes.put('DATE', 'date')
        temporalTypes.put('TIME', 'time')
        temporalTypes.put('TIMESTAMP', 'timestamp')

        defaultValues.put('Boolean', 'false')
        defaultValues.put('ByteArray', "''")
        defaultValues.put('Date', 'CURRENT_DATE')
        defaultValues.put('DateTime', 'CURRENT_TIMESTAMP')
        defaultValues.put('Time', 'CURRENT_TIME')
        defaultValues.put('BigDecimal', '0')
        defaultValues.put('Double', '0')
        defaultValues.put('Integer', '0')
        defaultValues.put('Long', '0')
        defaultValues.put('String', "''")
        defaultValues.put('UUID', 'newid()')

        reservedKeywords.addAll(Arrays.asList(
                'ADD', 'ADMIN', 'ALL', 'ALTER', 'AND', 'ANY', 'AS', 'AT', 'AVG', 'BEGIN', 'BETWEEN', 'BIGINT', 'BIT_LENGTH',
'BLOB', 'BOTH', 'BY', 'CASE', 'CAST', 'CHAR', 'CHAR_LENGTH', 'CHARACTER', 'CHARACTER_LENGTH', 'CHECK', 'CLOSE', 'COLLATE',
'COLUMN', 'COMMIT', 'CONNECT', 'CONSTRAINT', 'COUNT', 'CREATE', 'CROSS', 'CURRENT', 'CURRENT_CONNECTION', 'CURRENT_DATE',
'CURRENT_ROLE', 'CURRENT_TIME', 'CURRENT_TIMESTAMP', 'CURRENT_TRANSACTION', 'CURRENT_USER',
'CURSOR', 'DATE', 'DAY', 'DEC', 'DECIMAL', 'DECLARE', 'DEFAULT', 'DELETE', 'DISCONNECT', 'DISTINCT', 'DOUBLE',
'DROP', 'ELSE', 'END', 'ESCAPE', 'EXECUTE', 'EXISTS', 'EXTERNAL', 'EXTRACT', 'FETCH', 'FILTER', 'FLOAT', 'FOR', 'FOREIGN',
'FROM', 'FULL', 'FUNCTION', 'GDSCODE', 'GLOBAL', 'GRANT', 'GROUP', 'HAVING', 'HOUR', 'IN', 'INDEX', 'INNER', 'INSENSITIVE',
'INSERT', 'INT', 'INTEGER', 'INTO', 'IS', 'JOIN', 'LEADING', 'LEFT', 'LIKE', 'LONG', 'LOWER', 'MAX', 'MAXIMUM_SEGMENT', 'MERGE',
'MIN', 'MINUTE', 'MONTH', 'NATIONAL', 'NATURAL', 'NCHAR', 'NO', 'NOT', 'NULL', 'NUMERIC', 'OCTET_LENGTH', 'OF', 'ON', 'ONLY',
'OPEN', 'OR', 'ORDER', 'OUTER', 'PARAMETER', 'PLAN', 'POSITION', 'POST_EVENT', 'PRECISION', 'PRIMARY', 'PROCEDURE', 'REALY',
'RECORD_VERSION', 'RECREATE', 'RECURSIVE', 'REFERENCES', 'RELEASE', 'RETURNING_VALUES', 'RETURNS', 'REVOKE', 'RIGHT', 'ROLLBACK',
'ROW_COUNT', 'ROWS', 'SAVEPOINT', 'SECOND', 'SELECT', 'SENSITIVE', 'SET', 'SIMILAR', 'SMALLINT', 'SOME', 'SQLCODE',
'START', 'SUM', 'TABLE', 'THEN', 'TIME', 'TIMESTAMP', 'TO', 'TRAILING', 'TRIGGER', 'TRIM', 'UNION', 'UNIQUE', 'UPDATE', 'UPPER',
'USER', 'USING', 'VALUE', 'VALUES', 'VARCHAR', 'VARIABLE', 'VARYING', 'VIEW', 'WHEN', 'WHERE', 'WHILE', 'WITH', 'YEAR'))
    }

    GeneratedScript generateDropColumn(String tableName, String column) {
        return new GeneratedScript(code: "alter table ${tableName} drop ${column} ^", constraint: null)
    }

    GeneratedScript generatedDropTable(DatabaseMetaData databaseMetaData, String schema, String tableName) {
        return new GeneratedScript(code: "drop table ${tableName} ^", constraint: null)
    }

    boolean precisionChanged(int currPrecision, int oldPrecision, String oldSqlType) {
        return currPrecision != oldPrecision && !(oldPrecision == 18 && currPrecision == 0)
    }

    String generateRenameColumn(String table, String oldColumn, String newColumn) {
        return "alter table ${table} alter column ${oldColumn} to ${newColumn} ^"
    }

    String generateSequence(String sequenceName, long startValue, long increment) {
        return "create sequence ${sequenceName};\n" +
                "alter sequence ${sequenceName} restart with ${startValue};"
    }

    String sequenceExistsSql(String sequenceName) {
        return "SELECT * FROM RDB\$GENERATORS WHERE RDB\$GENERATOR_NAME = '${sequenceName}'"
    }

    String deleteSequenceSql(String sequenceName) {
        return "drop sequence ${sequenceName}"
    }

    String generateAddColumn(def entity, def attribute, String column) {
        if (attribute.isEmbedded()) {
            return ''
        }
        String attributeColumnStatement
        boolean includeNotNull = attribute.isMandatory() && attribute.isClass()
        if (StringUtils.isBlank(column)) {
            attributeColumnStatement = api.generateAttributeColumn(entity, attribute, includeNotNull)
        } else {
            attributeColumnStatement = api.generateAttributeColumn(entity, attribute, api.columnToUpperCase(column), includeNotNull)
        }
        addScript.append(String.format("alter table %s add column %s ^", entity.getTable(), attributeColumnStatement))
        api.processAddColumnMandatory(addScript, entity, attribute, StringUtils.isBlank(column) ? attribute.getDdlManipulationColumn() : column)
    }

    String generateAlterColumnTypeWithLength(def entity, def attribute, String newType, String columnName) {
        Integer length = api.parseLength(entity, attribute)
        String newLength = (length != null && length != 0) ? "(" + length.toString() + ")" : "";
        return String.format("alter table %s alter column %s %s%s ^",
                entity.getTable(), api.getColumnName(attribute, columnName), newType, newLength)
    }

    String generateAlterColumnDecimal(def entity, def attribute, String newType, String columnName) {
        return "alter table ${entity.getTable()} alter column ${api.getColumnName(attribute, columnName)}" +
                " ${newType}(${api.getDecimalParams(entity, attribute)}) ^"
    }

    String generateAlterColumnType(def entity, def attribute, String newType, String columnName) {
        return "alter table ${entity.getTable()} alter column ${api.getColumnName(attribute, columnName)} ${newType} ^"
    }

    String generateAlterColumnMandatoryStatement(def entity, def attribute, String columnName) {
        throw new UnsupportedOperationException("Firebird unsupported alter mandatory constraints for exists column. Table - ${entity.getTable()}; Column - ${columnName}")
    }

    String getLongIdentityType() {
        return 'bigint'
    }

    String getIntegerIdentityType() {
        return 'integer'
    }

    String getParams(def entity, def attribute, boolean includeNotNull) {
        StringBuilder params = new StringBuilder()
        //length
        def attrType = attribute.getType()
        if ((attrType.fqn?.equals('java.lang.String') || attrType.fqn == 'java.lang.Character') && attribute.getLength()) {
            Integer length = api.parseLength(entity, attribute)
            if (length != null)
                params.append('(').append(length).append(')')
        } else if (attribute.isEnum() && attrType.getType()?.getId().equals('String')) {
            //enum string length
            params.append("(").append(api.getDefaultColumnLengthOfEnums()).append(")");
        }

        api.processBigDecimalParams(attribute, params)
        //not null
        if ((attribute.isMandatory() && !attribute.isId()) && includeNotNull) {
            params.append(" not null")
        }
        return params.toString()
    }

    String generateDropDatabase(def dbName) {
        return 'DROP DATABASE;'
    }

    String generateCreateDatabase(def dbName) {
        def userHome = System.getProperty('user.home')
        return "CREATE DATABASE '${userHome}/firebird/data/${dbName}' page_size 8192;"
    }

    class GeneratedScript {
        String code
        String constraint
    }
}