package com.jkojote.libraryserver.application;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("queryRunner")
class QueryToJsonRunnerImpl implements QueryToJsonRunner {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    QueryToJsonRunnerImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public JsonObject runQuery(String sql) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
        return buildResponse(rowSet);
    }

    @Override
    public JsonObject runQuery(String sql, Object... params) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, params);
        return buildResponse(rowSet);
    }

    private JsonObject buildResponse(SqlRowSet rowSet) {
        SqlRowSetMetaData meta = rowSet.getMetaData();
        JsonObject result = new JsonObject();
        JsonArray columns = new JsonArray();
        JsonArray rows = new JsonArray();
        result.add("columns", columns);
        result.add("rows", rows);
        List<String> columnNames = getColumnNames(meta);
        for (String columnName : columnNames) {
            columns.add(new JsonPrimitive(columnName));
        }
        while (rowSet.next()) {
            JsonArray row = new JsonArray();
            for (String columnName : columnNames) {
                Object o = rowSet.getObject(columnName);
                if (o != null)
                    row.add(new JsonPrimitive(o.toString()));
                else
                    row.add(new JsonPrimitive("null"));
            }
            rows.add(row);
        }
        return result;
    }

    private List<String> getColumnNames(SqlRowSetMetaData metaData) {
        List<String> res = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            res.add(metaData.getColumnName(i));
        }
        return res;
    }
}
